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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class QoSPropertySet extends PropertySet
{
    public static final int CHANNEL_QOS = 0;
    public static final int ADMIN_QOS = 1;
    public static final int PROXY_QOS = 2;

    private static HashSet validPerChannelQoS_;
    private static HashSet validPerAdminQoS_;
    private static HashSet validPerProxyQoS_;
    private static HashSet validPerMessageQoS_;

    static Any connectionReliabilityLowValue_;
    static Any connectionReliabilityHighValue_;

    static Any orderPolicyLowValue_;
    static Any orderPolicyHighValue_;

    static Any discardPolicyLowValue_;
    static Any discardPolicyHighValue_;

    ////////////////////////////////////////

    static {
        validPerChannelQoS_ = new HashSet();
        validPerChannelQoS_.add(EventReliability.value);
        validPerChannelQoS_.add(ConnectionReliability.value);
        validPerChannelQoS_.add(Priority.value);
        validPerChannelQoS_.add(Timeout.value);
        validPerChannelQoS_.add(StartTimeSupported.value);
        validPerChannelQoS_.add(StopTimeSupported.value);
        validPerChannelQoS_.add(MaxEventsPerConsumer.value);
        validPerChannelQoS_.add(OrderPolicy.value);
        validPerChannelQoS_.add(DiscardPolicy.value);
        validPerChannelQoS_.add(MaximumBatchSize.value);
        validPerChannelQoS_.add(PacingInterval.value);

        validPerAdminQoS_ = new HashSet(validPerChannelQoS_);
        validPerAdminQoS_.remove(EventReliability.value);

        validPerProxyQoS_ = new HashSet(validPerAdminQoS_);

        validPerMessageQoS_ = new HashSet();
        validPerMessageQoS_.add(EventReliability.value);
        validPerMessageQoS_.add(Priority.value);
        validPerMessageQoS_.add(StartTime.value);
        validPerMessageQoS_.add(StopTime.value);
        validPerMessageQoS_.add(Timeout.value);


        connectionReliabilityHighValue_ = PropertySet.orb_.create_any();
        connectionReliabilityHighValue_.insert_short(Persistent.value);

        connectionReliabilityLowValue_ = PropertySet.orb_.create_any();
        connectionReliabilityLowValue_.insert_short(BestEffort.value);

        orderPolicyLowValue_ = PropertySet.orb_.create_any();
        orderPolicyLowValue_.insert_short(AnyOrder.value);

        orderPolicyHighValue_ = PropertySet.orb_.create_any();
        orderPolicyHighValue_.insert_short(DeadlineOrder.value);

        discardPolicyLowValue_ = PropertySet.orb_.create_any();
        discardPolicyLowValue_.insert_short(AnyOrder.value);

        discardPolicyHighValue_ = PropertySet.orb_.create_any();
        discardPolicyHighValue_.insert_short(DeadlineOrder.value);
    }

    ////////////////////////////////////////

    private int type_;

    private HashSet validNames_;

    ////////////////////////////////////////

    public QoSPropertySet(int type)
    {
        super();

        type_ = type;

        init(type);
    }


    public QoSPropertySet(int type, Property[] props)
    {
        super(props);

        type_ = type;

        init(type);
    }


    private void init(int type)
    {
        switch (type)
            {
            case CHANNEL_QOS:
                validNames_ = validPerChannelQoS_;
                break;
            case ADMIN_QOS:
                validNames_ = validPerAdminQoS_;
                break;
            case PROXY_QOS:
                validNames_ = validPerProxyQoS_;
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " is unknown");
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


    public void validate_qos(Property[] ps,
                             NamedPropertyRangeSeqHolder namedPropertyRange)
        throws UnsupportedQoS
    {

        List _errors = new ArrayList();

        checkPropertyExistence(ps, _errors);

        checkPropertyValues(ps, _errors);

        if (!_errors.isEmpty())
            {
                throw new UnsupportedQoS((PropertyError[])_errors.toArray(PropertySet.PROPERTY_ERROR_ARRAY_TEMPLATE));
            }
    }



    private void checkPropertyValues(Property[] ps, List errors)
    {
        for (int x = 0; x < ps.length; ++x)
            {
                if (ConnectionReliability.value.equals(ps[x].name))
                    {
                        try
                            {
                                switch (ps[x].value.extract_short())
                                    {
                                    case BestEffort.value:
                                        // fallthrough
                                    case Persistent.value:
                                        break;
                                    default:
                                        errors.add(new PropertyError(QoSError_code.BAD_VALUE,
                                                                     ps[x].name,
                                                                     new PropertyRange(connectionReliabilityLowValue_,
                                                                                       connectionReliabilityHighValue_)));
                                    }
                            }
                        catch (BAD_OPERATION e)
                            {
                                errors.add(badType(ps[x].name));
                            }
                    }
                else if (EventReliability.value.equals(ps[x].name))
                    {
                        try
                            {
                                switch (ps[x].value.extract_short())
                                    {
                                    case BestEffort.value:
                                        // fallthrough
                                    case Persistent.value:
                                        break;
                                    default:
                                        errors.add(new PropertyError(QoSError_code.BAD_VALUE,
                                                                     ps[x].name,
                                                                     new PropertyRange(connectionReliabilityLowValue_,
                                                                                       connectionReliabilityHighValue_)));
                                    }
                            }
                        catch (BAD_OPERATION e)
                            {
                                errors.add(badType(ps[x].name));
                            }
                    }
                else if (OrderPolicy.value.equals(ps[x].name))
                    {
                        try
                            {
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
                                        errors.add(new PropertyError(QoSError_code.BAD_VALUE,
                                                                     ps[x].name,
                                                                     new PropertyRange(orderPolicyLowValue_,
                                                                                       orderPolicyHighValue_)));
                                    }
                            }
                        catch (BAD_OPERATION e)
                            {
                                errors.add(badType(ps[x].name));
                            }
                    }
                else if (DiscardPolicy.value.equals(ps[x].name))
                    {
                        try
                            {
                                switch (ps[x].value.extract_short())
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
                                        errors.add(new PropertyError(QoSError_code.BAD_VALUE,
                                                                     ps[x].name,
                                                                     new PropertyRange(discardPolicyLowValue_,
                                                                                       discardPolicyHighValue_)));
                                    }
                            }
                        catch (BAD_OPERATION e)
                            {
                                errors.add(badType(ps[x].name));
                            }

                    }
            }
    }
}

