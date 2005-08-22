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

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedRef;

/**
 * @jmx.mbean extends = "AbstractProxySupplierMBean"
 * @jboss.xmbean
 * 
 * @--jmx.notification    name = "notification.proxy.push_failed"
 *                      description = "push to ProxyPushConsumer failed"
 *                      notificationType = "java.lang.String" 
 *                      
 * @author Alphonse Bendt
 * @version $Id$
 */
public abstract class AbstractProxyPushSupplier extends AbstractProxySupplier implements
        IProxyPushSupplier
{
    private final String NOTIFY_PUSH_FAILED = "notification.proxy.push_failed";
    
    private final SynchronizedRef retryStrategyFactory_;

    /**
     * flag to indicate that this ProxySupplier should invoke remote calls (push) during
     * deliverMessage.
     */
    private final SynchronizedBoolean enabled_ = new SynchronizedBoolean(true);

    private final PushTaskExecutor pushTaskExecutor_;

    private final SynchronizedInt pushCounter_ = new SynchronizedInt(0);
    
    private final SynchronizedInt pushErrors_ = new SynchronizedInt(0);
    
    private final PushTaskExecutor.PushTask pushTask_ = new PushTaskExecutor.PushTask()
    {
        public void doPush()
        {
            pushPendingData();
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

        retryStrategyFactory_ = new SynchronizedRef(newRetryStrategyFactory(conf, taskProcessor));
        
        eventTypes_.add(NOTIFY_PUSH_FAILED);
    }

    protected void handleFailedPushOperation(PushOperation operation, Exception error)
    {
        if (isDestroyed())
        {
            operation.dispose();
            
            return;
        }
        
        sendNotification(NOTIFY_PUSH_FAILED, "Push Operation failed");
        
        pushErrors_.increment();
        
        incErrorCounter();
        
        if (AbstractRetryStrategy.isFatalException(error))
        {
            // push operation caused a fatal exception
            // destroy the ProxySupplier
            if (logger_.isErrorEnabled())
            {
                logger_.error("push raised " + error + ": will destroy ProxySupplier, "
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
            RetryStrategy _retry = newRetryStrategy(this, operation);

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
        return ((RetryStrategyFactory) retryStrategyFactory_.get()).newRetryStrategy(pushSupplier, pushOperation);
    }

    private RetryStrategyFactory newRetryStrategyFactory(Configuration config,
            TaskProcessor taskProcessor) throws ConfigurationException
    {
        String factoryName = config.getAttribute(Attributes.RETRY_STRATEGY_FACTORY,
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
     * @jmx.managed-attribute   description = "Factory used to control RetryPolicy"
     *                          access = "read-write"
     */
    public void setRetryStrategy(String factoryName) throws ClassNotFoundException
    {
        RetryStrategyFactory factory = newRetryStrategyFactory(config_, getTaskProcessor(), factoryName);
        
        retryStrategyFactory_.set(factory);
        
        logger_.info("set RetryStrategyFactory: " + factoryName);
    }
    
    /**
     * @jmx.managed-attribute   description = "Factory used to control RetryPolicy"
     *                          access = "read-write"
     */
    public String getRetryStrategy()
    {
        return retryStrategyFactory_.get().getClass().getName();
    }

    private RetryStrategyFactory newRetryStrategyFactory(Configuration config, TaskProcessor taskProcessor, String factoryName) throws ClassNotFoundException
    {
        Class factoryClazz = ObjectUtil.classForName(factoryName);

        MutablePicoContainer pico = new DefaultPicoContainer();

        pico.registerComponentInstance(TaskProcessor.class, taskProcessor);

        pico.registerComponentImplementation(RetryStrategyFactory.class, factoryClazz);

        pico.registerComponentInstance(config);

        return (RetryStrategyFactory) pico.getComponentInstance(RetryStrategyFactory.class);
    }

    public final void schedulePush()
    {
        if (!isDestroyed() && !isSuspended() && isEnabled())
        {
            schedulePush(pushTask_);
        }
    }

    public final void schedulePush(PushTaskExecutor.PushTask pushTask)
    {
        pushTaskExecutor_.executePush(pushTask);
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

        pushCounter_.increment();
        
        enableDelivery();
    }

    public void disableDelivery()
    {
        logger_.debug("Disable Delivery to ProxySupplier");

        enabled_.set(false);
    }

    protected boolean isEnabled()
    {
        return enabled_.get();
    }

    private void enableDelivery()
    {
        logger_.debug("Enable Delivery to ProxySupplier");

        enabled_.set(true);
    }
    
    /**
     * @jmx.managed-attribute description = "Total Number of Push Operations"
     *                        access = "read-only"
     */
    public int getPushOperationCount()
    {
        return pushCounter_.get();
    }

    /**
     * @jmx.managed-attribute description = "Number of failed Push-Operations"
     *                        access = "read-only" 
     */
    public int getPushErrorCount()
    {
        return pushErrors_.get();
    }
    
    /**
     * @jmx.managed-attribute description = "Average time (in ms) per Push-Operation"
     *                        access = "read-only"
     */
    public int getAveragePushDuration()
    {
        return (int) getCost() / getPushOperationCount();
    }
}
