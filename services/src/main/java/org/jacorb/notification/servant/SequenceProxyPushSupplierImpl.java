package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.*;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetAdapter;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.PacingInterval;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierPOATie;
import org.omg.CosNotifyComm.SequencePushConsumer;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.TimeBase.TimeTHelper;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @jmx.mbean extends = "AbstractProxyPushSupplierMBean"
 * @jboss.xmbean
 * 
 * @author Alphonse Bendt
 */

public class SequenceProxyPushSupplierImpl extends AbstractProxyPushSupplier implements
        SequenceProxyPushSupplierOperations, SequenceProxyPushSupplierImplMBean
{
    private class PushSequenceOperation implements PushOperation
    {
        private final StructuredEvent[] structuredEvents_;

        public PushSequenceOperation(StructuredEvent[] structuredEvents)
        {
            structuredEvents_ = structuredEvents;
        }

        public void invokePush() throws Disconnected
        {
            deliverPendingMessagesInternal(structuredEvents_);
        }

        public void dispose()
        {
            // nothing to do
        }
    }

    public SequenceProxyPushSupplierImpl(IAdmin admin, ORB orb, POA poa, Configuration config,
            TaskProcessor taskProcessor, PushTaskExecutorFactory pushTaskExecutorFactory, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, config, taskProcessor, pushTaskExecutorFactory, offerManager,
                subscriptionManager, consumerAdmin);

        configureMaxBatchSize();

        configurePacingInterval();

        timerTask_ = new Runnable()
        {
            public void run()
            {
                if (isEnabled())
                {
                    scheduleFlush();
                }
            }
        };
        
        qosSettings_.addPropertySetListener(MaximumBatchSize.value, new PropertySetAdapter()
        {
            public void actionPropertySetChanged(PropertySet source)
            {
                configureMaxBatchSize();
            }
        });

        qosSettings_.addPropertySetListener(PacingInterval.value, new PropertySetAdapter()
        {
            public void actionPropertySetChanged(PropertySet source)
            {
                configurePacingInterval();
            }
        });
    }

    private final Runnable timerTask_;
    
    /**
     * The connected SequencePushConsumer.
     */
    private SequencePushConsumer sequencePushConsumer_;

    /**
     * registration for the Scheduled DeliverTask.
     */
    private ScheduledFuture taskId_;

    /**
     * maximum queue size before a delivery is forced.
     */
    private final AtomicInteger maxBatchSize_ = new AtomicInteger(1);

    /**
     * how long to wait between two scheduled deliveries.
     * (0 equals no scheduled deliveries).
     */
    private final AtomicLong pacingInterval_ = new AtomicLong(0);

    private long timeSpent_ = 0;

    public ProxyType MyType()
    {
        return ProxyType.PUSH_SEQUENCE;
    }

    public boolean pushEvent()
    {
        final Message[] _messages = getAtLeastMessages(maxBatchSize_.get());
        
        return pushMessages(_messages);
    }

    public void flushPendingEvents()
    {
        while (true)
        {
            try
            {
                final boolean _acquired = pushSync_.tryAcquire(1000, TimeUnit.MILLISECONDS);

                if (_acquired)
                {
                    try
                    {
                        final Message[] _messages = getUpToMessages(maxBatchSize_.get());

                        if (_messages == null)
                        {
                            break;
                        }

                        if (_messages.length == 0)
                        {
                            break;
                        }

                        final boolean _success = pushMessages(_messages);

                        if (!_success)
                        {
                            break;
                        }
                    } finally
                    {
                        pushSync_.release();
                    }
                }
            } catch (InterruptedException e)
            {
                break;
            }
        }
    }
    
    private boolean pushMessages(final Message[] messages)
    {
        if (messages == null)
        {
            return false;
        }
        
        if (messages.length == 0)
        {
            return false;
        }
        
        final StructuredEvent[] _structuredEvents = new StructuredEvent[messages.length];

        for (int x = 0; x < messages.length; ++x)
        {
            _structuredEvents[x] = messages[x].toStructuredEvent();

            messages[x].dispose();
        }

        try
        {
            deliverPendingMessagesInternal(_structuredEvents);

            return true;
        } catch (Exception e)
        {
            final PushSequenceOperation _failedOperation = new PushSequenceOperation(
                    _structuredEvents);

            handleFailedPushOperation(_failedOperation, e);

            return false;
        }
    }
    
    private void deliverPendingMessagesInternal(final StructuredEvent[] structuredEvents)
            throws Disconnected
    {
        long now = System.currentTimeMillis();
        sequencePushConsumer_.push_structured_events(structuredEvents);
        timeSpent_ += (System.currentTimeMillis() - now);
        resetErrorCounter();
    }

    public void connect_sequence_push_consumer(SequencePushConsumer consumer)
            throws AlreadyConnected, TypeError
    {
        logger_.debug("connect_sequence_push_consumer");

        checkIsNotConnected();

        sequencePushConsumer_ = consumer;

        connectClient(consumer);

        startTimerTask();
    }

    protected void connectionResumed()
    {
        scheduleFlush();
        
        startTimerTask();
    }

    protected void connectionSuspended()
    {
        stopTimerTask();
    }

    public void disconnect_sequence_push_supplier()
    {
        destroy();
    }

    protected void disconnectClient()
    {
        stopTimerTask();

        sequencePushConsumer_.disconnect_sequence_push_consumer();
        sequencePushConsumer_ = null;
    }

    private void startTimerTask()
    {
        if (pacingInterval_.get() > 0 && taskId_ == null)
        {
            final long _interval = timeT2millis();
            taskId_ = getTaskProcessor().executeTaskPeriodically(_interval,
                    timerTask_, true);
        }
    }

    public long timeT2millis()
    {
        final long timeT = pacingInterval_.get();
        return time2millis(timeT);
    }

    public static long time2millis(final long timeT)
    {
        return timeT / 10000;
    }

    synchronized private void stopTimerTask()
    {
        if (taskId_ != null)
        {
            taskId_.cancel(true);
            taskId_ = null;
        }
    }

    private void checkTimerTask()
    {
        if (getConnected() && pacingInterval_.get() > 0)
        {
            stopTimerTask();
            
            startTimerTask();
        }
        else
        {
            stopTimerTask();
        }
    }

    private boolean configurePacingInterval()
    {
        if (qosSettings_.containsKey(PacingInterval.value))
        {
            long _pacingInterval = TimeTHelper.extract(qosSettings_.get(PacingInterval.value));

            if (pacingInterval_.get() != _pacingInterval)
            {
                if (logger_.isInfoEnabled())
                {
                    logger_.info("set PacingInterval=" + _pacingInterval);
                }
                pacingInterval_.set(_pacingInterval);

                checkTimerTask();

                return true;
            }
        }
        return false;
    }

    private boolean configureMaxBatchSize()
    {
        if (qosSettings_.containsKey(MaximumBatchSize.value))
        {
            int _maxBatchSize = qosSettings_.get(MaximumBatchSize.value).extract_long();

            if (maxBatchSize_.get() != _maxBatchSize)
            {
                if (logger_.isInfoEnabled())
                {
                    logger_.info("set MaxBatchSize=" + _maxBatchSize);
                }

                maxBatchSize_.set(_maxBatchSize);

                return true;
            }
        }

        return false;
    }

    public Servant newServant()
    {
        return new SequenceProxyPushSupplierPOATie(this);
    }

    protected long getCost()
    {
        return timeSpent_;
    }
}