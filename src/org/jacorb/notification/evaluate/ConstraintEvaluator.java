/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.notification.evaluate;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import java.io.StringReader;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.StaticTypeChecker;
import org.jacorb.notification.node.StaticTypeException;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.node.TCLLexer;
import org.jacorb.notification.node.TCLNode;
import org.jacorb.notification.node.TCLParser;
import org.jacorb.notification.NotificationEvent;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.apache.log4j.Logger;

/**
 * ConstraintEvaluator.java
 *
 *
 * Created: Wed Sep 11 14:04:22 2002
 *
 * @author <a href="mailto:a.bendt@berlin.de">Alphonse Bendt</a>
 * @version
 */

public class ConstraintEvaluator {

    ORB orb_;
    public String constraint_;
    TCLNode rootNode_;

    protected Logger logger_ = Logger.getLogger("EVALUATE");

    public static TCLNode parse(String expr) throws RecognitionException, TokenStreamException {
	TCLLexer _lexer = new TCLLexer(new StringReader(expr));
	TCLParser _parser = new TCLParser(_lexer);
	_parser.startRule();

	return (TCLNode)_parser.getAST();
    }


    public ConstraintEvaluator(ORB orb, TCLNode root) {
	orb_ = orb;
	rootNode_ = root;
    }

    public ConstraintEvaluator(ORB orb, ConstraintExp constraintExp) 
	throws InvalidConstraint {

	try {
	    constraint_ = constraintExp.constraint_expr;
	    orb_ = orb;
	    rootNode_ = parse(constraintExp.constraint_expr);
	    TCLCleanUp _cleanUp = new TCLCleanUp();
	    _cleanUp.fix(rootNode_);
	    StaticTypeChecker _checker = new StaticTypeChecker();
	    _checker.check(rootNode_);
	    return;
	} catch (StaticTypeException ste) {
	    throw new InvalidConstraint(ste.getMessage(), constraintExp);
	} catch (TokenStreamException tse) {
	} catch (RecognitionException re) {
	}
	throw new InvalidConstraint(constraintExp);
    }

    public EvaluationResult evaluate(NotificationEvent event, 
				     EvaluationContext context) throws DynamicTypeException,
								       InvalidValue, 
								       TypeMismatch,
								       InconsistentTypeCode,
								       EvaluationException,
								       InvalidName {

	debug("evaluate");
	debug("root is a " + rootNode_.getClass().getName());
	context.setEvent(event);

	EvaluationResult _res = rootNode_.evaluate(context);

	debug("result " + _res);
	
	return _res;
    }

    static boolean DEBUG = true; 

    void debug(String msg) {
	logger_.debug(msg);
    }

}// ConstraintEvaluator
