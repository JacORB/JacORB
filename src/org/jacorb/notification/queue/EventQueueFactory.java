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

import java.util.Hashtable;

import org.jacorb.notification.Constants;
import org.jacorb.notification.ConfigurableProperties;
import org.jacorb.notification.PropertyManager;
import org.jacorb.util.Environment;

import org.omg.CosNotification.AnyOrder;
import org.omg.CosNotification.DeadlineOrder;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.LifoOrder;
import org.omg.CosNotification.MaxEventsPerConsumer;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.PriorityOrder;
import org.omg.CosNotification.UnsupportedQoS;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * EventQueueFactory.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventQueueFactory
{
    static Logger sLogger =
        Hierarchy.getDefaultHierarchy().getLoggerFor( EventQueueFactory.class.getName() );

    private final static short UNKNOWN_POLICY = Short.MIN_VALUE;

    private final static Hashtable mapOrderPolicyNameToValue = new Hashtable();

    private final static Hashtable mapDiscardPolicyNameToValue = new Hashtable();

    private final static String[] mapOrderPolicyValueToName;

    private final static String[] mapDiscardPolicyValueToName;

    static {
        mapOrderPolicyNameToValue.put( "AnyOrder", new Short( AnyOrder.value ) );
        mapOrderPolicyNameToValue.put( "FifoOrder", new Short( FifoOrder.value ) );
        mapOrderPolicyNameToValue.put( "PriorityOrder", new Short( PriorityOrder.value ) );
        mapOrderPolicyNameToValue.put( "DeadlineOrder", new Short( DeadlineOrder.value ) );

        mapOrderPolicyValueToName = new String[] {
                                        "AnyOrder",
                                        "FifoOrder",
                                        "PriorityOrder",
                                        "DeadlineOrder"
                                    };

        mapDiscardPolicyNameToValue.put( "AnyOrder", new Short( AnyOrder.value ) );
        mapDiscardPolicyNameToValue.put( "FifoOrder", new Short( FifoOrder.value ) );
        mapDiscardPolicyNameToValue.put( "LifoOrder", new Short( LifoOrder.value ) );
        mapDiscardPolicyNameToValue.put( "PriorityOrder", new Short( PriorityOrder.value ) );
        mapDiscardPolicyNameToValue.put( "DeadlineOrder", new Short( DeadlineOrder.value ) );

        mapDiscardPolicyValueToName = new String[] {
                                          "AnyOrder",
                                          "FifoOrder",
                                          "PriorityOrder",
                                          "DeadlineOrder",
                                          "LifoOrder"
                                      };
    }

    public static EventQueue newEventQueue( PropertyManager qosProperties ) throws UnsupportedQoS
    {

        int maxEventsPerConsumer =
            Environment.getIntPropertyWithDefault( ConfigurableProperties.MAX_EVENTS_PER_CONSUMER,
                                                   Constants.DEFAULT_MAX_EVENTS_PER_CONSUMER );

        String orderPolicy = Environment.getProperty( ConfigurableProperties.ORDER_POLICY,
                             Constants.DEFAULT_ORDER_POLICY );


        String discardPolicy = Environment.getProperty( ConfigurableProperties.DISCARD_POLICY,
                               Constants.DEFAULT_DISCARD_POLICY );

        sLogger.debug( "OrderPolicy: " + orderPolicy );
        sLogger.debug( "DiscardPolicy: " + discardPolicy );

        short shortOrderPolicy = orderPolicyNameToValue( orderPolicy );

        short shortDiscardPolicy = discardPolicyNameToValue( discardPolicy );

        if ( qosProperties.hasProperty( MaxEventsPerConsumer.value ) )
        {
            maxEventsPerConsumer =
                qosProperties.getProperty( MaxEventsPerConsumer.value ).extract_long();
        }

        if ( qosProperties.hasProperty( OrderPolicy.value ) )
        {
            shortOrderPolicy =
                qosProperties.getProperty( OrderPolicy.value ).extract_short();
        }

        if ( qosProperties.hasProperty( DiscardPolicy.value ) )
        {
            shortDiscardPolicy = qosProperties.getProperty( DiscardPolicy.value ).extract_short();
        }

        sLogger.info( "Create EventQueue with the Settings: MAX_EVENTS_PER_CONSUMER="
                      + maxEventsPerConsumer
                      + "/ORDER_POLICY=" + mapOrderPolicyValueToName[ shortOrderPolicy ]
                      + "/DISCARD_POLICY=" + mapDiscardPolicyValueToName[ shortDiscardPolicy ] );

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
                                                + orderPolicy
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
                                                + discardPolicy
                                                + "DiscardPolicyValue: "
                                                + shortDiscardPolicy
                                                + " unknown" );
        }

        return queue;
    }

    public static short orderPolicyNameToValue( String orderPolicyName )
    {
        if ( mapOrderPolicyNameToValue.containsKey( orderPolicyName ) )
        {
            return ( ( Short ) mapOrderPolicyNameToValue.get( orderPolicyName ) ).shortValue();
        }
        else
        {
            return UNKNOWN_POLICY;
        }
    }

    public static short discardPolicyNameToValue( String discardPolicyName )
    {
        if ( mapDiscardPolicyNameToValue.containsKey( discardPolicyName ) )
        {
            return ( ( Short ) mapDiscardPolicyNameToValue.get( discardPolicyName ) ).shortValue();
        }
        else
        {
            return UNKNOWN_POLICY;
        }
    }
}
