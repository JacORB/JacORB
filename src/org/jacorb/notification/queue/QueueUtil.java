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

class QueueUtil
{
    private QueueUtil()
    {
        // not intended to be invoked
    }

    ////////////////////////////////////////

    static final Message[] MESSAGE_ARRAY_TEMPLATE = new Message[0];

    static final HeapEntry[] HEAP_ENTRY_ARRAY_TEMPLATE = new HeapEntry[0];

    static Comparator ASCENDING_TIMEOUT_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            Message _left = toMessage(left);
            Message _right = toMessage(right);

            if (_left.hasTimeout())
            {
                if (!_right.hasTimeout())
                {
                    return -1;
                }

                return (int) (_left.getTimeout() - _right.getTimeout());
            }
            else if (_right.hasTimeout())
            {
                return 1;
            }

            return 0;
        }
    };

    static Comparator ASCENDING_AGE_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            HeapEntry _left = (HeapEntry) left;
            HeapEntry _right = (HeapEntry) right;

            return (int)(_left.order_ - _right.order_);
        }
    };

    static Comparator DESCENDING_AGE_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            return -ASCENDING_AGE_COMPARATOR.compare(left, right);
        }
    };

    static Comparator ASCENDING_PRIORITY_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            Message _right = toMessage(right);

            Message _left = toMessage(left);
            
            return _left.getPriority() - _right.getPriority();
        }        
    };
    
    static Comparator DESCENDING_PRIORITY_COMPARATOR = new Comparator()
    {
        public int compare(Object left, Object right)
        {
            return -ASCENDING_PRIORITY_COMPARATOR.compare(left, right);
        }
    };
    
    static Message toMessage(Object object)
    {
        final Message _message;
        
        if (object instanceof HeapEntry)
        {
            _message = ((HeapEntry) object).event_;
        }
        else if (object instanceof Message)
        {
            _message = (Message) object;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return _message;
    }    
}