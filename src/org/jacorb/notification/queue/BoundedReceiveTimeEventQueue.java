/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2006 Gerald Brose
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

package org.jacorb.notification.queue;

import org.jacorb.notification.interfaces.Message;

/**
 * a bounded event queue that orders the
 * added messages by their receivetimestamp attribute.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BoundedReceiveTimeEventQueue extends AbstractBoundedEventHeap
{
    public BoundedReceiveTimeEventQueue( int maxSize,
                                      EventQueueOverflowStrategy overflowStrategy )
    {
        super(maxSize, overflowStrategy, new Object(), QueueUtil.ASCENDING_RECEIVE_TIME_COMPARATOR);
    }
    
    protected Message getEarliestTimeout()
    {
        return removeFirstElement(QueueUtil.ASCENDING_TIMEOUT_COMPARATOR);
    }

    protected Message getLeastPriority()
    {
        return removeFirstElement(QueueUtil.ASCENDING_PRIORITY_COMPARATOR);  
    }

    protected Message getOldestElement()
    {
        return removeFirstElement(QueueUtil.DESCENDING_INSERT_ORDER_COMPARATOR);
    }

    protected Message getYoungestElement()
    {
        return removeFirstElement(QueueUtil.ASCENDING_INSERT_ORDER_COMPARATOR);  
    }

    public String getOrderPolicyName()
    {
        return "FifoOrder";
    }
}
