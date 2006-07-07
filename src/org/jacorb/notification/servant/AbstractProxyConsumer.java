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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.EventTypeWrapper;
import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.PropertySetAdapter;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.StartTimeSupported;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribe;
import org.omg.CosNotifyComm.NotifySubscribeHelper;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.PortableServer.POA;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * @jmx.mbean extends = "AbstractProxyMBean"
 * @jboss.xmbean
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxyConsumer extends AbstractProxy implements IProxyConsumer,
        NotifyPublishOperations, AbstractProxyConsumerMBean
{
    private final static EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    // //////////////////////////////////////

    private final MessageFactory messageFactory_;

    // TODO check StartTime, StopTime, TimeOut: naming and usage is inconsistent.
    private final AtomicBoolean isStartTimeSupported_ = new AtomicBoolean(true);

    private final AtomicBoolean isStopTimeSupported_ = new AtomicBoolean(true);

    private List subsequentDestinations_;

    private NotifySubscribeOperations proxySubscriptionListener_;

    private NotifySubscribe subscriptionListener_;

    protected final SupplierAdmin supplierAdmin_;

    private int messageCounter_ = 0;

    // //////////////////////////////////////

    protected AbstractProxyConsumer(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, MessageFactory messageFactory,
            SupplierAdmin supplierAdmin, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        super(admin, orb, poa, conf, taskProcessor, offerManager, subscriptionManager);

        supplierAdmin_ = supplierAdmin;
        messageFactory_ = messageFactory;

        configureStartTimeSupported();

        configureStopTimeSupported();

        qosSettings_.addPropertySetListener(new String[] { Priority.value, Timeout.value,
                StartTimeSupported.value, StopTimeSupported.value }, reconfigureQoS_);
    }

    protected MessageFactory getMessageFactory()
    {
        return messageFactory_;
    }

    public final List getSubsequentFilterStages()
    {
        return subsequentDestinations_;
    }

    public void setSubsequentDestinations(List list)
    {
        subsequentDestinations_ = list;
    }

    private PropertySetAdapter reconfigureQoS_ = new PropertySetAdapter()
    {
        public void actionPropertySetChanged(PropertySet source)
        {
            configureStartTimeSupported();

            configureStopTimeSupported();
        }
    };

    private void configureStartTimeSupported()
    {
        try
        {
            isStartTimeSupported_.set(qosSettings_.get(StartTimeSupported.value).extract_boolean());
        } catch (Exception e)
        {
            isStartTimeSupported_.set(Default.DEFAULT_START_TIME_SUPPORTED.equals("on"));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StartTimeSupported=" + isStartTimeSupported_);
        }
    }

    private void configureStopTimeSupported()
    {
        logger_.debug("QoSSettings: " + qosSettings_);
        try
        {
            isStopTimeSupported_.set(qosSettings_.get(StopTimeSupported.value).extract_boolean());
        } catch (Exception e)
        {
            isStopTimeSupported_.set(Default.DEFAULT_STOP_TIME_SUPPORTED.equals("on"));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StopTimeSupported=" + isStopTimeSupported_);
        }
    }

    protected void schedulePullTask(MessageSupplier target)
    {
        getTaskProcessor().scheduleTimedPullTask(target);
    }

    /**
     * check if a Message is acceptable to the QoS Settings of this ProxyConsumer
     */
    protected void checkMessageProperties(Message m)
    {
        // No Op
        // TODO implement
    }

    public FilterStage getFirstStage()
    {
        return this;
    }

    /**
     * @jmx.managed-attribute description = "Does this ProxyConsumer support the per Message Option TimeOut"
     *                        access = "read-only"
     */
    public boolean getStopTimeSupported()
    {
        return isStopTimeSupported_.get();
    }

    /**
     * @jmx.managed-attribute description = "Does this ProxyConsumer support the per Message Option StartTime"
     *                        access = "read-only"
     */
    public boolean getStartTimeSupported()
    {
        return isStartTimeSupported_.get();
    }

    public final SupplierAdmin MyAdmin()
    {
        return supplierAdmin_;
    }

    public final MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }

    public final boolean hasMessageConsumer()
    {
        return false;
    }

    public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
    {
        offerManager_.offer_change(added, removed);
    }

    public final EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode)
    {
        final EventType[] _subscriptionTypes;

        switch (obtainInfoMode.value()) {
        case ObtainInfoMode._ALL_NOW_UPDATES_ON:
            // attach the listener first, then return the current
            // subscription types. order is important so that no
            // updates are lost.

            registerListener();

            _subscriptionTypes = subscriptionManager_.obtain_subscription_types();
            break;
        case ObtainInfoMode._ALL_NOW_UPDATES_OFF:
            _subscriptionTypes = subscriptionManager_.obtain_subscription_types();

            removeListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_ON:
            _subscriptionTypes = EMPTY_EVENT_TYPE_ARRAY;

            registerListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_OFF:
            _subscriptionTypes = EMPTY_EVENT_TYPE_ARRAY;

            removeListener();
            break;
        default:
            throw new IllegalArgumentException("Illegal ObtainInfoMode: ObtainInfoMode."
                    + obtainInfoMode.value());
        }

        return _subscriptionTypes;
    }

    private void registerListener()
    {
        if (proxySubscriptionListener_ == null)
        {
            final NotifySubscribeOperations _listener = getSubscriptionListener();

            if (_listener != null)
            {
                proxySubscriptionListener_ = new NotifySubscribeOperations()
                {
                    public void subscription_change(EventType[] added, EventType[] removed)
                    {
                        try
                        {
                            _listener.subscription_change(added, removed);
                        } catch (NO_IMPLEMENT e)
                        {
                            logger_.info("disable subscription_change for Supplier", e);

                            removeListener();
                        } catch (InvalidEventType e)
                        {
                            if (logger_.isDebugEnabled())
                            {
                                logger_.debug("subscription_change("
                                        + EventTypeWrapper.toString(added) + ", "
                                        + EventTypeWrapper.toString(removed) + ") failed", e);
                            }
                            else
                            {
                                logger_.error("invalid event type", e);
                            }
                        } catch (Exception e)
                        {
                            logger_.error("subscription change failed", e);
                        }
                    }
                };
                subscriptionManager_.addListener(proxySubscriptionListener_);
            }
        }
    }

    /**
     * removes the listener. subscription_change will no more be issued to the connected Supplier
     */
    protected void removeListener()
    {
        if (proxySubscriptionListener_ != null)
        {
            subscriptionManager_.removeListener(proxySubscriptionListener_);

            proxySubscriptionListener_ = null;
        }
    }

    protected final void clientDisconnected()
    {
        subscriptionListener_ = null;
    }

    protected void connectClient(org.omg.CORBA.Object client)
    {
        super.connectClient(client);

        try
        {
            subscriptionListener_ = NotifySubscribeHelper.narrow(client);

            logger_.debug("successfully narrowed connecting Supplier to NotifySubscribe");
        } catch (Exception e)
        {
            logger_.info("connecting Supplier does not support subscription_change");
        }
    }

    final NotifySubscribeOperations getSubscriptionListener()
    {
        return subscriptionListener_;
    }

    protected void processMessage(Message mesg)
    {
        getTaskProcessor().processMessage(mesg);

        messageCounter_++;
    }

    /**
     * @jmx.managed-attribute description = "Total number of Messages received by this ProxyConsumer"
     *                        access = "read-only"
     */
    public final int getMessageCount()
    {
        return messageCounter_;
    }

    protected Message[] newMessages(StructuredEvent[] events)
    {
        final List _result = new ArrayList(events.length);
        final MessageFactory _messageFactory = getMessageFactory();

        for (int i = 0; i < events.length; ++i)
        {
            final Message _newMessage = _messageFactory.newMessage(events[i], this);
            checkMessageProperties(_newMessage);
            _result.add(_newMessage);
        }

        return (Message[]) _result.toArray(new Message[_result.size()]);
    }
}