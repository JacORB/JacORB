package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.jacorb.notification.filter.EventTypeIdentifier;
import org.omg.CosNotification.EventType;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventTypeWrapper implements EventTypeIdentifier, Comparable
{
    private final EventType eventType_;
    private final String constraintKey_;
    
    public static final EventType EVENT_TYPE_ALL = new EventType("", "%ALL");
    
    public static final EventType[] EMPTY_EVENT_TYPE_ARRAY = new EventType[0];
    
    public EventTypeWrapper(EventType eventType)
    {
        eventType_ = eventType;
        
        constraintKey_ =
            AbstractMessage.calcConstraintKey( eventType.domain_name, eventType.type_name );
    }

    public EventType getEventType()
    {
        return eventType_;
    }
    
    public String toString()
    {
        return toString(eventType_);
    }
    
    public boolean equals(Object other)
    {
        try {
            return equals(eventType_, ((EventTypeWrapper)other).eventType_);
        } catch (ClassCastException e)
        {
            return super.equals(other);
        }
    }
    
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    public int compareTo(Object o)
    {
        try
        {
            EventTypeWrapper _other = (EventTypeWrapper) o;

            int _compare = eventType_.domain_name
                    .compareTo(_other.eventType_.domain_name);

            if (_compare == 0)
            {
                _compare = eventType_.type_name
                        .compareTo(_other.eventType_.type_name);
            }

            return _compare;

        } catch (ClassCastException e)
        {
            throw new IllegalArgumentException();
        }
    }
    
    ////////////////////////////////////////

    public static String toString(EventType et)
    {
        StringBuffer buffer = new StringBuffer();
        appendEventTypeToBuffer(et, buffer);
        return buffer.toString();
    }

    private static void appendEventTypeToBuffer(EventType et, StringBuffer buffer)
    {
        buffer.append(et.domain_name);
        buffer.append("/");
        buffer.append(et.type_name);
    }

    public static String toString(EventType[] ets)
    {
        StringBuffer b = new StringBuffer("[");

        for (int x = 0; x < ets.length; ++x)
        {
            appendEventTypeToBuffer(ets[x], b);

            if (x < ets.length - 1)
            {
                b.append(", ");
            }
        }

        b.append("]");

        return b.toString();
    }

    public static boolean equals(EventType left, EventType right)
    {
        return left.domain_name.equals(right.domain_name) && left.type_name.equals(right.type_name);
    }
    
    public String getConstraintKey()
    {
        return constraintKey_;
    }
}