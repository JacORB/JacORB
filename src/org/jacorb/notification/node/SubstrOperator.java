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
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;

public class SubstrOperator extends TCLNode {

    static final String NAME = "SubstrOperator";

    String value_;

    public String getName() {
	return NAME;
    }

    public SubstrOperator(Token tok) {
	super(tok);
	value_ = tok.getText();
    }


    public String toString() {
	return " ~";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode, EvaluationException {
	EvaluationResult _res;

	String _l, _r;

	_l = left().evaluate(context).getString();
	_r = right().evaluate(context).getString();

	int _idx = _r.indexOf(_l);
	if (_idx == -1) {
	    _res = EvaluationResult.BOOL_FALSE;
	} else {
	    _res = EvaluationResult.BOOL_TRUE;
	}

	return _res;
    }

    public boolean isStatic() {
	return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptInOrder(visitor);
	visitor.visitSubstr(this);
	right().acceptInOrder(visitor);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	left().acceptPostOrder(visitor);
	right().acceptPostOrder(visitor);
	visitor.visitSubstr(this);
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitSubstr(this);
	left().acceptPreOrder(visitor);
	right().acceptPreOrder(visitor);
    }
}
