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
package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

/** A simple node to represent EQ operation */
public class EqOperator extends TCLNode {

    public EqOperator(Token tok) {
	super(tok);
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode,
	       EvaluationException {

	EvaluationResult _left = left().evaluate(context);
	EvaluationResult _right = right().evaluate(context);

	if (_left.compareTo(_right) == 0) {
	    return EvaluationResult.BOOL_TRUE;
	}
	return EvaluationResult.BOOL_FALSE;

    }

    public String toString() {
	return "==";
    }

    public String getName() {
	return getClass().getName();
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	debug("accept visitor");

	left().acceptInOrder(visitor);
	visitor.visitEq(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitEq(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitEq(this);
    }

}
