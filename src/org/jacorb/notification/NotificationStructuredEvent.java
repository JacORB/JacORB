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
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.DotOperator;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.jacorb.notification.node.DynamicTypeException;

/**
 * Adapt a StructuredEvent to the NotificationEvent Interface.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class NotificationStructuredEvent extends NotificationEvent {

    private Any anyValue_;
    private StructuredEvent structuredEventValue_;

    NotificationStructuredEvent(ApplicationContext appContext) {
	super(appContext);
	anyValue_ = applicationContext_.getOrb().create_any();
    }

    public void setStructuredEventValue(StructuredEvent event) {
	structuredEventValue_ = event;
	StructuredEventHelper.insert(anyValue_, structuredEventValue_);
    }

    public void reset() {
	super.reset();
	anyValue_ = applicationContext_.getOrb().create_any();;
	structuredEventValue_ = null;
    }

    public EventTypeIdentifier getEventTypeIdentifier() {
	return null;
    }

    public int getType() {
	return TYPE_STRUCTURED;
    }

    public Any toAny() {
	return anyValue_;
    }

    public StructuredEvent toStructuredEvent() {
	return structuredEventValue_;
    }

    public EvaluationResult evaluate(EvaluationContext evaluationContext,
				     ComponentName op) throws EvaluationException {
	try {
	    TCLNode _left = (TCLNode)op.left();
	    EvaluationResult _ret = null;

	    switch(_left.getType()) {
	    case TCLNode.IDENTIFIER:
		IdentValue _iv = (IdentValue)_left;
		
		_ret = 
		    NotificationEventUtils.evaluateShorthand(evaluationContext, 
							     anyValue_, 
							     op, 
							     _iv);

		break;
	    case TCLNode.DOT:
		DotOperator _dot = (DotOperator)_left;
		_ret = NotificationEventUtils.evaluateComponent(evaluationContext, 
								anyValue_, 
								op);
		break;
	    default:
		throw new RuntimeException("Unexpected Node: " + _left.getClass().getName());
	    }
	    return _ret;
	} catch (TypeMismatch tm) {
	    reThrowException(tm);
	} catch (InconsistentTypeCode itc) {
	    reThrowException(itc);
	} catch (InvalidValue iv) {
	    reThrowException(iv);
	} catch (DynamicTypeException d) {
	    reThrowException(d);
	}
	return null;
    }

    public EvaluationResult testExists(EvaluationContext evaluationContext,
				       ComponentName op) {
	try {
	    evaluate(evaluationContext, op);
	    return EvaluationResult.BOOL_TRUE;
	} catch (EvaluationException e) {
	    return EvaluationResult.BOOL_FALSE;
	}
    }

    public String getConstraintKey() {
	return FilterUtils.calcConstraintKey(structuredEventValue_.header.fixed_header.event_type.domain_name,
					     structuredEventValue_.header.fixed_header.event_type.type_name);
    }
    
    public EvaluationResult hasDefault(EvaluationContext evaluationContext,
				       ComponentName op) 
	throws EvaluationException {

	try {
	    EvaluationResult _er = evaluate(evaluationContext, op);
	    Any _any = _er.getAny();
	    
	    if (evaluationContext.getDynamicEvaluator().hasDefaultDiscriminator(_any)) {
		return EvaluationResult.BOOL_TRUE;
	    } else {
		return EvaluationResult.BOOL_FALSE;
	    }
	} catch (BadKind bk) {
	    throw NotificationEventUtils.getException(bk);
	}
    }

    static void reThrowException(Exception e) throws EvaluationException {
	logger_.error("rethrow Exception:" , e);

	throw new EvaluationException(e.getMessage());
    }
}
