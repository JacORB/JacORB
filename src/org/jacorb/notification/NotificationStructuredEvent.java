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
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.CosNotification.StructuredEvent;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEventHelper;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.ComponentOperator;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.DotOperator;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.notification.node.ComponentPositionOperator;
import org.jacorb.notification.node.ImplicitOperator;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.jacorb.notification.node.AssocOperator;
import org.omg.CORBA.TypeCodePackage.BadKind;

class NotificationStructuredEvent extends NotificationEvent {

    NotificationStructuredEvent(ApplicationContext appContext,
				Logger logger) {
	super(appContext, logger);
	anyValue_ = applicationContext_.getOrb().create_any();
    }

    Any anyValue_;
    StructuredEvent structuredEventValue_;

    public void setStructuredEventValue(StructuredEvent event) {
	structuredEventValue_ = event;
	StructuredEventHelper.insert(anyValue_, structuredEventValue_);
    }

    public void reset() {
	super.reset();
	anyValue_ = applicationContext_.getOrb().create_any();
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

    public EvaluationResult evaluate(ComponentOperator op) throws EvaluationException {
	logger_.debug("evaluate()");
	try {
	    logger_.debug("evaluate(" + op + ")");
	    TCLNode _left = (TCLNode)op.left();
	    EvaluationResult _ret = null;

	    switch(_left.getType()) {
	    case TCLNode.IDENTIFIER:
		IdentValue _iv = (IdentValue)_left;
		
		_ret = NotificationEventUtils.evaluateShorthand(evaluationContext_, anyValue_, op, _iv);

		break;
	    case TCLNode.DOT:
		DotOperator _dot = (DotOperator)_left;
		_ret = NotificationEventUtils.evaluateComponent(evaluationContext_, anyValue_, op);
		break;
	    default:
		logger_.debug("Unexpected Node: " + _left.getClass().getName());
		throw new RuntimeException();
	    }
	    return _ret;
	} catch (TypeMismatch tm) {
	    reThrowException(tm);
	} catch (InconsistentTypeCode itc) {
	    reThrowException(itc);
	} catch (InvalidValue iv) {
	    reThrowException(iv);
	}
	return null;
    }

    public EvaluationResult testExists(ComponentOperator op) {
	try {
	    evaluate(op);
	    return EvaluationResult.BOOL_TRUE;
	} catch (EvaluationException e) {
	    return EvaluationResult.BOOL_FALSE;
	}
    }

    public String getConstraintKey() {
	return FilterUtils.calcConstraintKey(structuredEventValue_.header.fixed_header.event_type.domain_name,
					     structuredEventValue_.header.fixed_header.event_type.type_name);
    }
    
    public EvaluationResult hasDefault(ComponentOperator op) throws EvaluationException {
	try {
	    EvaluationResult _er = evaluate(op);
	    Any _any = _er.getAny();
	    
	    if (evaluationContext_.getDynamicEvaluator().hasDefaultDiscriminator(_any)) {
		return EvaluationResult.BOOL_TRUE;
	    } else {
		return EvaluationResult.BOOL_FALSE;
	    }
	} catch (BadKind bk) {
	    throw NotificationEventUtils.getException(bk);
	}
    }

    static void reThrowException(Exception e) throws EvaluationException {
	e.printStackTrace();
	throw new EvaluationException(e.getMessage());
    }
}
