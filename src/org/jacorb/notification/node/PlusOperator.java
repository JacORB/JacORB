package org.jacorb.notification.node;

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

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.*;
import org.omg.CORBA.TCKind;
import org.jacorb.notification.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

/**
 * A simple node to represent PLUS operation 
 */

public class PlusOperator extends TCLNode {
    boolean unary_;

    public PlusOperator(Token tok) {
	super(tok);
    }

    public void setType(int type) {
	unary_ = type == UNARY_PLUS;
    }

    public String getName() {
	return "PlusOperator";
    }

    public String toString() {
	return " +";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode, EvaluationException  {

	EvaluationResult _left = left().evaluate(context);

	if (unary_) {
	    return _left;
	}

	return EvaluationResult.plus(_left, right().evaluate(context));

    }

    public boolean isStatic() {
	return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitPlus(this);
	if (!unary_) {
	    right().acceptInOrder(visitor);
	}
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	if (!unary_) {
	    right().acceptPostOrder(visitor);
	}
	visitor.visitPlus(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitPlus(this);
	left().acceptPreOrder(visitor);
	if (!unary_) {
	    right().acceptPreOrder(visitor);
	}
    }
}
