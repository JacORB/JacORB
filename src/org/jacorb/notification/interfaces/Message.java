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
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.UnsupportedFilterableData;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.RuntimeVariableNode;

import java.util.Date;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public interface Message extends Disposable
{
    public interface MessageStateListener {
        public void actionLifetimeChanged(long lifetime);
    }

    public static final int TYPE_ANY = 0;
    public static final int TYPE_STRUCTURED = 1;
    public static final int TYPE_TYPED = 2;

    public void setMessageStateListener(MessageStateListener listener);

    public MessageStateListener removeMessageStateListener();

    public String getConstraintKey();

    public Any toAny();

    public StructuredEvent toStructuredEvent();

    public FilterStage getInitialFilterStage();

    public void setInitialFilterStage( FilterStage node );

    public EvaluationResult extractValue(EvaluationContext context,
                                         ComponentName componentRootNode,
                                         RuntimeVariableNode runtimeVariable )
        throws EvaluationException,
               DynamicTypeException;

    public  EvaluationResult extractFilterableData(EvaluationContext context,
                                                   ComponentName componentRootNode,
                                                   String variable)
        throws EvaluationException;

    public  EvaluationResult extractVariableHeader(EvaluationContext context,
                                                   ComponentName componentRootNode,
                                                   String variable)
        throws EvaluationException;

    public EvaluationResult extractValue( EvaluationContext evaluationContext,
                                          ComponentName componentRootNode )
        throws EvaluationException;

    public boolean hasStartTime();

    public Date getStartTime();

    public boolean hasStopTime();

    public Date getStopTime();

    public boolean hasTimeout();

    public long getTimeout();

    public void setTimeout(long timeout);

    public int getPriority();

    public void setPriority(int priority);

    public boolean match(FilterStage filterStage);

    public boolean match(MappingFilter filter,
                         AnyHolder value) throws UnsupportedFilterableData;

    public Object clone();

    public boolean isInvalid();

    public int getType();

    public void actionTimeout();

}
