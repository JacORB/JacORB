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

import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterUtils;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Time;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.Priority;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StartTime;
import org.omg.CosNotification.StopTime;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.CosNotification.Timeout;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.TimeBase.TimeTHelper;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * Adapts a StructuredEvent to the Message Interface.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class StructuredEventMessage extends AbstractMessage
{
    private Any anyValue_;

    private StructuredEvent structuredEventValue_;

    private String constraintKey_;

    private Date startTime_;

    private Date stopTime_;

    private long timeout_ = 0;

    private boolean isTimeoutSet_;

    private short priority_;

    ////////////////////////////////////////

    StructuredEventMessage( )
    {
        super( );
    }

    ////////////////////////////////////////

    public void setStructuredEventValue( StructuredEvent event )
    {
        structuredEventValue_ = event;

        constraintKey_ =
            FilterUtils.calcConstraintKey( structuredEventValue_.header.fixed_header.event_type.domain_name,
                                            structuredEventValue_.header.fixed_header.event_type.type_name );

        parseQosSettings();
    }


    public void reset()
    {
        super.reset();

        anyValue_ = null;
        structuredEventValue_ = null;
        constraintKey_ = null;
        startTime_ = null;
        stopTime_ = null;
        priority_ = 0;
    }


    public int getType()
    {
        return Message.TYPE_STRUCTURED;
    }


    public synchronized Any toAny()
    {
        if (anyValue_ == null) {
            anyValue_ = sOrb.create_any();
            StructuredEventHelper.insert( anyValue_, structuredEventValue_ );
        }

        return anyValue_;
    }


    public StructuredEvent toStructuredEvent()
    {
        return structuredEventValue_;
    }


    public String getConstraintKey()
    {
        return constraintKey_;
    }


    public EvaluationResult extractFilterableData(EvaluationContext context,
                                                  ComponentName root,
                                                  String v) throws EvaluationException {
            Any _any =
                context.getDynamicEvaluator().evaluatePropertyList(structuredEventValue_.filterable_data, v);

            return EvaluationResult.fromAny(_any);
    }


    public EvaluationResult extractVariableHeader(EvaluationContext context,
                                                  ComponentName root,
                                                  String v)
        throws EvaluationException {


        Any _any =
            context.getDynamicEvaluator().evaluatePropertyList(structuredEventValue_.header.variable_header, v);

        return EvaluationResult.fromAny(_any);
    }


    private void parseQosSettings() {
        Property[] props = toStructuredEvent().header.variable_header;

        for (int x=0; x < props.length; ++x) {
            if (StartTime.value.equals(props[x].name)) {
                startTime_ = new Date(unixTime(UtcTHelper.extract(props[x].value)));
            } else if (StopTime.value.equals(props[x].name)) {
                stopTime_ = new Date(unixTime(UtcTHelper.extract(props[x].value)));
            } else if (Timeout.value.equals(props[x].name)) {
                setTimeout(TimeTHelper.extract(props[x].value));
            } else if (Priority.value.equals(props[x].name)) {
                priority_ = props[x].value.extract_short();
            }
        }
    }


    public static long unixTime(UtcT corbaTime) {
        long _unixTime = (corbaTime.time - Time.UNIX_OFFSET) / 10000;

        if (corbaTime.tdf != 0) {
            _unixTime = _unixTime - (corbaTime.tdf * 60000);
        }

        return _unixTime;
    }


    public boolean hasStartTime() {
        return startTime_ != null;
    }


    public Date getStartTime() {
        return startTime_;
    }


    public boolean hasStopTime() {
        return stopTime_ != null;
    }


    public Date getStopTime() {
        return stopTime_;
    }


    public boolean hasTimeout() {
        return isTimeoutSet_;
    }


    public long getTimeout() {
        return timeout_;
    }


    private void setTimeout(long timeout) {
        isTimeoutSet_ = true;
        timeout_ = timeout;
    }


    public boolean match(FilterStage destination) {
        List _filterList = destination.getFilters();

        if ( _filterList.isEmpty() )
        {
            return true;
        }

        Iterator _allFilters = _filterList.iterator();

        while ( _allFilters.hasNext() )
        {
            try
            {
                Filter _filter = ( Filter )
                                 ( ( KeyedListEntry ) _allFilters.next() ).getValue();

                if ( _filter.match_structured( toStructuredEvent() ) )
                {
                    return true;
                }
            }
            catch ( UnsupportedFilterableData e )
            {
                // error means false
                logger_.error("unsupported filterable data", e);
            }
        }

        return false;
    }


    public int getPriority() {
        return priority_;
    }


    public boolean match(MappingFilter filter, AnyHolder value) throws UnsupportedFilterableData {
        return filter.match_structured(toStructuredEvent(), value);
    }
}
