/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.jacorb.notification.servant;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.PushTaskExecutor;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.RetryException;
import org.jacorb.notification.engine.RetryStrategy;
import org.jacorb.notification.engine.RetryStrategyFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.IProxyPushSupplier;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import edu.emory.mathcs.backport.java.util.concurrent.Semaphore;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

/**
 * @jmx.mbean extends = "AbstractProxySupplierMBean"
 * @jboss.xmbean
 * 
 * @--jmx.notification name = "notification.proxy.push_failed" description = "push to
 *                     ProxyPushConsumer failed" notificationType = "java.lang.String"
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractProxyPushSupplier extends AbstractProxySupplier implements
        IProxyPushSupplier
{
    private static final String NOTIFY_PUSH_FAILED = "notification.proxy.push_failed";

    private final AtomicReference retryStrategyFactory_;

    /**
     * flag to indicate that this ProxySupplier should invoke remote calls (push) during
     * deliverMessage.
     */
    private final AtomicBoolean enabled_ = new AtomicBoolean(true);

    private final PushTaskExecutor pushTaskExecutor_;

    private final AtomicInteger pushCounter_ = new AtomicInteger(0);

    private final AtomicInteger pushErrors_ = new AtomicInteger(0);

    /**
     * number of concurrent push operations allowed.
     */
    protected final Semaphore pushSync_ = new Semaphore(1);
    
    private final PushTaskExecutor.PushTask pushTask_ = new PushTaskExecutor.PushTask()
    {
        public void doPush()
        {
            if (isEnabled())
            {
                tryPushEvent();
            }
        }

        public void cancel()
        {
            // ignore, only depends on settings of ProxyPushSupplier
        }
    };
    
    private final PushTaskExecutor.PushTask flushTask_ = new PushTaskExecutor.PushTask()
    {
        public void doPush()
        {
            if (isEnabled())
            {
                flushPendingEvents();
            }
        }
        
        public void cancel()
        {
            // ignore, only depends on settings of ProxyPushSupplier
        }
    };

    public AbstractProxyPushSupplier(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory,
            OfferManager offerManager, SubscriptionManager subscriptionManager,
            ConsumerAdmin consumerAdmin) throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager, subscriptionManager,
                consumerAdmin);

        pushTaskExecutor_ = pushTaskExecutorFactory.newExecutor(this);

        retryStrategyFactory_ = new AtomicReference(newRetryStrategyFactory(conf, taskProcessor));

        eventTypes_.add(NOTIFY_PUSH_FAILED);
    }

    private boolean tryPushEvent()
    {
        try
        {
            boolean _acquired = pushSync_.tryAcquire(1000, TimeUnit.MILLISECONDS);
            
            if (_acquired)
            {
                try
                {
                    return pushEvent();
                }
                finally
                {
                    pushSync_.release();
                }
            }
            
            // the scheduled push was not processed.
            // therfor we need to schedule a push again.
            schedulePush();
        }
        catch (InterruptedException e)
        {
            // ignored
        }
        
        return true;
    }
    
    protected abstract boolean pushEvent();

    protected void handleFailedPushOperation(PushOperation operation, Exception error)
    {
        logger_.warn("handle failed pushoperation", error);
        
        if (isDestroyed())
        {
            operation.dispose();

            return;
        }

        StringWriter out = new StringWriter();
        error.printStackTrace(new PrintWriter(out));
        sendNotification(NOTIFY_PUSH_FAILED, "Push Operation failed", out.toString());

        pushErrors_.getAndIncrement();

        incErrorCounter();

        if (AbstractRetryStrategy.isFatalException(error))
        {
            // push operation caused a fatal exception
            // destroy the ProxySupplier
            if (logger_.isWarnEnabled())
            {
                logger_.warn("push raised " + error + ": will destroy ProxySupplier, "
                        + "disconnect Consumer", error);
            }

            operation.dispose();
            destroy();
        }
        else if (!isRetryAllowed())
        {
            operation.dispose();

            destroy();
        }
        else if (!isDestroyed())
        {
            final RetryStrategy _retry = newRetryStrategy(this, operation);

            try
            {
                _retry.retry();
            } catch (RetryException e)
            {
                logger_.error("retry failed", e);

                _retry.dispose();
                destroy();
            }
        }
        else
        {
            // retry allowed && isDestroyed
            throw new IllegalStateException("should not happen");
        }
    }

    private RetryStrategy newRetryStrategy(IProxyPushSupplier pushSupplier,
            PushOperation pushOperation)
    {
        return ((RetryStrategyFactory) retryStrategyFactory_.get()).newRetryStrategy(pushSupplier,
                pushOperation);
    }

    private RetryStrategyFactory newRetryStrategyFactory(Configuration config,
            TaskProcessor taskProcessor) throws ConfigurationException
    {
        final String factoryName = config.getAttribute(Attributes.RETRY_STRATEGY_FACTORY,
                Default.DEFAULT_RETRY_STRATEGY_FACTORY);

        try
        {
            return newRetryStrategyFactory(config, taskProcessor, factoryName);

        } catch (ClassNotFoundException e)
        {
            throw new ConfigurationException(Attributes.RETRY_STRATEGY_FACTORY, e);
        }
    }

    /**
     * @jmx.managed-attribute description = "Factory used to control RetryPolicy" access =
     *                        "read-write"
     */
    public void setRetryStrategy(String factoryName) throws ClassNotFoundException
    {
        RetryStrategyFactory factory = newRetryStrategyFactory(config_, getTaskProcessor(),
                factoryName);

        retryStrategyFactory_.set(factory);

        logger_.info("set RetryStrategyFactory: " + factoryName);
    }

    /**
     * @jmx.managed-attribute description = "Factory used to control RetryPolicy" access =
     *                        "read-write"
     */
    public String getRetryStrategy()
    {
        return retryStrategyFactory_.get().getClass().getName();
    }

    private RetryStrategyFactory newRetryStrategyFactory(Configuration config,
            TaskProcessor taskProcessor, String factoryName) throws ClassNotFoundException
    {
        final Class factoryClazz = ObjectUtil.classForName(factoryName);

        final MutablePicoContainer pico = new DefaultPicoContainer();

        pico.registerComponentInstance(TaskProcessor.class, taskProcessor);

        pico.registerComponentImplementation(RetryStrategyFactory.class, factoryClazz);

        pico.registerComponentInstance(config);

        return (RetryStrategyFactory) pico.getComponentInstance(RetryStrategyFactory.class);
    }

    public final void schedulePush()
    {
        if (isEnabled())
        {
            scheduleTask(pushTask_);
        }
    }
    
    public void scheduleFlush()
    {
        if (isEnabled())
        {
            scheduleTask(flushTask_);
        }
    }

    public final void scheduleTask(PushTaskExecutor.PushTask pushTask)
    {
        if (!isDestroyed() && !isSuspended())
        {
            pushTaskExecutor_.executePush(pushTask);
        }
    }

    public void flushPendingEvents()
    {
        while (tryPushEvent())
        {
            // nothing
        }
    }
    
    public final void messageQueued()
    {
        if (isEnabled())
        {
            schedulePush();
        }
    }

    public void resetErrorCounter()
    {
        super.resetErrorCounter();

        pushCounter_.getAndIncrement();

        enableDelivery();
    }

    public void disableDelivery()
    {
        boolean _wasEnabled = enabled_.getAndSet(false);

        if (_wasEnabled)
        {
            logger_.warn("disabled delivery to ProxySupplier temporarily");
        }
    }

    protected boolean isEnabled()
    {
        return enabled_.get();
    }

    private void enableDelivery()
    {
        boolean _wasEnabled = enabled_.getAndSet(true);

        if (!_wasEnabled)
        {
            logger_.debug("enabled delivery to ProxySupplier");
        }
    }

    /**
     * @jmx.managed-attribute description = "Total Number of Push Operations" access = "read-only"
     */
    public int getPushOperationCount()
    {
        return pushCounter_.get();
    }

    /**
     * @jmx.managed-attribute description = "Number of failed Push-Operations" access = "read-only"
     */
    public int getPushErrorCount()
    {
        return pushErrors_.get();
    }

    /**
     * @jmx.managed-attribute description = "Average time (in ms) per Push-Operation" access =
     *                        "read-only"
     */
    public int getAveragePushDuration()
    {
        return (int) getCost() / getPushOperationCount();
    }
}
