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

import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEventHelper;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.ComponentName;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.DotOperator;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.RuntimeVariableNode;

/**
 * Adapt a StructuredEvent to the NotificationEvent Interface.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class NotificationStructuredEvent extends NotificationEvent
{

    private Any anyValue_;
    private StructuredEvent structuredEventValue_;
    private String constraintKey_;

    NotificationStructuredEvent( ApplicationContext appContext )
    {
        super( appContext );
    }

    public void setStructuredEventValue( StructuredEvent event )
    {
        structuredEventValue_ = event;

        constraintKey_ = 
	    FilterUtils.calcConstraintKey( structuredEventValue_.header.fixed_header.event_type.domain_name,
					   structuredEventValue_.header.fixed_header.event_type.type_name );
    }

    public void reset()
    {
        super.reset();
	anyValue_ = null;
        structuredEventValue_ = null;
        constraintKey_ = null;
    }

    public int getType()
    {
        return TYPE_STRUCTURED;
    }

    public Any toAny()
    {
	if (anyValue_ == null) {
	    synchronized(this) {
		if (anyValue_ == null) {
		    anyValue_ = applicationContext_.getOrb().create_any();
		    StructuredEventHelper.insert( anyValue_, structuredEventValue_ );
		}
	    }
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
	try {
	    Any _a =
		context.getDynamicEvaluator().evaluatePropertyList(structuredEventValue_.filterable_data, v);
	    return context.getResultExtractor().extractFromAny(_a);

	} catch (InconsistentTypeCode e) {
	} catch (TypeMismatch e) {
	} catch (InvalidValue e) {
	}
	throw new EvaluationException();
	
    }

    public EvaluationResult extractVariableHeader(EvaluationContext context,
						  ComponentName root,
						  String v) throws EvaluationException {
	
	try {
	    Any _a = 
		context.getDynamicEvaluator().evaluatePropertyList(structuredEventValue_.header.variable_header, v);
	    
		return context.getResultExtractor().extractFromAny(_a);
	} catch (InconsistentTypeCode e) {
	} catch (TypeMismatch e) {
	} catch (InvalidValue e) {
	}
	throw new EvaluationException();
    }

}
