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
package org.jacorb.notification;

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

    NotificationAnyEvent(ORB orb, ResultExtractor extractor, DynamicEvaluator evaluator, Any event, Logger logger) {
	super(orb, extractor, evaluator, logger);
	anyValue_ = event;
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
	    
	    if (dynamicEvaluator_.hasDefaultDiscriminator(_any)) {
		return EvaluationResult.BOOL_TRUE;
	    } else {
		return EvaluationResult.BOOL_FALSE;
	    }
	} catch (BadKind bk) {
	    throw getException(bk);
	}
    }

    public EvaluationResult evaluate(ComponentOperator op) throws EvaluationException {
	try{
	    TCLNode _left = (TCLNode)op.left();
	    Any _res;
	    EvaluationResult _ret = null;

	    switch (_left.getType()) {
	    case TCLNode.IDENTIFIER:
		IdentValue _iv = (IdentValue)_left;
		//	    _res = _evaluator.evaluateIdentifier(_linkedValue, _iv.getIdentifier());
		//_ret = context.getResultExtractor().extractFromAny(_res);
		break;
	    case TCLNode.DOT:
		DotOperator _dot = (DotOperator)_left;
		_ret = evaluateDot(_dot);
		//context.linkNodeToValue(_dot, _linkedValue);
		//_ret = _dot.evaluate(context);
		break;
	    default:
		debug("unknown left: " + _left.getClass().getName());
		throw new RuntimeException("not implemented yet");
	    }
	    return _ret;
	} catch (TypeMismatch tm) {
	    throw getException(tm);
	} catch (InconsistentTypeCode itc) {
	    throw getException(itc);
	} catch (InvalidValue iv) {
	    throw getException(iv);
	}
    }

    EvaluationResult evaluateDot(DotOperator dot) 
	throws InconsistentTypeCode, 
	       TypeMismatch, 
	       EvaluationException, 
	       InvalidValue {

        TCLNode _operator = dot;
        Any _valueCursor = anyValue_;
        Any _result = null;
        EvaluationResult _ret = null;

	String _path = "$" + dot.toString();

        while (_operator.hasNextSibling()) {
            _operator = (TCLNode)_operator.getNextSibling();
	    
	    _path += _operator.toString();

	    debug("current path: " + _path);
	    debug("next operator is:" + _operator.toString());

            switch (_operator.getType()) {
	    case TCLNode.DOT:
		// skip
		debug("skip");
		break;
            case TCLNode.UNION_POS:
                debug("eval union by pos");
                UnionPositionOperator _upo = (UnionPositionOperator)_operator;
                if (_upo.isDefault()) {
                    _result = dynamicEvaluator_.evaluateUnion(_valueCursor);
                } else {
                    _result = dynamicEvaluator_.evaluateUnion(_valueCursor, _upo.getPosition());
                }
                break;
            case TCLNode.IDENTIFIER:
                debug("eval by identifier");
                _result = dynamicEvaluator_.evaluateIdentifier(_valueCursor, ((IdentValue)_operator).getIdentifier());
                break;
            case TCLNode.COMP_POS:
                debug("evaluateByPosition()");
                _result = dynamicEvaluator_.evaluateIdentifier(_valueCursor, ((ComponentPositionOperator)_operator).getPosition());
                break;
            case TCLNode.IMPLICIT:
                debug("evaluate implicit operator");
		ImplicitOperator _op = ((ImplicitOperatorNode)_operator).getOperator();
		debug("Operator is " + _op);
		_result = evaluateImplicitOperator(dynamicEvaluator_, _op, _valueCursor);
		_ret = resultExtractor_.extractFromAny(_result);
		_ret.addAny(_valueCursor);
		debug("result is " + _result);

		return _ret;
	    case TCLNode.ARRAY:
		_result = dynamicEvaluator_.evaluateArrayIndex(_valueCursor, ((ArrayOperator)_operator).getArrayIndex());
		break;
	    case TCLNode.ASSOC:
		_result = dynamicEvaluator_.evaluateNamedValueList(_valueCursor, ((AssocOperator)_operator).getAssocName());
		break;
            default:
		debug("unknown: " + _operator);
                throw new RuntimeException();
            }
            _valueCursor = _result;
        }
        _ret = resultExtractor_.extractFromAny(_result);
	debug("extracted: "  + _ret);

        return _ret;
    }

    static EvaluationException getException() {
        return new EvaluationException();
    }

    static EvaluationException getException(Exception e) {
        e.printStackTrace();
        return new EvaluationException();
    }


    static Any evaluateImplicitOperator(DynamicEvaluator evaluator, 
					ImplicitOperator op, 
					Any value) throws EvaluationException {

	try {
	    if (op == ImplicitOperatorNode.OPERATOR_DISCRIM) {
		return evaluator.evaluateDiscriminator(value);
	    } else if (op == ImplicitOperatorNode.OPERATOR_TYPE_ID) {
		return evaluator.evaluateTypeName(value);
	    } else if (op == ImplicitOperatorNode.OPERATOR_REPO_ID) {
		return evaluator.evaluateRepositoryId(value);
	    } else if (op == ImplicitOperatorNode.OPERATOR_LENGTH) {
		return evaluator.evaluateListLength(value);
	    } else {
		throw getException();
	    }
	} catch (BadKind bk) {
	    throw getException(bk);
	} catch (InconsistentTypeCode itc) {
	    throw getException(itc);
	}
    }
}
