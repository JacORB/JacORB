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
package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.NotificationEvent;

/** A simple node to represent COMPONENT operation */
public class ComponentOperator extends TCLNode {

    String value_;

    public ComponentOperator(Token tok) {
	super(tok);
	setName("ComponentOperator");
	value_ = tok.getText();
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       TypeMismatch,
	       InconsistentTypeCode,
	       InvalidValue,
	       EvaluationException {

	NotificationEvent _event = context.getEvent();

	EvaluationResult _r = _event.evaluate(this);

	debug("return " + _r);
	debug(_r.getClass().getName());

	return _r;
    }

    public String toString() {
	return value_;
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getFirstChild()).acceptPostOrder(visitor);
	visitor.visitComponent(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitComponent(this);
	((TCLNode)getFirstChild()).acceptPreOrder(visitor);
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	((TCLNode)getFirstChild()).acceptInOrder(visitor);
	visitor.visitComponent(this);
    }

}
