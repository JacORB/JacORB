package org.jacorb.notification.queue;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 */

public interface EventQueueOverflowStrategy
{
    Message removeElementFromQueue( AbstractBoundedEventQueue queue );

    String getDiscardPolicyName();
    
    EventQueueOverflowStrategy FIFO =
        new EventQueueOverflowStrategy()
        {
            public Message removeElementFromQueue( AbstractBoundedEventQueue queue )
            {
                return queue.getOldestElement();
            }
            
            public String getDiscardPolicyName()
            {
                return "FifoOrder";
            }
        };

    EventQueueOverflowStrategy LIFO =
        new EventQueueOverflowStrategy()
        {
            public Message removeElementFromQueue( AbstractBoundedEventQueue queue )
            {
                return queue.getYoungestElement();
            }
            
            public String getDiscardPolicyName()
            {
                return "LifoOrder";
            }
        };

    EventQueueOverflowStrategy LEAST_PRIORITY =
        new EventQueueOverflowStrategy()
        {
            public Message removeElementFromQueue( AbstractBoundedEventQueue queue )
            {
                return queue.getLeastPriority();
            }
            
            public String getDiscardPolicyName()
            {
               return "PriorityOrder";
            }
        };

    EventQueueOverflowStrategy EARLIEST_TIMEOUT =
        new EventQueueOverflowStrategy()
        {
            public Message removeElementFromQueue( AbstractBoundedEventQueue queue )
            {
                return queue.getEarliestTimeout();
            }
            
            public String getDiscardPolicyName()
            {
                return "TimeoutOrder";
            }
        };
}
