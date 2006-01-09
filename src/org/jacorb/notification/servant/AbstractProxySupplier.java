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
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.queue.EventQueueFactory;
import org.jacorb.notification.queue.MessageQueue;
import org.jacorb.notification.queue.MessageQueueAdapter;
import org.jacorb.notification.queue.RWLockEventQueueDecorator;
import org.jacorb.notification.util.CollectionsWrapper;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetAdapter;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.MaxEventsPerConsumer;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublish;
import org.omg.CosNotifyComm.NotifyPublishHelper;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.PortableServer.POA;

/**
 * Abstract base class for ProxySuppliers. This class provides base functionality
 * for the different ProxySuppliers:
 * <ul>
 * <li>queue management,
 * <li>error threshold settings.
 * </ul>
 * 
 * @jmx.mbean extends = "AbstractProxyMBean"
 * @jboss.xmbean
 * 
 * @--jmx.notification    name = "notification.proxy.message_discarded"
 *                      description = "queue overflow causes messages to be discarded"
 *                      notificationType = "java.lang.String"
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxySupplier extends AbstractProxy implements MessageConsumer,
        NotifySubscribeOperations, AbstractProxySupplierMBean
{
    private static final String EVENT_MESSAGE_DISCARDED = "notification.proxy.message_discarded";

    private int numberOfDiscardedMessages_ = 0;

    private MessageQueue.DiscardListener discardListener_ = new MessageQueue.DiscardListener()
    {
        private long sendTimestamp_;
        private int discardedMessagesSinceLastBroadcast_ = 1;

        public void messageDiscarded(int maxSize)
        {
            numberOfDiscardedMessages_++;

            // max. one notification every five second
            if (!((System.currentTimeMillis() - sendTimestamp_) < 5000))
            {
                sendNotification(EVENT_MESSAGE_DISCARDED, discardedMessagesSinceLastBroadcast_
                        + " Message(s) discarded. Queue Limit: " + maxSize);
                sendTimestamp_ = System.currentTimeMillis();
                discardedMessagesSinceLastBroadcast_ = 1;
            }
            else
            {
                ++discardedMessagesSinceLastBroadcast_;
            }
        }
    };

    private static final Runnable EMPTY_RUNNABLE = new Runnable()
    {
        public void run()
        {
            // no operation
        }
    };

    private static final EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    private static final Message[] EMPTY_MESSAGE = new Message[0];

    // //////////////////////////////////////

    private final RWLockEventQueueDecorator pendingMessages_;

    private final int errorThreshold_;

    private final ConsumerAdmin consumerAdmin_;

    private final EventQueueFactory eventQueueFactory_;

    private NotifyPublishOperations proxyOfferListener_;

    private NotifyPublish offerListener_;

    // //////////////////////////////////////

    protected AbstractProxySupplier(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, OfferManager offerManager,
            SubscriptionManager subscriptionManager, ConsumerAdmin consumerAdmin)
            throws ConfigurationException
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager, subscriptionManager);

        consumerAdmin_ = consumerAdmin;

        eventQueueFactory_ = new EventQueueFactory(conf);

        errorThreshold_ = conf.getAttributeAsInteger(Attributes.EVENTCONSUMER_ERROR_THRESHOLD,
                Default.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD);

        if (logger_.isInfoEnabled())
        {
            logger_.info("set Error Threshold to : " + errorThreshold_);
        }

        qosSettings_.addPropertySetListener(new String[] { OrderPolicy.value, DiscardPolicy.value,
                MaxEventsPerConsumer.value }, eventQueueConfigurationChangedCB);

        MessageQueueAdapter initialEventQueue = 
            getMessageQueueFactory().newMessageQueue(qosSettings_);

        pendingMessages_ = new RWLockEventQueueDecorator(initialEventQueue);

        pendingMessages_.addDiscardListener(discardListener_);

        eventTypes_.add(EVENT_MESSAGE_DISCARDED);
    }

    // //////////////////////////////////////

    protected EventQueueFactory getMessageQueueFactory()
    {
        return eventQueueFactory_;
    }

    /**
     * configure pending messages queue. the queue is reconfigured according to the current QoS
     * Settings. the contents of the queue are reorganized according to the new OrderPolicy.
     */
    private final void configureEventQueue()
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

    private PropertySetAdapter eventQueueConfigurationChangedCB = new PropertySetAdapter()
    {
        public void actionPropertySetChanged(PropertySet source)
        {
            configureEventQueue();
        }
    };

    /**
     * @jmx.managed-attribute description = "Number of Pending Messages"
     *                        access = "read-only"
     */
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

    /**
     * @jmx.managed-attribute description = "current OrderPolicy"
     *                        access = "read-only"
     */
    public final String getOrderPolicy()
    {
        return pendingMessages_.getOrderPolicyName();
    }

    /**
     * @jmx.managed-attribute description = "current DiscardPolicy"
     *                        access = "read-only"
     */
    public final String getDiscardPolicy()
    {
        return pendingMessages_.getDiscardPolicyName();
    }

    /**
     * @jmx.managed-attribute description = "maximum number of events that may be queued per consumer"
     *                        access = "read-write"
     */
    public final int getMaxEventsPerConsumer()
    {
        return qosSettings_.get(MaxEventsPerConsumer.value).extract_long();
    }

    /**
     * @jmx.managed-attribute access = "read-write"
     */
    public void setMaxEventsPerConsumer(int max)
    {
        Any any = getORB().create_any();
        any.insert_long(max);
        Property prop = new Property(MaxEventsPerConsumer.value, any);
        qosSettings_.set_qos(new Property[] { prop });
    }

    /**
     * @jmx.managed-attribute access = "read-only"
     */
    public int getNumberOfDiscardedMessages()
    {
        return numberOfDiscardedMessages_;
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
        Message _copy = (Message) message.clone();

        try
        {
            pendingMessages_.enqeue(_copy);

            if (logger_.isDebugEnabled())
            {
                logger_.debug("enqueue " + message + " to pending Messages.");
            }
        } catch (InterruptedException e)
        {
            _copy.dispose();
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

    public void queueMessage(final Message message)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("queueMessage() connected=" + getConnected() + " suspended="
                    + isSuspended());
        }

        if (getConnected())
        {
            enqueue(message);

            messageQueued();
        }
    }

    /**
     * this is an extension point.
     */
    protected void messageQueued()
    {
        // no operation
    }

    /**
     * @param max maximum number of messages
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

        pendingMessages_.clear();

        // insert an empty command into the taskProcessor's queue.
        // otherwise queue seems to contain old entries that prevent GC'ing
        getTaskProcessor().executeTaskAfterDelay(1000, EMPTY_RUNNABLE);
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

    protected final void clientDisconnected()
    {
        offerListener_ = null;
    }
    
    public void connectClient(org.omg.CORBA.Object client)
    {
        super.connectClient(client);

        try
        {
            offerListener_ = NotifyPublishHelper.narrow(client);

            logger_.debug("successfully narrowed connecting Client to IF NotifyPublish");
        } catch (Exception t)
        {
            logger_.info("disable offer_change for connecting Consumer");
        }
    }

    public boolean isRetryAllowed()
    {
        return !isDestroyed() && getErrorCounter() < getErrorThreshold();
    }

    protected abstract long getCost();

    public int compareTo(Object o)
    {
        AbstractProxySupplier other = (AbstractProxySupplier) o;

        return (int) (getCost() - other.getCost());
    }

    public final boolean hasMessageConsumer()
    {
        return true;
    }
    
    public final List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList(this);
    }
    
    public final MessageConsumer getMessageConsumer()
    {
        return this;
    }
    
    /**
     * @jmx.managed-operation   impact = "ACTION"
     *                          description = "delete all queued Messages"
     */
    public void clearPendingMessageQueue()
    {
        pendingMessages_.clear();
    }
}