package org.jacorb.notification.queue;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.util.QoSPropertySet;
import org.jacorb.notification.conf.Default;

import org.omg.CosNotification.AnyOrder;
import org.omg.CosNotification.DeadlineOrder;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.LifoOrder;
import org.omg.CosNotification.MaxEventsPerConsumer;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.PriorityOrder;
import org.omg.CosNotification.UnsupportedQoS;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventQueueFactory implements Configurable
{
    private static final short UNKNOWN_POLICY = Short.MIN_VALUE;
    private static final Map mapOrderPolicyNameToValue = new HashMap();
    private static final Map mapDiscardPolicyNameToValue = new HashMap();
    private static final String[] mapOrderPolicyValueToName;
    private static final String[] mapDiscardPolicyValueToName;

    private String orderPolicy_;
    private String discardPolicy_;

    static {
        mapOrderPolicyNameToValue.put("AnyOrder",
                                      new Short(AnyOrder.value));
        mapOrderPolicyNameToValue.put("FifoOrder",
                                      new Short(FifoOrder.value));
        mapOrderPolicyNameToValue.put("PriorityOrder",
                                      new Short(PriorityOrder.value));
        mapOrderPolicyNameToValue.put("DeadlineOrder",
                                      new Short(DeadlineOrder.value));
        mapOrderPolicyValueToName = new String[] {
                                        "AnyOrder",
                                        "FifoOrder",
                                        "PriorityOrder",
                                        "DeadlineOrder"
                                    };

        mapDiscardPolicyNameToValue.put( "AnyOrder",
                                         new Short(AnyOrder.value));
        mapDiscardPolicyNameToValue.put( "FifoOrder",
                                         new Short(FifoOrder.value));
        mapDiscardPolicyNameToValue.put( "LifoOrder",
                                         new Short(LifoOrder.value));
        mapDiscardPolicyNameToValue.put( "PriorityOrder",
                                         new Short(PriorityOrder.value));
        mapDiscardPolicyNameToValue.put( "DeadlineOrder",
                                         new Short(DeadlineOrder.value));
        mapDiscardPolicyValueToName = new String[] {
                                          "AnyOrder",
                                          "FifoOrder",
                                          "PriorityOrder",
                                          "DeadlineOrder",
                                          "LifoOrder"
                                      };

    }

    public void configure(Configuration conf)
    {
        orderPolicy_ = conf.getAttribute( Attributes.ORDER_POLICY,
                                          Default.DEFAULT_ORDER_POLICY );

        discardPolicy_ = conf.getAttribute( Attributes.DISCARD_POLICY,
                                            Default.DEFAULT_DISCARD_POLICY );
    }

    ////////////////////////////////////////

    public EventQueueFactory()
    {}

    ////////////////////////////////////////

    public EventQueue newEventQueue( QoSPropertySet qosProperties )
        throws UnsupportedQoS
    {
        short shortOrderPolicy = orderPolicyNameToValue( orderPolicy_ );

        short shortDiscardPolicy = discardPolicyNameToValue( discardPolicy_ );

        int maxEventsPerConsumer;

        try {
            maxEventsPerConsumer = qosProperties.get( MaxEventsPerConsumer.value ).extract_long();
        } catch (Exception e) {
            maxEventsPerConsumer = Default.DEFAULT_MAX_EVENTS_PER_CONSUMER;
        }

        if (qosProperties.containsKey( OrderPolicy.value ))
        {
            shortOrderPolicy =
                qosProperties.get(OrderPolicy.value).extract_short();
        }

        if (qosProperties.containsKey(DiscardPolicy.value))
        {
            shortDiscardPolicy =
                qosProperties.get( DiscardPolicy.value ).extract_short();
        }

        AbstractBoundedEventQueue queue;

        switch ( shortOrderPolicy )
        {
            case AnyOrder.value:
                // fallthrough

            case FifoOrder.value:
                queue = new BoundedFifoEventQueue( maxEventsPerConsumer );
                break;

            case PriorityOrder.value:
                queue = new BoundedPriorityEventQueue( maxEventsPerConsumer );
                break;

            case DeadlineOrder.value:
                queue = new BoundedDeadlineEventQueue( maxEventsPerConsumer );
                break;

            default:
                throw new IllegalArgumentException( "Orderpolicy: "
                                                    + orderPolicy_
                                                    + " OrderPolicyValue: "
                                                    + shortOrderPolicy
                                                    + " unknown" );
        }

        switch ( shortDiscardPolicy )
        {
            case AnyOrder.value:
                // fallthrough

            case FifoOrder.value:
                queue.setOverflowStrategy( EventQueueOverflowStrategy.FIFO );
                break;

            case LifoOrder.value:
                queue.setOverflowStrategy( EventQueueOverflowStrategy.LIFO );
                break;

            case PriorityOrder.value:
                queue.setOverflowStrategy( EventQueueOverflowStrategy.LEAST_PRIORITY );
                break;

            case DeadlineOrder.value:
                queue.setOverflowStrategy( EventQueueOverflowStrategy.EARLIEST_TIMEOUT );
                break;

            default:
                throw new IllegalArgumentException( "Discardpolicy: "
                                                    + discardPolicy_
                                                    + "DiscardPolicyValue: "
                                                    + shortDiscardPolicy
                                                    + " unknown" );
        }
        return queue;
    }


    public static short orderPolicyNameToValue( String orderPolicyName )
    {
        if (mapOrderPolicyNameToValue.containsKey(orderPolicyName))
            return ((Short)mapOrderPolicyNameToValue.get(orderPolicyName)).
                   shortValue();
        return UNKNOWN_POLICY;
    }


    public static short discardPolicyNameToValue( String discardPolicyName )
    {
        if (mapDiscardPolicyNameToValue.containsKey(discardPolicyName))
            return ((Short)mapDiscardPolicyNameToValue.get(discardPolicyName)).
                   shortValue();
        return UNKNOWN_POLICY;
    }
}
