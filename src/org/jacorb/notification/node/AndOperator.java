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

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;

/** A simple node to represent AND operation */

public class AndOperator extends AbstractTCLNode {

    public AndOperator(Token tok) {
        super(tok);
        setKind(TCKind.tk_boolean);
    }

    public String toString() {
        return "and";
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws DynamicTypeException,
               EvaluationException {

        boolean _l, _r;

        _l = left().evaluate(context).getBool();

        if (!_l) {
            return EvaluationResult.BOOL_FALSE;
        }

        _r = right().evaluate(context).getBool();

        return (_r ? EvaluationResult.BOOL_TRUE : EvaluationResult.BOOL_FALSE);
    }

    public boolean isStatic() {
        return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitor.visitAnd(this);
        right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitAnd(this);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitor.visitAnd(this);
    }

    public String getName() {
        return NAME;
    }

    static final String NAME = "AndOperator";
}
