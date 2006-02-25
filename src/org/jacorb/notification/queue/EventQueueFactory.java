package org.jacorb.notification.queue;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CosNotification.AnyOrder;
import org.omg.CosNotification.DeadlineOrder;
import org.omg.CosNotification.DiscardPolicy;
import org.omg.CosNotification.FifoOrder;
import org.omg.CosNotification.LifoOrder;
import org.omg.CosNotification.MaxEventsPerConsumer;
import org.omg.CosNotification.OrderPolicy;
import org.omg.CosNotification.PriorityOrder;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventQueueFactory implements Configurable
{
    private static final short UNKNOWN_POLICY = Short.MIN_VALUE;

    private static final Map mapOrderPolicyNameToValue;

    private static final Map mapDiscardPolicyNameToValue;

    private String orderPolicy_;

    private String discardPolicy_;

    static
    {
        Map orderMap = new HashMap();
        
        orderMap.put("anyorder", new Short(AnyOrder.value));
        orderMap.put("fifoorder", new Short(FifoOrder.value));
        orderMap.put("priorityorder", new Short(PriorityOrder.value));
        orderMap.put("deadlineorder", new Short(DeadlineOrder.value));

        mapOrderPolicyNameToValue = Collections.unmodifiableMap(orderMap);
        
        
        Map discardMap = new HashMap();
        
        discardMap.put("anyorder", new Short(AnyOrder.value));
        discardMap.put("fifoorder", new Short(FifoOrder.value));
        discardMap.put("lifoorder", new Short(LifoOrder.value));
        discardMap.put("priorityorder", new Short(PriorityOrder.value));
        discardMap.put("deadlineorder", new Short(DeadlineOrder.value));
        
        mapDiscardPolicyNameToValue = Collections.unmodifiableMap(discardMap);
    }

    ////////////////////////////////////////

    public EventQueueFactory(Configuration config) throws ConfigurationException
    {
        configure(config);
    }

    ////////////////////////////////////////

    public void configure(Configuration conf) throws ConfigurationException
    {
        try
        {
            setOrderPolicy(conf.getAttribute(Attributes.ORDER_POLICY, Default.DEFAULT_ORDER_POLICY));

            setDiscardPolicy(conf.getAttribute(Attributes.DISCARD_POLICY,
                    Default.DEFAULT_DISCARD_POLICY));
        } catch (IllegalArgumentException e)
        {
            throw new ConfigurationException("Invalid Policy", e);
        }
    }

    private void setDiscardPolicy(String s)
    {
        final String policy = s.toLowerCase().trim();

        if (mapDiscardPolicyNameToValue.containsKey(policy))
        {
            discardPolicy_ = policy;
        }
        else
        {
            throw new IllegalArgumentException("Invalid DiscardPolicy: " + s);
        }
    }

    private void setOrderPolicy(String s)
    {
        final String policy = s.toLowerCase().trim();

        if (mapOrderPolicyNameToValue.containsKey(policy))
        {
            orderPolicy_ = policy;
        }
        else
        {
            throw new IllegalArgumentException("Invalid OrderPolicy: " + s);
        }
    }

    public MessageQueueAdapter newMessageQueue(QoSPropertySet qosProperties)
    {
        short shortOrderPolicy = orderPolicyNameToValue(orderPolicy_);

        short shortDiscardPolicy = discardPolicyNameToValue(discardPolicy_);

        int maxEventsPerConsumer;

        try
        {
            maxEventsPerConsumer = qosProperties.get(MaxEventsPerConsumer.value).extract_long();
        } catch (Exception e)
        {
            maxEventsPerConsumer = Default.DEFAULT_MAX_EVENTS_PER_CONSUMER;
        }

        if (qosProperties.containsKey(OrderPolicy.value))
        {
            shortOrderPolicy = qosProperties.get(OrderPolicy.value).extract_short();
        }

        if (qosProperties.containsKey(DiscardPolicy.value))
        {
            shortDiscardPolicy = qosProperties.get(DiscardPolicy.value).extract_short();
        }

        final EventQueueOverflowStrategy _overflowStrategy;
        switch (shortDiscardPolicy) {
        case AnyOrder.value:
        // fallthrough. will default to FifoOrder

        case FifoOrder.value:
            _overflowStrategy = EventQueueOverflowStrategy.FIFO;
            break;

        case LifoOrder.value:
            _overflowStrategy = EventQueueOverflowStrategy.LIFO;
            break;

        case PriorityOrder.value:
            _overflowStrategy = EventQueueOverflowStrategy.LEAST_PRIORITY;
            break;

        case DeadlineOrder.value:
            _overflowStrategy = EventQueueOverflowStrategy.EARLIEST_TIMEOUT;
            break;

        default:
            throw new IllegalArgumentException("Discardpolicy: " + discardPolicy_
                    + "DiscardPolicyValue: " + shortDiscardPolicy + " unknown");
        }

        final AbstractBoundedEventQueue queue;
        switch (shortOrderPolicy) {
        case AnyOrder.value:
        // fallthrough. will default to FifoOrder

        case FifoOrder.value:
            queue = new BoundedReceiveTimeEventQueue(maxEventsPerConsumer, _overflowStrategy);
            break;

        case PriorityOrder.value:
            queue = new BoundedPriorityEventQueue(maxEventsPerConsumer, _overflowStrategy);
            break;

        case DeadlineOrder.value:
            queue = new BoundedDeadlineEventQueue(maxEventsPerConsumer, _overflowStrategy);
            break;

        default:
            throw new IllegalArgumentException("Orderpolicy: " + orderPolicy_
                    + " OrderPolicyValue: " + shortOrderPolicy + " unknown");
        }

        return new DefaultMessageQueueAdapter(queue);
    }

    private static short orderPolicyNameToValue(String orderPolicyName)
    {
        if (mapOrderPolicyNameToValue.containsKey(orderPolicyName.toLowerCase()))
        {
            return ((Short) mapOrderPolicyNameToValue.get(orderPolicyName)).shortValue();
        }
        return UNKNOWN_POLICY;
    }

    private static short discardPolicyNameToValue(String discardPolicyName)
    {
        if (mapDiscardPolicyNameToValue.containsKey(discardPolicyName.toLowerCase()))
        {
            return ((Short) mapDiscardPolicyNameToValue.get(discardPolicyName)).shortValue();
        }
        return UNKNOWN_POLICY;
    }
}