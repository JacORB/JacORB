package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

import java.util.Date;

import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterUtils;
import org.jacorb.notification.interfaces.Message;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedEventMessage extends AbstractMessage
{
    public static final String TYPE_NAME = "%TYPED";

    public static final String OPERATION_NAME = "operation";

    public static final String EVENT_TYPE = "event_type";

    private static final ORB sORB = ORB.init();

    private static final Any sUndefinedAny = sORB.create_any();

    private static final EventHeader sEventHeader;

    private static final String sTypedKey =
        FilterUtils.calcConstraintKey( "", TYPE_NAME );

    static {
        EventType _eventType = new EventType( "", TYPE_NAME );
        FixedEventHeader _fixedHeader = new FixedEventHeader( _eventType, "" );
        Property[] _variableHeader = new Property[ 0 ];
        sEventHeader = new EventHeader( _fixedHeader, _variableHeader );
    }

    ////////////////////////////////////////

    private String idlInterfaceName_;

    private String operationName_;

    private Property[] parameters_;

    /**
     * used for conversion to filterable TypedEvent
     */
    private Property[] typedEvent_;

    /**
     * used for conversion to Any and StructuredEvent
     */
    private Property[] filterableHeader_;

    private Any anyValue_;

    private StructuredEvent structuredEventValue_;

    ////////////////////////////////////////

    public void reset() {
        super.reset();

        typedEvent_ = null;
        parameters_ = null;
        filterableHeader_ = null;
        anyValue_ = null;
        structuredEventValue_ = null;
    }


    public String getConstraintKey()
    {
        return sTypedKey;
    }


    public void setTypedEvent(String interfaceName, String operation, Property[] params) {
        idlInterfaceName_ = interfaceName;

        operationName_ = operation;

        parameters_ = params;
    }


    public void setTypedEvent(Property[] props) {
        parameters_ = props;
    }


    private Property[] getFilterableHeader() {
        if (filterableHeader_ == null) {
            filterableHeader_ = new Property[parameters_.length + 1];
            Any _operationAny = sORB.create_any();
            _operationAny.insert_string(operationName_);
            filterableHeader_[0] = new Property(OPERATION_NAME, _operationAny);

            for (int x=0; x<parameters_.length; ++x) {
                filterableHeader_[1 + x] = parameters_[x];
            }
        }

        return filterableHeader_;
    }


    public Any toAny()
    {
        if (anyValue_ == null) {
            Property[] _filterableHeader = getFilterableHeader();

            anyValue_ = sORB.create_any();

            PropertySeqHelper.insert(anyValue_, _filterableHeader);
        }
        return anyValue_;
    }


    public StructuredEvent toStructuredEvent()
    {
        if (structuredEventValue_ == null) {
            structuredEventValue_ = new StructuredEvent();

            structuredEventValue_.header = sEventHeader;
            structuredEventValue_.filterable_data = getFilterableHeader();
            structuredEventValue_.remainder_of_body = sUndefinedAny;
        }
        return structuredEventValue_;
    }


    public Property[] toTypedEvent() {
        if (typedEvent_ == null) {
            typedEvent_ = new Property[parameters_.length + 1];

            EventType _eventType = new EventType();
            _eventType.domain_name = idlInterfaceName_;
            _eventType.type_name = operationName_;

            Any _eventTypeAny = sORB.create_any();
            EventTypeHelper.insert(_eventTypeAny, _eventType);

            typedEvent_[0] = new Property(EVENT_TYPE, _eventTypeAny);

            for (int x=0; x<parameters_.length; ++x) {
                typedEvent_[1 + x] = parameters_[x];
            }
        }
        return typedEvent_;
    }


    public int getType()
    {
        return Message.TYPE_TYPED;
    }


    public EvaluationResult extractFilterableData(EvaluationContext evaluationContext,
                                                  ComponentName componentName,
                                                  String headerName)
        throws EvaluationException
    {
        throw new EvaluationException();
    }


    public EvaluationResult extractVariableHeader(EvaluationContext evaluationContext,
                                                  ComponentName componentName,
                                                  String headerName)
        throws EvaluationException
    {
        for (int x=0; x<parameters_.length; ++x) {
            if (parameters_[x].name.equals(headerName)) {
                EvaluationResult _result = new EvaluationResult();
                _result.addAny(parameters_[x].value);

                return _result;
            }
        }

        throw new EvaluationException();
    }


    public boolean hasStartTime()
    {
        return false;
    }


    public Date getStartTime()
    {
        return null;
    }


    public boolean hasStopTime()
    {
        return false;
    }


    public Date getStopTime()
    {
        return null;
    }


    public boolean hasTimeout()
    {
        return false;
    }


    public long getTimeout()
    {
        return 0;
    }


    public int getPriority()
    {
        return 0;
    }


    public boolean match(Filter filter) throws UnsupportedFilterableData
    {
        return filter.match_typed(toTypedEvent());
    }


    public boolean match(MappingFilter mappingFilter, AnyHolder anyHolder)
        throws UnsupportedFilterableData
    {
        return mappingFilter.match_typed(toTypedEvent(), anyHolder);
    }
}
