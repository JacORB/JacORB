package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.notification.ChannelContext;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.util.TaskExecutor;
import org.jacorb.util.Environment;

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
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribeOperations;

import java.util.List;

/**
 * Abstract base class for ProxySuppliers.
 * This class provides following logic for the different
 * ProxySuppliers:
 * <ul>
 * <li> generic queue management,
 * <li> error threshold settings.
 * </ul>
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxySupplier
    extends AbstractProxy
    implements MessageConsumer,
               NotifySubscribeOperations

{
    private final static EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    ////////////////////////////////////////

    private TaskExecutor taskExecutor_;

    private Disposable disposeTaskExecutor_;

    private EventQueue pendingMessages_;

    private int errorThreshold_;

    /**
     * lock variable used to control access to the reference to the
     * pending messages queue.
     */
    private Object pendingMessagesRefLock_ = new Object();

    private NotifyPublishOperations offerListener_;

    ////////////////////////////////////////

    protected AbstractProxySupplier(AbstractAdmin admin,
                                    ChannelContext channelContext)
    {
        super(admin,
              channelContext);
    }

    ////////////////////////////////////////

    public void preActivate() throws UnsupportedQoS
    {
        synchronized (pendingMessagesRefLock_)
        {
            pendingMessages_ = EventQueueFactory.newEventQueue(qosSettings_);
        }

        errorThreshold_ =
            Environment.getIntPropertyWithDefault(Configuration.EVENTCONSUMER_ERROR_THRESHOLD,
                                                  Default.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD);

        if (logger_.isInfoEnabled())
        {
            logger_.info("set Error Threshold to : " + errorThreshold_);
        }

        qosSettings_.addPropertySetListener(new String[] {OrderPolicy.value, DiscardPolicy.value},
                                            eventQueueConfigurationChangedCB);
    }


    /**
     * configure pending messages queue.
     * the queue is reconfigured according to the current QoS
     * Settings. the contents of the queue are reorganized according
     * to the new OrderPolicy.
     */
    private void configureEventQueue() throws UnsupportedQoS
    {
        EventQueue _newQueue = EventQueueFactory.newEventQueue( qosSettings_ );

        try
        {
            synchronized (pendingMessagesRefLock_)
            {
                if (!pendingMessages_.isEmpty())
                {
                    Message[] _allEvents =
                        pendingMessages_.getAllEvents(true);

                    for (int x = 0; x < _allEvents.length; ++x)
                    {
                        _newQueue.put(_allEvents[x]);
                    }
                }

                pendingMessages_ = _newQueue;
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }


    private PropertySetListener eventQueueConfigurationChangedCB =
        new PropertySetListener()
        {

            public void validateProperty(Property[] p, List errors)
            {}

            public void actionPropertySetChanged(PropertySet source)
            throws UnsupportedQoS
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
            throw new IllegalArgumentException("set only once");
        }
    }


    public void setTaskExecutor(TaskExecutor executor, Disposable disposeTaskExecutor)
    {
        setTaskExecutor(executor);

        disposeTaskExecutor_ = disposeTaskExecutor;
    }


    public boolean hasPendingMessages()
    {
        synchronized (pendingMessagesRefLock_)
        {
            return !pendingMessages_.isEmpty();
        }
    }


    /**
     * put a Message in the queue of pending Messages.
     *
     * @param message the <code>Message</code> to queue.
     */
    protected void enqueue(Message message)
    {
        synchronized (pendingMessagesRefLock_)
        {
            pendingMessages_.put(message);
        }

        if (logger_.isDebugEnabled() )
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
            }
            catch (InterruptedException e)
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
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();

                return null;
            }
        }
    }


    protected Message[] getUpToMessages(int max)
    {
        try
        {
            synchronized (pendingMessagesRefLock_)
            {
                return pendingMessages_.getEvents(max, false);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            return null;
        }
    }


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
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        return null;
    }


    public int getErrorThreshold()
    {
        return errorThreshold_;
    }


    final public void dispose()
    {
        super.dispose();

        if (disposeTaskExecutor_ != null)
        {
            disposeTaskExecutor_.dispose();
        }
    }


    final public ConsumerAdmin MyAdmin()
    {
        return ConsumerAdminHelper.narrow(admin_.activate());
    }


    final public void subscription_change(EventType[] added,
                                          EventType[] removed)
    throws InvalidEventType
    {
        subscriptionManager_.subscription_change(added, removed);
    }


    final public EventType[] obtain_offered_types(ObtainInfoMode obtainInfoMode)
    {
        logger_.debug("obtain_offered_types " + obtainInfoMode.value() );

        EventType[] _offeredTypes = EMPTY_EVENT_TYPE_ARRAY;

        switch (obtainInfoMode.value())
        {
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
        if (offerListener_ == null)
        {
            final NotifyPublishOperations _listener = getOfferListener();

            if (_listener != null)
            {
                offerListener_ = new NotifyPublishOperations()
                    {
                        public void offer_change(EventType[] added, EventType[] removed)
                        {
                            try
                                {
                                    _listener.offer_change(added, removed);
                                }
                            catch (NO_IMPLEMENT e)
                                {
                                    logger_.info("disable offer_change for Consumer.", e);
                                    removeListener();
                                }
                            catch (InvalidEventType e)
                                {
                                    logger_.error("invalid event type", e);
                                }
                        }
                    };

                offerManager_.addListener(offerListener_);
            }
        }
    }


    private void removeListener()
    {
        if (offerListener_ != null)
        {
            offerManager_.removeListener(offerListener_);
            offerListener_ = null;
        }
    }


    abstract NotifyPublishOperations getOfferListener();


    /**
     * factory method for new ProxyPullSuppliers.
     */
    static AbstractProxySupplier newProxyPullSupplier(AbstractAdmin admin,
                                                      ClientType clientType)
    {

        AbstractProxySupplier _servant;

        switch ( clientType.value() )
        {
            case ClientType._ANY_EVENT:
                _servant = new ProxyPullSupplierImpl( admin,
                                                      admin.getChannelContext());
                break;

            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPullSupplierImpl( admin,
                                                         admin.getChannelContext());
                break;

            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPullSupplierImpl( admin,
                                                       admin.getChannelContext());

                break;

            default:
                throw new BAD_PARAM();
        }
        return _servant;
    }


    /**
     * factory method for new ProxyPushSuppliers.
     */
    static AbstractProxySupplier newProxyPushSupplier(AbstractAdmin admin,
                                                      ClientType clientType)
    {

        AbstractProxySupplier _servant;

        switch ( clientType.value() )
        {

            case ClientType._ANY_EVENT:
                _servant = new ProxyPushSupplierImpl( admin,
                                                      admin.getChannelContext());
                break;

            case ClientType._STRUCTURED_EVENT:
                _servant =
                    new StructuredProxyPushSupplierImpl( admin,
                                                         admin.getChannelContext());
                break;

            case ClientType._SEQUENCE_EVENT:
                _servant =
                    new SequenceProxyPushSupplierImpl( admin,
                                                       admin.getChannelContext());
                break;

            default:
                throw new BAD_PARAM("The ClientType: " + clientType.value() + " is unknown");
        }
        return (_servant);
    }
}
