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

import org.omg.CORBA.Any;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.node.ImplicitOperator;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.node.ImplicitOperatorNode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.DotOperator;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.node.TCLNode;
import org.apache.log4j.Logger;
import org.jacorb.notification.node.UnionPositionOperator;
import org.jacorb.notification.node.IdentValue;
import org.jacorb.notification.node.ComponentPositionOperator;
import org.jacorb.notification.node.ArrayOperator;
import org.jacorb.notification.node.AssocOperator;
import org.jacorb.notification.node.ComponentOperator;
import java.util.Map;
import java.util.Hashtable;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jacorb.notification.node.TCLVisitor;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.node.VisitorException;

/**
 * NotificationEventUtils.java
 *
 *
 * Created: Thu May 10 16:30:04 2001
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class NotificationEventUtils {

    static Map shortHandOperators_ = new Hashtable();
    static boolean error_;
    static boolean DEBUG = false;

    static void debug(Object o) {
	if (DEBUG) {
	    System.err.println("[NotificationEventUtils] " + o);
	}
    }

    static {
	try {
	    TCLNode _root;
	    TCLVisitor _cleaner = new TCLCleanUp();

	    _root = ConstraintEvaluator.parse("$.header.fixed_header.event_type.domain_name");
	    _root.acceptInOrder(_cleaner);
	    shortHandOperators_.put("domain_name", 
				    _root);

	    _root = ConstraintEvaluator.parse("$.header.fixed_header.event_type.type_name");
	    _root.acceptInOrder(_cleaner);
	    shortHandOperators_.put("type_name", 
				    _root);

	    _root = ConstraintEvaluator.parse("$.header.fixed_header.event_name");
	    _root.acceptInOrder(_cleaner);
	    shortHandOperators_.put("event_name", 
				    _root);

	} catch (RecognitionException re) {
	    error_ = true;
	    debug(re);
	} catch (TokenStreamException tse) {
	    error_ = true;
	    debug(tse);
	} catch (VisitorException ve) {
	    error_ = true;
	    debug(ve);
	}
    }

    static EvaluationResult evaluateShorthand(EvaluationContext evaluationContext,
					      Any event,
					      ComponentOperator comp,
					      IdentValue ident) 
	throws InconsistentTypeCode, 
	       TypeMismatch, 
	       EvaluationException, 
	       InvalidValue {

	EvaluationResult _ret = null;
	String _completePath = comp.getComponentName();
	_ret = evaluationContext.lookupResult(_completePath);

	if (_ret == null) {
	    ComponentOperator _expandedOperator = (ComponentOperator)shortHandOperators_.get(ident.getIdentifier());
	    _ret = evaluateComponent(evaluationContext, event, _expandedOperator);
	    evaluationContext.storeResult(_completePath, _ret);
	}

	return _ret;
    }

    static EvaluationResult evaluateComponent(EvaluationContext evaluationContext, 
					      Any event, 
					      ComponentOperator comp) 
	throws InconsistentTypeCode, 
	       TypeMismatch, 
	       EvaluationException, 
	       InvalidValue {

	debug("evaluate Component: " + comp.toStringTree());

        EvaluationResult _ret = null;
	String _completePath = comp.getComponentName();
	debug("complete path is: " + _completePath);
	_ret = evaluationContext.lookupResult(_completePath);
	
	if (_ret == null) {	    
	    Any _result = null;
	    TCLNode _operator = (TCLNode)comp.left();
	    Any _valueCursor = event;

	    String _path = comp.toString() + _operator.toString();

	    while (_operator.hasNextSibling()) {
		_operator = (TCLNode)_operator.getNextSibling();
	    
		_path += _operator.toString();

		debug("current path: " + _path);
		debug("next operator is:" + _operator.toString());
		
		_result = evaluationContext.lookupAny(_path);
		if (_result == null) {
		    switch (_operator.getType()) {
		    case TCLNode.DOT:
			// skip
			debug("skip");
			break;
		    case TCLNode.UNION_POS:
			debug("eval union by pos");
			UnionPositionOperator _upo = (UnionPositionOperator)_operator;
			if (_upo.isDefault()) {
			    _result = evaluationContext.getDynamicEvaluator().evaluateUnion(_valueCursor);
			} else {
			    _result = evaluationContext.getDynamicEvaluator().evaluateUnion(_valueCursor, 
											    _upo.getPosition());
			}
			break;
		    case TCLNode.IDENTIFIER:
			debug("eval by identifier");
			
			_result = evaluationContext.getDynamicEvaluator().evaluateIdentifier(_valueCursor, 
											     ((IdentValue)_operator).getIdentifier());
			break;
		    case TCLNode.COMP_POS:
			debug("evaluateByPosition()");

			_result = evaluationContext.getDynamicEvaluator().evaluateIdentifier(_valueCursor, 
											     ((ComponentPositionOperator)_operator).getPosition());
			break;
		    case TCLNode.IMPLICIT:
			debug("evaluate implicit operator");
			ImplicitOperator _op = ((ImplicitOperatorNode)_operator).getOperator();
			
			debug("Operator is " + _op);
			debug("Cursor: " + _valueCursor);
			debug("DynamicEvaluator: " + evaluationContext.getDynamicEvaluator());
			
			_result = NotificationEventUtils.evaluateImplicitOperator(evaluationContext.getDynamicEvaluator(), 
										  _op, 
										  _valueCursor);
			
			_ret = evaluationContext.getResultExtractor().extractFromAny(_result);
			_ret.addAny(_valueCursor);
			debug("result is " + _result);
			
			return _ret;
		    case TCLNode.ARRAY:
			_result = evaluationContext.getDynamicEvaluator().evaluateArrayIndex(_valueCursor, 
											     ((ArrayOperator)_operator).getArrayIndex());
			break;
		    case TCLNode.ASSOC:
			_result = evaluationContext.getDynamicEvaluator().evaluateNamedValueList(_valueCursor, 
												 ((AssocOperator)_operator).getAssocName());
			break;
		    default:
			debug("unknown: " + _operator);
			throw new RuntimeException();
		    }
		} else {
		    debug("Any Cache HIT");
		}
		if (_result != null) {
		    evaluationContext.storeAny(_path, _result);
		    _valueCursor = _result;
		}
	    }
	    // Create the EvaluationResult
	    _ret = evaluationContext.getResultExtractor().extractFromAny(_result);
	    debug("extracted: "  + _ret);

	    // Cache the EvaluationResult
	    if (_ret != null) {
		debug("Cache Result: " +_completePath + " => " + _ret);
		evaluationContext.storeResult(_completePath, _ret);
	    }
	} else {
	    debug("Result Cache HIT");
	}
        return _ret;
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
    
    static EvaluationException getException(Exception e) {
        e.printStackTrace();
        return new EvaluationException();
    }

    static EvaluationException getException() {
        return new EvaluationException();
    }

}// NotificationEventUtils
