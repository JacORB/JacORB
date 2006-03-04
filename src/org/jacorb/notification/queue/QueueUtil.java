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

import java.util.Comparator;

import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class QueueUtil
{
    private QueueUtil()
    {
        // not intended to be invoked
    }

    ////////////////////////////////////////

    static final Message[] MESSAGE_ARRAY_TEMPLATE = new Message[0];

    public static Comparator ASCENDING_TIMEOUT_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            final Message _left = (Message) left;
            final Message _right = (Message) right;

            if (_left.hasTimeout())
            {
                if (!_right.hasTimeout())
                {
                    return -1;
                }
                
                return compareLong(_left.getTimeout(), _right.getTimeout());
            }
            else if (_right.hasTimeout())
            {
                return 1;
            }

            return 0;
        }
    };

    public static Comparator ASCENDING_INSERT_ORDER_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            final Message _left = (Message) left;
            final Message _right = (Message) right;

            return compareLong(_left.getReceiveTimestamp(), _right.getReceiveTimestamp());
        }
    };

    public static Comparator DESCENDING_INSERT_ORDER_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            return -ASCENDING_INSERT_ORDER_COMPARATOR.compare(left, right);
        }
    };

    public static Comparator ASCENDING_PRIORITY_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            final Message _right = (Message) right;

            final Message _left = (Message) left;
            
            return _left.getPriority() - _right.getPriority();
        }        
    };
    
    public static Comparator DESCENDING_PRIORITY_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            return -ASCENDING_PRIORITY_COMPARATOR.compare(left, right);
        }
    };
    
    private static int compareLong(long left, long right)
    {
        return left < right ? -1 : (left == right ? 0 : 1);
    }
}