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

public class StringValue extends TCLNode {

    String value_;

    public StringValue(Token tok) {
	super(tok);
	setKind(TCKind.tk_string);
	value_ = tok.getText();
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }

    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }

    public String toString() {
	return "'" + value_ + "'";
    }

    public EvaluationResult evaluate(EvaluationContext context) throws DynamicTypeException,
	       InvalidValue,
	       TypeMismatch,
	       InconsistentTypeCode {
	EvaluationResult _res = new EvaluationResult();
	_res.setString(value_);

	return _res;
    }

    public boolean isNumber() {
	return (value_.length() == 1);
    }

    public boolean isStatic() {
	return true;
    }

    public boolean isString() {
	return true;
    }

    public String getName() {
	return "StringValue";
    }

    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitString(this);
    }
}
