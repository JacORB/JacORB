package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.AbstractRetryStrategy;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.RetryException;
import org.jacorb.notification.engine.RetryStrategy;
import org.jacorb.notification.engine.RetryStrategyFactory;
import org.jacorb.notification.engine.TaskExecutor;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.queue.MessageQueueAdapter;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetListener;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublish;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.PortableServer.POA;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Abstract base class for ProxySuppliers. This class provides following logic for the different
 * ProxySuppliers:
 * <ul>
 * <li>queue management,
 * <li>error threshold settings.
 * </ul>
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxySupplier extends AbstractProxy implements MessageConsumer,
        NotifySubscribeOperations
{
    private static final EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    private static final Message[] EMPTY_MESSAGE = new Message[0];

    // //////////////////////////////////////

    /**
     * Check if there are pending Messages and deliver them to the Consumer. the operation is not
     * executed immediately. instead it is scheduled to the Push Thread Pool. only initialized for
     * ProxyPushSuppliers.
     */
    protected final Runnable scheduleDeliverPendingMessagesOperation_;

    private static final Runnable NO_OP = new Runnable()
    {
        public void run()
        {

        }
    };

    private final TaskExecutor taskExecutor_;

    private final RWLockEventQueueDecorator pendingMessages_;

    private final int errorThreshold_;

    private final ConsumerAdmin consumerAdmin_;

    private final EventQueueFactory eventQueueFactory_;

    private final RetryStrategyFactory retryStrategyFactory_;

    private NotifyPublishOperations proxyOfferListener_;

    private NotifyPublish offerListener_;

    /**
     * flag to indicate that this ProxySupplier may invoke remote calls during deliverMessage.
     */
    private boolean enabled_ = true;

    // //////////////////////////////////////

    protected AbstractProxySupplier(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, TaskExecutor taskExecutor, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager, subscriptionManager);

        taskExecutor_ = taskExecutor;

        consumerAdmin_ = consumerAdmin;

        eventQueueFactory_ = new EventQueueFactory(conf);

        errorThreshold_ = conf.getAttributeAsInteger(Attributes.EVENTCONSUMER_ERROR_THRESHOLD,
                Default.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD);

        if (logger_.isInfoEnabled())
        {
            logger_.info("set Error Threshold to : " + errorThreshold_);
        }

        if (isPushSupplier())
        {
            scheduleDeliverPendingMessagesOperation_ = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        getTaskProcessor().scheduleTimedPushTask(AbstractProxySupplier.this);
                    } catch (InterruptedException e)
                    {
                        logger_.info("scheduleTimedPushTask interrupted", e);
                    }
                }
            };
        }
        else
        {
            scheduleDeliverPendingMessagesOperation_ = NO_OP;
        }

        qosSettings_.addPropertySetListener(
                new String[] { OrderPolicy.value, DiscardPolicy.value },
                eventQueueConfigurationChangedCB);

        try
        {
            MessageQueueAdapter initialEventQueue = getMessageQueueFactory().newMessageQueue(
                    qosSettings_);

            pendingMessages_ = new RWLockEventQueueDecorator(initialEventQueue);
        } catch (InterruptedException e)
        {
            throw new RuntimeException();
        }

        retryStrategyFactory_ = newRetryStrategyFactory(conf, taskProcessor);
    }

    // //////////////////////////////////////

    protected EventQueueFactory getMessageQueueFactory()
    {
        return eventQueueFactory_;
    }

    /**
     * @deprecated TODO remove
     */
    public void preActivate() throws UnsupportedQoS, Exception
    {
        // nothing
    }

    /**
     * configure pending messages queue. the queue is reconfigured according to the current QoS
     * Settings. the contents of the queue are reorganized according to the new OrderPolicy.
     */
    private final void configureEventQueue() // throws UnsupportedQoS
    {
        MessageQueueAdapter _newQueue = getMessageQueueFactory().newMessageQueue(qosSettings_);

        try
        {
            pendingMessages_.replaceDelegate(_newQueue);
        } catch (InterruptedException e)
        {
            // ignored
        }
    }

    private PropertySetListener eventQueueConfigurationChangedCB = new PropertySetListener()
    {
        public void validateProperty(Property[] p, List errors)
        {
        }

        public void actionPropertySetChanged(PropertySet source) throws UnsupportedQoS
        {
            configureEventQueue();
        }
    };

    public TaskExecutor getExecutor()
    {
        return taskExecutor_;
    }

    public int getPendingMessagesCount()
    {
        try
        {
            return pendingMessages_.getPendingMessagesCount();
        } catch (InterruptedException e)
        {
            return -1;
        }
    }

    public boolean hasPendingData()
    {
        try
        {
            return pendingMessages_.hasPendingMessages();
        } catch (InterruptedException e)
        {
            return false;
        }
    }

    /**
     * put a copy of the Message in the queue of pending Messages.
     * 
     * @param message
     *            the <code>Message</code> to queue.
     */
    protected void enqueue(Message message)
    {
        final Message _messageClone = (Message) message.clone();

        try
        {
            pendingMessages_.enqeue(_messageClone);

            if (logger_.isDebugEnabled())
            {
                logger_.debug("added " + message + " to pending Messages.");
            }
        } catch (InterruptedException e)
        {
            _messageClone.dispose();
            logger_.info("enqueue was interrupted", e);
        }
    }

    public Message getMessageBlocking() throws InterruptedException
    {
        return pendingMessages_.getMessageBlocking();
    }

    protected Message getMessageNoBlock()
    {
        try
        {
            return pendingMessages_.getMessageNoBlock();
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return null;
        }
    }

    protected Message[] getAllMessages()
    {
        try
        {
            return pendingMessages_.getAllMessages();
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return EMPTY_MESSAGE;
        }
    }

    public void deliverMessage(final Message message)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("deliverMessage() connected=" + isConnected() + " suspended="
                    + isSuspended() + " enabled=" + isEnabled());
        }

        if (isConnected())
        {
            enqueue(message);

            messageDelivered();
        }
    }

    protected abstract void messageDelivered();

    /**
     * @param max
     *            maximum number of messages
     * @return an array containing at most max Messages
     */
    protected Message[] getUpToMessages(int max)
    {
        try
        {
            return pendingMessages_.getUpToMessages(max);
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return EMPTY_MESSAGE;
        }
    }

    /**
     * @param min
     *            minimum number of messages
     * @return an array containing the requested number of Messages or null
     */
    protected Message[] getAtLeastMessages(int min)
    {
        try
        {
            return pendingMessages_.getAtLeastMessages(min);
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return EMPTY_MESSAGE;
        }
    }

    public int getErrorThreshold()
    {
        return errorThreshold_;
    }

    public final void dispose()
    {
        super.dispose();
    }

    public final ConsumerAdmin MyAdmin()
    {
        return consumerAdmin_;
    }

    public final void subscription_change(EventType[] added, EventType[] removed)
            throws InvalidEventType
    {
        subscriptionManager_.subscription_change(added, removed);
    }

    public final EventType[] obtain_offered_types(ObtainInfoMode obtainInfoMode)
    {
        EventType[] _offeredTypes = EMPTY_EVENT_TYPE_ARRAY;

        switch (obtainInfoMode.value()) {
        case ObtainInfoMode._ALL_NOW_UPDATES_ON:
            registerListener();
            _offeredTypes = offerManager_.obtain_offered_types();
            break;
        case ObtainInfoMode._ALL_NOW_UPDATES_OFF:
            _offeredTypes = offerManager_.obtain_offered_types();
            removeListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_ON:
            registerListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_OFF:
            removeListener();
            break;
        default:
            throw new IllegalArgumentException("Illegal ObtainInfoMode");
        }

        return _offeredTypes;
    }

    private void registerListener()
    {
        if (proxyOfferListener_ == null)
        {
            final NotifyPublishOperations _listener = getOfferListener();

            if (_listener != null)
            {
                proxyOfferListener_ = new NotifyPublishOperations()
                {
                    public void offer_change(EventType[] added, EventType[] removed)
                    {
                        try
                        {
                            _listener.offer_change(added, removed);
                        } catch (NO_IMPLEMENT e)
                        {
                            logger_.info("disable offer_change for connected Consumer.", e);

                            removeListener();
                        } catch (InvalidEventType e)
                        {
                            logger_.error("invalid event type", e);
                        } catch (Exception e)
                        {
                            logger_.error("offer_change failed", e);
                        }
                    }
                };

                offerManager_.addListener(proxyOfferListener_);
            }
        }
    }

    protected void removeListener()
    {
        if (proxyOfferListener_ != null)
        {
            offerManager_.removeListener(proxyOfferListener_);
            proxyOfferListener_ = null;
        }
    }

    final NotifyPublishOperations getOfferListener()
    {
        return offerListener_;
    }

    protected void connectClient(org.omg.CORBA.Object client)
    {
        super.connectClient(client);

        try
        {
            offerListener_ = NotifyPublishHelper.narrow(client);

            logger_.debug("successfully narrowed connecting Client to IF NotifyPublish");
        } catch (Throwable t)
        {
            logger_.info("disable offer_change for connecting Consumer");
        }
    }

    public synchronized void enableDelivery()
    {
        enabled_ = true;
    }

    public synchronized void disableDelivery()
    {
        enabled_ = false;
    }

    protected synchronized boolean isEnabled()
    {
        return enabled_;
    }

    public boolean isPushSupplier()
    {
        switch (MyType().value()) {
        case ProxyType._PUSH_ANY:
        // fallthrough
        case ProxyType._PUSH_STRUCTURED:
        // fallthrough
        case ProxyType._PUSH_SEQUENCE:
        // fallthrough
        case ProxyType._PUSH_TYPED:
            return true;
        default:
            return false;
        }
    }

    protected void handleFailedPushOperation(PushOperation operation, Throwable error)
    {
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
            dispose();

            return;
        }

        RetryStrategy _retry = newRetryStrategy(this, operation);

        try
        {
            _retry.retry();
        } catch (RetryException e)
        {
            logger_.error("retry failed", e);

            _retry.dispose();
            dispose();
        }
    }

    private RetryStrategy newRetryStrategy(MessageConsumer mc, PushOperation op)
    {
        return retryStrategyFactory_.newRetryStrategy(mc, op);
    }

    private RetryStrategyFactory newRetryStrategyFactory(Configuration config,
            TaskProcessor taskProcessor) throws ConfigurationException
    {
        String factoryName = config.getAttribute(Attributes.RETRY_STRATEGY_FACTORY,
                Default.DEFAULT_RETRY_STRATEGY_FACTORY);

        try
        {
            Class factoryClazz = ObjectUtil.classForName(factoryName);

            MutablePicoContainer pico = new DefaultPicoContainer();

            pico.registerComponentInstance(TaskProcessor.class, taskProcessor);

            pico.registerComponentImplementation(RetryStrategyFactory.class, factoryClazz);

            return (RetryStrategyFactory) pico.getComponentInstance(RetryStrategyFactory.class);

        } catch (ClassNotFoundException e)
        {
            throw new ConfigurationException(Attributes.RETRY_STRATEGY_FACTORY, e);
        }
    }

    public boolean isRetryAllowed()
    {
        return !isDisposed() && getErrorCounter() < getErrorThreshold();
    }
}