package org.jacorb.notification.interfaces;

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

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;

import org.jacorb.notification.AbstractMessage;
import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.RuntimeVariable;

import java.util.Date;
import org.jacorb.notification.*;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface Message extends Disposable
{
    interface MessageStateListener {
        void actionLifetimeChanged(long lifetime);
    }

    int TYPE_ANY = 0;

    int TYPE_STRUCTURED = 1;

    int TYPE_TYPED = 2;


    void setMessageStateListener(MessageStateListener listener);


    MessageStateListener removeMessageStateListener();


    String getConstraintKey();


    Any toAny();


    StructuredEvent toStructuredEvent();


    Property[] toTypedEvent() throws NoTranslationException;


    FilterStage getInitialFilterStage();


    void setInitialFilterStage( FilterStage node );


    EvaluationResult extractValue(EvaluationContext context,
                                  ComponentName componentRootNode,
                                  RuntimeVariable runtimeVariable )
        throws EvaluationException;


    EvaluationResult extractFilterableData(EvaluationContext context,
                                           ComponentName componentRootNode,
                                           String variable)
        throws EvaluationException;


    EvaluationResult extractVariableHeader(EvaluationContext context,
                                           ComponentName componentRootNode,
                                           String variable)
        throws EvaluationException;



    EvaluationResult extractValue( EvaluationContext evaluationContext,
                                   ComponentName componentRootNode )
        throws EvaluationException;


    boolean hasStartTime();


    Date getStartTime();


    boolean hasStopTime();


    Date getStopTime();


    boolean hasTimeout();


    long getTimeout();


    void setTimeout(long timeout);


    int getPriority();


    void setPriority(int priority);


    boolean match(FilterStage filterStage);


    boolean match(MappingFilter filter,
                  AnyHolder value) throws UnsupportedFilterableData;


    Object clone();


    boolean isInvalid();


    int getType();


    void actionTimeout();
}
