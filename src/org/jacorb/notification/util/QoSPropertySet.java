package org.jacorb.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INTERNAL;
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

/**
 * @author Alphonse Bendt
 */

public class QoSPropertySet extends PropertySet
{
    public static final int CHANNEL_QOS = 0;
    public static final int ADMIN_QOS = 1;
    public static final int PROXY_QOS = 2;

    private Property[] defaultChannelQoS_;
    private Property[] defaultAdminQoS_;

    private static final Set<String> sValidChannelQoSNames_;
    private static final Set<String> sValidAdminQoSNames_;
    private static final Set<String> sValidProxyQoSNames_;

    private static final Any connectionReliabilityLow_;
    private static final Any connectionReliabilityHigh_;

    private static final Any eventReliabilityLow_;
    private static final Any eventReliabilityHigh_;

    private static Any orderPolicyLow_;
    //private Any orderPolicyDefault_;
    private static Any orderPolicyHigh_;

    private static Any discardPolicyLow_;
    private static Any discardPolicyHigh_;

    private static Any priorityLow_;
    private static Any priorityDefault_;
    private static Any priorityHigh_;

    private Any maxEventsPerConsumerLow_;
    private Any maxEventsPerConsumerDefault_;
    private Any maxEventsPerConsumerHigh_;

    //private Any timeoutHigh_;
    private Any timeoutDefault_;
    //private Any timeoutLow_;

    private static final Any trueAny;
    private static final Any falseAny;

    ////////////////////////////////////////

    static {
        trueAny = sORB.create_any();
        falseAny = sORB.create_any();
        trueAny.insert_boolean(true);
        falseAny.insert_boolean(false);

        //////////////////////////////

        HashSet<String> _validChannelQoS = new HashSet<String>();
        _validChannelQoS.add(EventReliability.value);
        _validChannelQoS.add(ConnectionReliability.value);
        _validChannelQoS.add(Priority.value);
        _validChannelQoS.add(Timeout.value);
        _validChannelQoS.add(StartTimeSupported.value);
        _validChannelQoS.add(StopTimeSupported.value);
        _validChannelQoS.add(MaxEventsPerConsumer.value);
        _validChannelQoS.add(OrderPolicy.value);
        _validChannelQoS.add(DiscardPolicy.value);
        _validChannelQoS.add(MaximumBatchSize.value);
        _validChannelQoS.add(PacingInterval.value);

        sValidChannelQoSNames_ = Collections.unmodifiableSet(_validChannelQoS);

        ////////////////////

        HashSet<String> _adminNames = new HashSet<String>(sValidChannelQoSNames_);
        _adminNames.remove(EventReliability.value);
        sValidAdminQoSNames_ = Collections.unmodifiableSet(_adminNames);

        ////////////////////

        sValidProxyQoSNames_ = sValidAdminQoSNames_;

        ////////////////////

        HashSet<String> _validMessageQoS = new HashSet<String>();
        _validMessageQoS.add(EventReliability.value);
        _validMessageQoS.add(Priority.value);
        _validMessageQoS.add(StartTime.value);
        _validMessageQoS.add(StopTime.value);
        _validMessageQoS.add(Timeout.value);

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
    }


    private void configure (Configuration conf)
    {
       int _maxEventsPerConsumerDefault;
       int _maxBatchSize;

       try
       {
        _maxEventsPerConsumerDefault =
            conf.getAttributeAsInteger( Attributes.MAX_EVENTS_PER_CONSUMER,
                                        Default.DEFAULT_MAX_EVENTS_PER_CONSUMER );
        _maxBatchSize =
           conf.getAttributeAsInteger(Attributes.MAX_BATCH_SIZE,
                                      Default.DEFAULT_MAX_BATCH_SIZE);
       }
       catch (ConfigurationException ex)
       {
          logger_.error ("Error configuring QoSPropertySet ", ex);
          throw new INTERNAL ("Error configuring QoSPropertySet " + ex);
       }


        maxEventsPerConsumerDefault_ = sORB.create_any();
        maxEventsPerConsumerDefault_.insert_long(_maxEventsPerConsumerDefault);

        maxEventsPerConsumerHigh_ = sORB.create_any();
        maxEventsPerConsumerLow_ = sORB.create_any();

        maxEventsPerConsumerLow_.insert_long(0);
        maxEventsPerConsumerHigh_.insert_long(Integer.MAX_VALUE);

        ////////////////////

        timeoutDefault_ = sORB.create_any();
        TimeTHelper.insert(timeoutDefault_, 0);

        ////////////////////

        Any _isStartTimeSupportedDefault = sORB.create_any();

        boolean _isStartTimeSupported =
            conf.getAttribute(Attributes.START_TIME_SUPPORTED,
                              Default.DEFAULT_START_TIME_SUPPORTED).
            equalsIgnoreCase("on");

        _isStartTimeSupportedDefault.insert_boolean(_isStartTimeSupported);

        ////////////////////

        Any _isStopTimeSupportedDefault = sORB.create_any();

        boolean _isStopTimeSupported =
            conf.getAttribute(Attributes.STOP_TIME_SUPPORTED,
                              Default.DEFAULT_STOP_TIME_SUPPORTED).
            equalsIgnoreCase("on");
        _isStopTimeSupportedDefault.insert_boolean(_isStopTimeSupported);

        ////////////////////

        Any _maxBatchSizeDefault = sORB.create_any();
        _maxBatchSizeDefault.insert_long(_maxBatchSize);

        ////////////////////

        defaultChannelQoS_ = new Property[] {
            new Property(EventReliability.value, eventReliabilityLow_),
            new Property(ConnectionReliability.value,
                         connectionReliabilityLow_),
            new Property(Priority.value, priorityDefault_),
            new Property(MaxEventsPerConsumer.value,
                         maxEventsPerConsumerDefault_),
            new Property(Timeout.value, timeoutDefault_),
            new Property(StartTimeSupported.value,
                         _isStartTimeSupportedDefault),
            new Property(StopTimeSupported.value,
                         _isStartTimeSupportedDefault),
            new Property(MaximumBatchSize.value,
                         _maxBatchSizeDefault)
        };

        defaultAdminQoS_ = new Property[] {
            new Property(ConnectionReliability.value,
                         connectionReliabilityLow_),
            new Property(Priority.value, priorityDefault_),
            new Property(MaxEventsPerConsumer.value,
                         maxEventsPerConsumerDefault_),
            new Property(Timeout.value, timeoutDefault_),
            new Property(StartTimeSupported.value,
                         _isStartTimeSupportedDefault),
            new Property(StopTimeSupported.value,
                         _isStartTimeSupportedDefault),
            new Property(MaximumBatchSize.value, _maxBatchSizeDefault)
        };
    }

