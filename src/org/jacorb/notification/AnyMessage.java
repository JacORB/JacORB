package org.jacorb.notification;

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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.FilterUtils;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class AnyMessage extends AbstractMessage
{
    public static final String TYPE_NAME = "%ANY";

    public static final int DEFAULT_PRIORITY = 0;

    private static final Property[] sFilterableData;

    private static final EventHeader sEventHeader;

    private static final String sAnyKey =
        FilterUtils.calcConstraintKey( "", TYPE_NAME );

    static {
        EventType _eventType = new EventType( "", TYPE_NAME );
        FixedEventHeader _fixedHeader = new FixedEventHeader( _eventType, "" );
        Property[] _variableHeader = new Property[ 0 ];
        sEventHeader = new EventHeader( _fixedHeader, _variableHeader );
        sFilterableData = new Property[ 0 ];
    }

    ////////////////////////////////////////

    /**
     * the wrapped value
     */
    protected Any anyValue_;

    /**
     * the wrapped Any converted to a StructuredEvent
     */
    protected StructuredEvent structuredEventValue_;

    ////////////////////////////////////////

    public AnyMessage( )
    {
        super( );
    }

    ////////////////////////////////////////

    public void setAny( Any any )
    {
        anyValue_ = any;
    }


    public void reset()
    {
        super.reset();

        anyValue_ = null;
        structuredEventValue_ = null;
    }


    public int getType()
    {
        return Message.TYPE_ANY;
    }


    public Any toAny()
    {
        return anyValue_;
    }


    public synchronized StructuredEvent toStructuredEvent()
    {
        // the conversion should only be done once !

        if ( structuredEventValue_ == null )
        {
            structuredEventValue_ = new StructuredEvent();
            structuredEventValue_.header = sEventHeader;
            structuredEventValue_.filterable_data = sFilterableData;
            structuredEventValue_.remainder_of_body = toAny();
        }

        return structuredEventValue_;
    }


    public String getConstraintKey()
    {
        return sAnyKey;
    }


    public EvaluationResult extractFilterableData( EvaluationContext context,
                                                   ComponentName root,
                                                   String v )
        throws EvaluationException
    {
        return extractValue( context, root );
    }


    public EvaluationResult extractVariableHeader( EvaluationContext context,
                                                   ComponentName root,
                                                   String v ) throws EvaluationException
    {
        return extractValue( context, root );
    }


    public boolean match( Filter filter ) throws UnsupportedFilterableData
    {
        return filter.match( toAny() );
    }


    public int getPriority()
    {
        return DEFAULT_PRIORITY;
    }


    public boolean match( MappingFilter filter,
                          AnyHolder value ) throws UnsupportedFilterableData
    {
        return filter.match( toAny(), value );
    }


    public boolean hasStartTime()
    {
        return false;
    }


    public Date getStartTime()
    {
        throw new UnsupportedOperationException();
    }


    public boolean hasStopTime()
    {
        return false;
    }


    public Date getStopTime()
    {
        throw new UnsupportedOperationException();
    }


    public boolean hasTimeout()
    {
        return false;
    }


    public long getTimeout()
    {
        throw new UnsupportedOperationException();
    }
}
