package org.jacorb.notification.util;

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

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CosNotification.AnyOrder;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.DeadlineOrder;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.EventReliability;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.LifoOrder;
import org.omg.CosNotification.MaxEventsPerConsumer;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.PacingInterval;
import org.omg.CosNotification.Persistent;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.PriorityOrder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.QoSError_code;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StartTimeSupported;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StopTimeSupported;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.TimeBase.TimeTHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jacorb.notification.conf.Configuration;
import org.jacorb.notification.conf.Default;
import org.jacorb.util.Environment;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class QoSPropertySet extends PropertySet
{
    public static final int CHANNEL_QOS = 0;
    public static final int ADMIN_QOS = 1;
    public static final int PROXY_QOS = 2;

    private static final Property[] sDefaultChannelQoS_;
    private static final Property[] sDefaultAdminQoS_;

    private static final HashSet sValidChannelQoSNames_;
    private static final HashSet sValidAdminQoSNames_;
    private static final HashSet sValidProxyQoSNames_;
    private static final HashSet sValidMessageQoSNames_;

    static final Any connectionReliabilityLow_;
    static final Any connectionReliabilityHigh_;

    static final Any eventReliabilityLow_;
    static final Any eventReliabilityHigh_;

    static Any orderPolicyLow_;
    static Any orderPolicyDefault_;
    static Any orderPolicyHigh_;

    static Any discardPolicyLow_;
    static Any discardPolicyHigh_;

    static Any priorityLow_;
    static Any priorityDefault_;
    static Any priorityHigh_;

    static Any maxEventsPerConsumerLow_;
    static Any maxEventsPerConsumerDefault_;
    static Any maxEventsPerConsumerHigh_;

    static Any timeoutHigh_;
    static Any timeoutDefault_;
    static Any timeoutLow_;

    private static final Any trueAny;
    private static final Any falseAny;

    ////////////////////////////////////////

    static {
        trueAny = sORB.create_any();
        falseAny = sORB.create_any();
        trueAny.insert_boolean(true);
        falseAny.insert_boolean(false);

        //////////////////////////////

        sValidChannelQoSNames_ = new HashSet();
        sValidChannelQoSNames_.add(EventReliability.value);
        sValidChannelQoSNames_.add(ConnectionReliability.value);
        sValidChannelQoSNames_.add(Priority.value);
        sValidChannelQoSNames_.add(Timeout.value);
        sValidChannelQoSNames_.add(StartTimeSupported.value);
        sValidChannelQoSNames_.add(StopTimeSupported.value);
        sValidChannelQoSNames_.add(MaxEventsPerConsumer.value);
        sValidChannelQoSNames_.add(OrderPolicy.value);
        sValidChannelQoSNames_.add(DiscardPolicy.value);
        sValidChannelQoSNames_.add(MaximumBatchSize.value);
        sValidChannelQoSNames_.add(PacingInterval.value);

        ////////////////////

        sValidAdminQoSNames_ = new HashSet(sValidChannelQoSNames_);
        sValidAdminQoSNames_.remove(EventReliability.value);

        ////////////////////

        sValidProxyQoSNames_ = new HashSet(sValidAdminQoSNames_);

        sValidMessageQoSNames_ = new HashSet();
        sValidMessageQoSNames_.add(EventReliability.value);
        sValidMessageQoSNames_.add(Priority.value);
        sValidMessageQoSNames_.add(StartTime.value);
        sValidMessageQoSNames_.add(StopTime.value);
        sValidMessageQoSNames_.add(Timeout.value);

        ////////////////////

        connectionReliabilityHigh_ = sORB.create_any();
        connectionReliabilityHigh_.insert_short(Persistent.value);

        connectionReliabilityLow_ = sORB.create_any();
        connectionReliabilityLow_.insert_short(BestEffort.value);

        ////////////////////

        eventReliabilityLow_ = sORB.create_any();
        eventReliabilityLow_.insert_short(BestEffort.value);

        eventReliabilityHigh_ = sORB.create_any();
        eventReliabilityHigh_.insert_short(BestEffort.value);

        ////////////////////

        orderPolicyLow_ = sORB.create_any();
        orderPolicyLow_.insert_short(AnyOrder.value);

        orderPolicyHigh_ = sORB.create_any();
        orderPolicyHigh_.insert_short(DeadlineOrder.value);

        ////////////////////

        discardPolicyLow_ = sORB.create_any();
        discardPolicyLow_.insert_short(AnyOrder.value);

        discardPolicyHigh_ = sORB.create_any();
        discardPolicyHigh_.insert_short(DeadlineOrder.value);

        ////////////////////

        priorityLow_ = sORB.create_any();
        priorityLow_.insert_short(Short.MIN_VALUE);

        priorityDefault_ = sORB.create_any();
        priorityDefault_.insert_short((short)0);

        priorityHigh_ = sORB.create_any();
        priorityHigh_.insert_short(Short.MAX_VALUE);

        ////////////////////

        int _maxEventsPerConsumerDefault =
            Environment.getIntPropertyWithDefault( Configuration.MAX_EVENTS_PER_CONSUMER,
                                                   Default.DEFAULT_MAX_EVENTS_PER_CONSUMER );

        maxEventsPerConsumerDefault_ = sORB.create_any();
        maxEventsPerConsumerDefault_.insert_long(_maxEventsPerConsumerDefault);

        maxEventsPerConsumerHigh_ = sORB.create_any();
        maxEventsPerConsumerLow_ = sORB.create_any();

        maxEventsPerConsumerLow_.insert_long(0);
        maxEventsPerConsumerHigh_.insert_long(Integer.MAX_VALUE);

        ////////////////////

        String _orderPolicy = Environment.getProperty( Configuration.ORDER_POLICY,
                                                       Default.DEFAULT_ORDER_POLICY );

        String _discardPolicy = Environment.getProperty( Configuration.DISCARD_POLICY,
                                                         Default.DEFAULT_DISCARD_POLICY );

        ////////////////////

        timeoutDefault_ = sORB.create_any();
        TimeTHelper.insert(timeoutDefault_, 0);

        ////////////////////

        Any _isStartTimeSupportedDefault = sORB.create_any();

        boolean _isStartTimeSupported = Environment.isPropertyOn(Configuration.START_TIME_SUPPORTED,
                                                                Default.DEFAULT_START_TIME_SUPPORTED);

        _isStartTimeSupportedDefault.insert_boolean(_isStartTimeSupported);

        ////////////////////

        Any _isStopTimeSupportedDefault = sORB.create_any();

        boolean _isStopTimeSupported = Environment.isPropertyOn(Configuration.STOP_TIME_SUPPORTED,
                                                                Default.DEFAULT_STOP_TIME_SUPPORTED);

        _isStopTimeSupportedDefault.insert_boolean(_isStopTimeSupported);

        ////////////////////

        int _maxBatchSize = Environment.getIntPropertyWithDefault(Configuration.MAX_BATCH_SIZE,
                                                                  Default.DEFAULT_MAX_BATCH_SIZE);

        Any _maxBatchSizeDefault = sORB.create_any();
        _maxBatchSizeDefault.insert_long(_maxBatchSize);

        ////////////////////

        sDefaultChannelQoS_ = new Property[] {
            new Property(EventReliability.value, eventReliabilityLow_),
            new Property(ConnectionReliability.value, connectionReliabilityLow_),

            new Property(Priority.value, priorityDefault_),
            new Property(MaxEventsPerConsumer.value, maxEventsPerConsumerDefault_),
            new Property(Timeout.value, timeoutDefault_),
            new Property(StartTimeSupported.value, _isStartTimeSupportedDefault),
            new Property(StopTimeSupported.value, _isStartTimeSupportedDefault),
            new Property(MaximumBatchSize.value, _maxBatchSizeDefault)
        };


        sDefaultAdminQoS_ = new Property[] {
            new Property(ConnectionReliability.value, connectionReliabilityLow_),

            new Property(Priority.value, priorityDefault_),
            new Property(MaxEventsPerConsumer.value, maxEventsPerConsumerDefault_),
            new Property(Timeout.value, timeoutDefault_),
            new Property(StartTimeSupported.value, _isStartTimeSupportedDefault),
            new Property(StopTimeSupported.value, _isStartTimeSupportedDefault),
            new Property(MaximumBatchSize.value, _maxBatchSizeDefault)
        };

    }

    ////////////////////////////////////////

    private HashSet validNames_;

    ////////////////////////////////////////

    public QoSPropertySet(int type)
    {
        super();

        init(type);
    }


    private void init(int type)
    {
        switch (type)
            {
            case CHANNEL_QOS:
                validNames_ = sValidChannelQoSNames_;

                set_qos(sDefaultChannelQoS_);
                break;
            case ADMIN_QOS:
                validNames_ = sValidAdminQoSNames_;

                set_qos(sDefaultAdminQoS_);
                break;
            case PROXY_QOS:
                validNames_ = sValidProxyQoSNames_;
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " is invalid");
            }
    }

    ////////////////////////////////////////

    HashSet getValidNames()
    {
        return validNames_;
    }


    public void set_qos(Property[] ps)
    {
        set_properties(ps);
    }


    public Property[] get_qos()
    {
        return toArray();
    }


    public void validate_qos(Property[] props,
                             NamedPropertyRangeSeqHolder namedPropertyRange)
        throws UnsupportedQoS
    {
        logger_.info("validate_qos");

        List _errors = new ArrayList();

        checkPropertyExistence(props, _errors);

        if (!_errors.isEmpty()) {
            throw new UnsupportedQoS((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
        }

        checkPropertyValues(props, _errors);

        if (!_errors.isEmpty()) {
            throw new UnsupportedQoS((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
        }


    }


    private short checkIsShort(String name, Any value, List errors) throws BAD_OPERATION {
        try {
            return value.extract_short();
        } catch (BAD_OPERATION e) {
            errors.add(badType(name));

            throw e;
        }
    }


    private void logError(List errors,
                          QoSError_code error_code,
                          String name,
                          Any value,
                          Any high,
                          Any low) {

        if (logger_.isErrorEnabled()) {
            logger_.error("wrong value for Property '" +name + "': " + value);
        }

        errors.add(new PropertyError(error_code,
                                     name,
                                     new PropertyRange(high,
                                                       low)));
    }


    private void checkPropertyValues(Property[] ps, List errors)
    {
        for (int x = 0; x < ps.length; ++x) {
            String _propertyName = ps[x].name;
            Any _value = ps[x].value;

            try {
                if (ConnectionReliability.value.equals(_propertyName)) {

                    short _connectionReliability =
                        checkIsShort(_propertyName, _value, errors);

                    switch (_connectionReliability)
                        {
                        case BestEffort.value:
                            // fallthrough
                        case Persistent.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _propertyName,
                                     _value,
                                     connectionReliabilityLow_,
                                     connectionReliabilityHigh_ );
                        }
                } else if (EventReliability.value.equals(_propertyName)) {

                    short _eReliability = checkIsShort(_propertyName, _value, errors);

                    switch (_eReliability)
                        {
                        case BestEffort.value:
                            // fallthrough
                        case Persistent.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _propertyName,
                                     _value,
                                     eventReliabilityLow_,
                                     eventReliabilityHigh_);
                        }
                } else if (OrderPolicy.value.equals(_propertyName)) {

                    short _oPolicy = checkIsShort(_propertyName, _value, errors);

                    switch (ps[x].value.extract_short())
                        {
                        case AnyOrder.value:
                            break;
                        case FifoOrder.value:
                            break;
                        case PriorityOrder.value:
                            break;
                        case DeadlineOrder.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _propertyName,
                                     _value,
                                     orderPolicyLow_,
                                     orderPolicyHigh_);
                        }
                } else if (DiscardPolicy.value.equals(_propertyName)) {
                    short _dPolicy = checkIsShort(_propertyName, _value, errors);

                    switch (_dPolicy)
                        {
                        case AnyOrder.value:
                            break;
                        case FifoOrder.value:
                            break;
                        case LifoOrder.value:
                            break;
                        case PriorityOrder.value:
                            break;
                        case DeadlineOrder.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _propertyName,
                                     _value,
                                     discardPolicyLow_,
                                     discardPolicyHigh_);
                        }
                }
            } catch (BAD_OPERATION e) {
                // Nothing to do. a error has already been added to
                // List 'errors'.
            }
        }
    }
}

