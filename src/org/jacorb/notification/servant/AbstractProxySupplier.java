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
import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.PushOperation;
import org.jacorb.notification.engine.RetryException;
import org.jacorb.notification.engine.RetryStrategy;
import org.jacorb.notification.engine.TaskExecutor;
import org.jacorb.notification.engine.TaskProcessorRetryStrategy;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.queue.EventQueueFactoryDependency;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetListener;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdminHelper;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublish;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribeOperations;

/**
 * Abstract base class for ProxySuppliers. This class provides following logic
 * for the different ProxySuppliers:
 * <ul>
 * <li>generic queue management,
 * <li>error threshold settings.
 * </ul>
 * 
 * @author Alphonse Bendt
 * @version $Id: AbstractProxySupplier.java,v 1.13 2004/07/12 11:19:56
 *          alphonse.bendt Exp $
 */

public abstract class AbstractProxySupplier extends AbstractProxy implements MessageConsumer,
        NotifySubscribeOperations, EventQueueFactoryDependency
{
    private static final EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    ////////////////////////////////////////

    /**
     * Check if there are pending Messages and deliver them to the Consumer. the
     * operation is not executed immediately. instead it is scheduled to the
     * Push Thread Pool. only initialized for ProxyPushSuppliers.
     */
    protected Runnable scheduleDeliverPendingMessagesOperation_;

    private TaskExecutor taskExecutor_;

    private Disposable disposeTaskExecutor_;

    private EventQueue pendingMessages_;

    private int errorThreshold_;

    private ConsumerAdmin consumerAdmin_;

    private EventQueueFactory eventQueueFactory_;

    /**
     * lock variable used to control access to the reference to the pending
     * messages queue. calls to set_qos may cause the MessageQueue instance to
     * be replaced.
     */
    private final Object pendingMessagesRefLock_ = new Object();

    private NotifyPublishOperations proxyOfferListener_;

    private NotifyPublish offerListener_;

    /**
     * flag to indicate that this ProxySupplier may invoke remote calls during
     * deliverMessage.
     */
    private boolean enabled_ = true;

    ////////////////////////////////////////

    public AbstractProxySupplier()
    {
    }

    protected AbstractProxySupplier(ChannelContext channelContext)
    {
        super();

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
                        logger_.fatalError("scheduleTimedPushTask failed", e);
                    }
                }
            };
        }
    }

    public void configure(Configuration conf)
    {
        super.configure(conf);

        errorThreshold_ = conf.getAttributeAsInteger(Attributes.EVENTCONSUMER_ERROR_THRESHOLD,
                Default.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD);
    }

    ////////////////////////////////////////

    public final void setEventQueueFactory(EventQueueFactory factory)
    {
        eventQueueFactory_ = factory;
    }

    protected EventQueueFactory getEventQueueFactory()
    {
        return eventQueueFactory_;
    }

    public void preActivate() throws UnsupportedQoS, Exception
    {
        synchronized (pendingMessagesRefLock_)
        {
            pendingMessages_ = getEventQueueFactory().newEventQueue(qosSettings_);
        }

        if (logger_.isInfoEnabled())
            logger_.info("set Error Threshold to : " + errorThreshold_);

        qosSettings_.addPropertySetListener(
                new String[] { OrderPolicy.value, DiscardPolicy.value },
                eventQueueConfigurationChangedCB);
    }

    /**
     * configure pending messages queue. the queue is reconfigured according to
     * the current QoS Settings. the contents of the queue are reorganized
     * according to the new OrderPolicy.
     */
    private void configureEventQueue() throws UnsupportedQoS
    {
        EventQueue _newQueue = getEventQueueFactory().newEventQueue(qosSettings_);

        try
        {
            synchronized (pendingMessagesRefLock_)
            {
                if (!pendingMessages_.isEmpty())
                {
                    Message[] _allEvents = pendingMessages_.getAllEvents(true);
                    for (int x = 0; x < _allEvents.length; ++x)
                        _newQueue.put(_allEvents[x]);
                }
                pendingMessages_ = _newQueue;
            }
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e.getMessage());
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

    public void setTaskExecutor(TaskExecutor executor)
    {
        if (taskExecutor_ == null)
        {
            taskExecutor_ = executor;
        }
        else
        {
            throw new IllegalArgumentException("TaskExecutor should be set only once!");
        }
    }

    public void setTaskExecutor(TaskExecutor executor, Disposable disposeTaskExecutor)
    {
        setTaskExecutor(executor);

        disposeTaskExecutor_ = disposeTaskExecutor;
    }

    public int getPendingMessagesCount()
    {
        synchronized (pendingMessagesRefLock_)
        {
            return pendingMessages_.getSize();
        }
    }

    public boolean hasPendingData()
    {
        synchronized (pendingMessagesRefLock_)
        {
            if (!pendingMessages_.isEmpty())
            {
                return true;
            }

            return false;
        }
    }

    /**
     * put a Message in the queue of pending Messages.
     * 
     * @param message
     *            the <code>Message</code> to queue.
     */
    protected void enqueue(Message message)
    {
        synchronized (pendingMessagesRefLock_)
        {
            pendingMessages_.put(message);
        }

        if (logger_.isDebugEnabled())
        {
            logger_.debug("added " + message + " to pending Messages.");
        }
    }

    protected Message getMessageBlocking() throws InterruptedException
    {
        synchronized (pendingMessagesRefLock_)
        {
            return pendingMessages_.getEvent(true);
        }
    }

    protected Message getMessageNoBlock()
    {
        synchronized (pendingMessagesRefLock_)
        {
            try
            {
                return pendingMessages_.getEvent(false);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();

                return null;
            }
        }
    }

    protected Message[] getAllMessages()
    {
        synchronized (pendingMessagesRefLock_)
        {
            try
            {
                return pendingMessages_.getAllEvents(false);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();

                return null;
            }
        }
    }

    /**
     * @param max
     *            maximum number of messages
     * @return an array containing at most max Messages
     */
    protected Message[] getUpToMessages(int max)
    {
        try
        {
            synchronized (pendingMessagesRefLock_)
            {
                return pendingMessages_.getEvents(max, false);
            }
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return null;
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
            synchronized (pendingMessagesRefLock_)
            {
                if (pendingMessages_.getSize() >= min)
                {
                    return pendingMessages_.getAllEvents(true);
                }
            }
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        return null;
    }

    public int getErrorThreshold()
    {
        return errorThreshold_;
    }

    public final void dispose()
    {
        super.dispose();

        if (disposeTaskExecutor_ != null)
        {
            disposeTaskExecutor_.dispose();
        }
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

    /**
     * factory method for new ProxyPullSuppliers.
     */
    static AbstractProxySupplier newProxyPullSupplier(AbstractAdmin admin, ClientType clientType)
    {
        AbstractProxySupplier _servant;

        switch (clientType.value()) {
        case ClientType._ANY_EVENT:
            _servant = new ProxyPullSupplierImpl();
            break;

        case ClientType._STRUCTURED_EVENT:
            _servant = new StructuredProxyPullSupplierImpl();
            break;

        case ClientType._SEQUENCE_EVENT:
            _servant = new SequenceProxyPullSupplierImpl();

            break;

        default:
            throw new BAD_PARAM();
        }
        admin.getChannelContext().resolveDependencies(_servant);

        _servant.consumerAdmin_ = ConsumerAdminHelper.narrow(admin.activate());

        _servant.configure(((org.jacorb.orb.ORB) admin.getORB()).getConfiguration());
        return _servant;
    }

    /**
     * factory method for new ProxyPushSuppliers.
     */
    static AbstractProxySupplier newProxyPushSupplier(AbstractAdmin admin, ClientType clientType)
    {
        AbstractProxySupplier _servant;

        switch (clientType.value()) {

        case ClientType._ANY_EVENT:
            _servant = new ProxyPushSupplierImpl();
            break;

        case ClientType._STRUCTURED_EVENT:
            _servant = new StructuredProxyPushSupplierImpl();
            break;

        case ClientType._SEQUENCE_EVENT:
            _servant = new SequenceProxyPushSupplierImpl();
            break;

        default:
            throw new BAD_PARAM("The ClientType: " + clientType.value() + " is unknown");
        }
        admin.getChannelContext().resolveDependencies(_servant);

        _servant.consumerAdmin_ = ConsumerAdminHelper.narrow(admin.activate());

        _servant.configure(((org.jacorb.orb.ORB) admin.getORB()).getConfiguration());
        return _servant;
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
        if (RetryStrategy.isFatalException(error))
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

        RetryStrategy _retry = getRetryStrategy(this, operation);

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

    private RetryStrategy getRetryStrategy(MessageConsumer mc, PushOperation op)
    {
        return new TaskProcessorRetryStrategy(mc, op, getTaskProcessor());
    }
    
    public boolean isRetryAllowed() {
        return !isDisposed() && getErrorCounter() < getErrorThreshold();
    }
}