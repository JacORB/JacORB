package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationResult;
import org.omg.CORBA.TCKind;

import antlr.Token;

public class StringValue extends AbstractTCLNode {

    String value_;
    EvaluationResult result_;

    public StringValue(Token tok) {
        super(tok);
        setKind(TCKind.tk_string);
        value_ = tok.getText();
        EvaluationResult _result = new EvaluationResult();
        _result.setString(value_);
        result_ = EvaluationResult.wrapImmutable(_result);
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitString(this);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitString(this);
    }

    public String toString() {
        return "'" + value_ + "'";
    }

    public EvaluationResult evaluate(EvaluationContext context)
         {

        return result_;
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

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitString(this);
    }
}
