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
import org.omg.CORBA.Any;
import org.apache.log4j.Logger;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventHeader;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.ComponentOperator;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.IdentValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.node.DotOperator;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.notification.node.ComponentPositionOperator;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.AssocOperator;
import org.jacorb.notification.node.ImplicitOperator;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

class NotificationAnyEvent extends NotificationEvent {

    static String sAnyKey = FilterUtils.calcConstraintKey("", "%ANY");
    Any anyValue_;
    StructuredEvent structuredEventValue_;

    NotificationAnyEvent(ApplicationContext appContext,
			 Logger logger) {
	super(appContext, logger);
    }

    public void setAny(Any any) {
	anyValue_ = any;
    }

    public void reset() {
	super.reset();
	anyValue_ = null;
	structuredEventValue_ = null;
    }

    public EventTypeIdentifier getEventTypeIdentifier() {
	return null;
    }

    public int getType() {
	return TYPE_ANY;
    }

    public Any toAny() {
	return anyValue_;
    }

    public StructuredEvent toStructuredEvent() {
	if(structuredEventValue_ == null) {
	    synchronized(this) {
		if (structuredEventValue_ == null) {
		    structuredEventValue_ = new StructuredEvent();
		    EventType _type = new EventType("", "%ANY");
		    FixedEventHeader _fixed = new FixedEventHeader(_type, "");
		    Property[] _variable = new Property[0];
		    structuredEventValue_.header = new EventHeader(_fixed, _variable);
		    structuredEventValue_.filterable_data = new Property[0];
		    structuredEventValue_.remainder_of_body = anyValue_;
		}
	    }
	}
	return structuredEventValue_;
    }

    public String getConstraintKey() {
	return sAnyKey;
    }

    public EvaluationResult testExists(ComponentOperator op) throws EvaluationException {
	try {
	    evaluate(op);
	    return EvaluationResult.BOOL_TRUE;
	} catch (EvaluationException e) {
	    return EvaluationResult.BOOL_FALSE;
	}
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

    public EvaluationResult evaluate(ComponentOperator op) 
	throws EvaluationException {

	try{
	    TCLNode _left = (TCLNode)op.left();
	    Any _res;
	    EvaluationResult _ret = null;

	    switch (_left.getType()) {
	    case TCLNode.IDENTIFIER:
		IdentValue _iv = (IdentValue)_left;

		_ret = NotificationEventUtils.evaluateShorthand(evaluationContext_, anyValue_, op, _iv);
		
		break;
	    case TCLNode.DOT:
		_ret = NotificationEventUtils.evaluateComponent(evaluationContext_, 
								anyValue_, 
								op);

		break;
	    default:
		logger_.debug("unknown left: " + _left.getClass().getName());
		throw new RuntimeException("not implemented yet");
	    }
	    return _ret;
	} catch (TypeMismatch tm) {
	    throw NotificationEventUtils.getException(tm);
	} catch (InconsistentTypeCode itc) {
	    throw NotificationEventUtils.getException(itc);
	} catch (InvalidValue iv) {
	    throw NotificationEventUtils.getException(iv);
	}
    }

}
