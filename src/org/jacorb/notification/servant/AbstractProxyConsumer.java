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
import org.jacorb.notification.CollectionsWrapper;
import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.interfaces.MessageConsumer;
import org.jacorb.notification.interfaces.TimerEventSupplier;
import org.jacorb.util.Environment;

import org.omg.CORBA.BAD_QOS;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTimeSupported;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.NotifySubscribeOperations;

import java.util.List;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

abstract class AbstractProxyConsumer
    extends AbstractProxy
    implements AbstractProxyConsumerI,
               NotifyPublishOperations
{
    private final static EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];

    private TaskProcessor taskProcessor_;

    private SynchronizedBoolean isStartTimeSupported_ = new SynchronizedBoolean(true);

    private SynchronizedBoolean isStopTimeSupported_ = new SynchronizedBoolean(true);

    private List subsequentDestinations_;

    private NotifySubscribeOperations subscriptionListener_;

    ////////////////////////////////////////

    AbstractProxyConsumer( AbstractAdmin adminServant,
                           ChannelContext channelContext)
    {

        super( adminServant,
               channelContext);

        subsequentDestinations_ = CollectionsWrapper.singletonList(myAdmin_);

        taskProcessor_ = channelContext.getTaskProcessor();
    }

    ////////////////////////////////////////

    public final List getSubsequentFilterStages() {
        return subsequentDestinations_;
    }


    public void preActivate()
    {
        logger_.debug("AbstractProxyConsumer.initialize");

        configureStartTimeSupported();

        configureStopTimeSupported();

        qosSettings_.addPropertySetListener(new String[] {Priority.value,
                                                          Timeout.value,
                                                          StartTimeSupported.value,
                                                          StopTimeSupported.value},
                                            reconfigureCB);
    }


    private PropertySetListener reconfigureCB =
        new PropertySetListener()
        {
            public void validateProperty(Property[] props, List errors)
            {}
            public void actionPropertySetChanged(PropertySet source)
            {
                configureStartTimeSupported();
                configureStopTimeSupported();
            }
        };


    private void configureStartTimeSupported()
    {
        if (qosSettings_.containsKey(StartTimeSupported.value))
        {
            isStartTimeSupported_.set(qosSettings_.get(StartTimeSupported.value).extract_boolean());
        }
        else
        {
            isStartTimeSupported_.set(Environment.isPropertyOn(Configuration.START_TIME_SUPPORTED,
                                      Default.DEFAULT_START_TIME_SUPPORTED));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StartTimeSupported=" + isStartTimeSupported_);
        }
    }


    private void configureStopTimeSupported()
    {
        if (qosSettings_.containsKey(StopTimeSupported.value))
        {
            isStopTimeSupported_.set(qosSettings_.get(StopTimeSupported.value).extract_boolean());
        }
        else
        {
            isStopTimeSupported_.set(Environment.isPropertyOn(Configuration.STOP_TIME_SUPPORTED,
                                     Default.DEFAULT_STOP_TIME_SUPPORTED));
        }

        if (logger_.isInfoEnabled())
        {
            logger_.info("set QoS: StopTimeSupported=" + isStopTimeSupported_);
        }
    }


    public void scheduleTimedPullTask( TimerEventSupplier target )
    {
        try
        {
            taskProcessor_.scheduleTimedPullTask(target);
        }
        catch (InterruptedException e)
        {
            logger_.fatalError("interrupted", e);
        }
    }


    protected void checkMessageProperties(Message mesg)
    {
        if (mesg.hasStartTime() && !isStartTimeSupported_.get() )
        {
            logger_.error("StartTime NOT allowed");

            throw new BAD_QOS("property StartTime is not allowed");
        }

        if (mesg.hasStopTime() && !isStopTimeSupported_.get() )
        {
            logger_.error("StopTime NOT allowed");

            throw new BAD_QOS("property StopTime is not allowed");
        }
    }


    public FilterStage getFirstStage()
    {
        return this;
    }


    public boolean isTimeOutSupported()
    {
        return isStopTimeSupported_.get();
    }


    public boolean isStartTimeSupported()
    {
        return isStartTimeSupported_.get();
    }


    public final SupplierAdmin MyAdmin()
    {
        return ( SupplierAdmin ) myAdmin_.activate();
    }


    public final MessageConsumer getMessageConsumer()
    {
        throw new UnsupportedOperationException();
    }


    public final boolean hasMessageConsumer()
    {
        return false;
    }


    public void offer_change(EventType[] added,
                             EventType[] removed)
        throws InvalidEventType
    {
        offerManager_.offer_change(added, removed);
    }


    public EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode)
    {
        EventType[] _subscriptionTypes = EMPTY_EVENT_TYPE_ARRAY;

        switch(obtainInfoMode.value()) {
        case ObtainInfoMode._ALL_NOW_UPDATES_ON:
            registerListener();
            _subscriptionTypes = subscriptionManager_.obtain_subscription_types();
            break;
        case ObtainInfoMode._ALL_NOW_UPDATES_OFF:
            _subscriptionTypes = subscriptionManager_.obtain_subscription_types();
            removeListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_ON:
            registerListener();
            break;
        case ObtainInfoMode._NONE_NOW_UPDATES_OFF:
            removeListener();
            break;
        default:
            throw new IllegalArgumentException("Illegal ObtainInfoMode " + obtainInfoMode.value() );
        }

        return _subscriptionTypes;
    }


    private void registerListener() {
        if (subscriptionListener_ == null) {
            final NotifySubscribeOperations _listener = getSubscriptionListener();

            if (_listener != null) {

                subscriptionListener_ = new NotifySubscribeOperations() {
                        public void subscription_change(EventType[] added, EventType[] removed) {
                            try {
                                _listener.subscription_change(added, removed);
                            } catch (NO_IMPLEMENT e) {
                                logger_.info("Listener does not support subscription_change. remove it.", e);
                                removeListener();
                            } catch (InvalidEventType e) {
                                logger_.error("invalid event type", e);
                            }
                        }
                    };

                subscriptionManager_.addListener(subscriptionListener_);
            }
        }
    }


    private void removeListener() {
        if (subscriptionListener_ != null) {
            subscriptionManager_.removeListener(subscriptionListener_);
            subscriptionListener_ = null;
        }
    }


    abstract NotifySubscribeOperations getSubscriptionListener();
}