    ////////////////////////////////////////

    private final Set<String> validNames_;

    ////////////////////////////////////////

    public QoSPropertySet(Configuration configuration, int type)
    {
        super();

        configure(configuration);

        switch (type)
            {
            case CHANNEL_QOS:
                validNames_ = sValidChannelQoSNames_;

                set_qos(defaultChannelQoS_);
                break;
            case ADMIN_QOS:
                validNames_ = sValidAdminQoSNames_;

                set_qos(defaultAdminQoS_);
                break;
            case PROXY_QOS:
                validNames_ = sValidProxyQoSNames_;
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " is invalid");
            }
    }

    ////////////////////////////////////////

    protected Set<String> getValidNames()
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
        List<PropertyError> _errors = new ArrayList<PropertyError>();

        checkPropertyExistence(props, _errors);

        if (!_errors.isEmpty()) {
            throw new UnsupportedQoS((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
        }

        checkPropertyValues(props, _errors);

        if (!_errors.isEmpty()) {
            throw new UnsupportedQoS((PropertyError[])_errors.toArray(PROPERTY_ERROR_ARRAY_TEMPLATE));
        }
    }


    private short checkIsShort(String name, Any value, List<PropertyError> errors) throws BAD_OPERATION {
        try {
            return value.extract_short();
        } catch (BAD_OPERATION e) {
            errors.add(badType(name));

            throw e;
        }
    }


    private void logError(List<PropertyError> errors,
                          QoSError_code error_code,
                          String name,
                          Any value,
                          Any high,
                          Any low) {

         if (logger_.isInfoEnabled()) {
             logger_.info("wrong value for Property '" +name + "': " + value);
         }

        errors.add(new PropertyError(error_code,
                                     name,
                                     new PropertyRange(high,
                                                       low)));
    }


    private void checkPropertyValues(Property[] props, List<PropertyError> errors)
    {
        for (int x = 0; x < props.length; ++x) {
            final String _name = props[x].name;
            final Any _value = props[x].value;

            try {
                if (ConnectionReliability.value.equals(_name)) {

                    final short _connectionReliability =
                        checkIsShort(_name, _value, errors);

                    switch (_connectionReliability)
                        {
                        case BestEffort.value:
                            // fallthrough
                        case Persistent.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _name,
                                     _value,
                                     connectionReliabilityLow_,
                                     connectionReliabilityHigh_ );
                        }
                } else if (EventReliability.value.equals(_name)) {

                    final short _eventReliability = checkIsShort(_name, _value, errors);

                    switch (_eventReliability)
                        {
                        case BestEffort.value:
                            // fallthrough
                        case Persistent.value:
                            break;
                        default:
                            logError(errors,
                                     QoSError_code.BAD_VALUE,
                                     _name,
                                     _value,
                                     eventReliabilityLow_,
                                     eventReliabilityHigh_);
                        }
                } else if (OrderPolicy.value.equals(_name)) {

                    final short _orderPolicy = checkIsShort(_name, _value, errors);

                    switch (_orderPolicy)
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
                                     _name,
                                     _value,
                                     orderPolicyLow_,
                                     orderPolicyHigh_);
                        }
                } else if (DiscardPolicy.value.equals(_name)) {
                    final short _discardPolicy = checkIsShort(_name, _value, errors);

                    switch (_discardPolicy)
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
                                     _name,
                                     _value,
                                     discardPolicyLow_,
                                     discardPolicyHigh_);
                        }
                }
            } catch (BAD_OPERATION e) {
                // Nothing to do. an error has already been added to
                // List 'errors'.
            }
        }
    }
}
