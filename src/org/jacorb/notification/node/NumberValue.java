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
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;

/**
 * A simple node to represent a Number
 * @version $Id$
 */

public class NumberValue extends AbstractTCLNode {

    private Double  number_;
    EvaluationResult result_;

    public Double getNumber() {
        return number_;
    }

    public String getName() {
        return getClass().getName();
    }

    public NumberValue(Token tok) {
        super(tok);

        EvaluationResult _r = new EvaluationResult();
        number_ = new Double(tok.getText());

        int t = getType();

        switch(t) {
        case NUMBER:
            _r.setLong(number_);
            break;
        case NUM_FLOAT:
            _r.setFloat(number_);
            break;
        default:
            throw new RuntimeException();
        }

        result_ = EvaluationResult.wrapImmutable(_r);
    }

    public EvaluationResult evaluate(EvaluationContext context) {

        return result_;
    }

    public String toString() {
        switch(getType()) {
        case NUM_FLOAT:
            return "" + number_.floatValue();
        default:
            return "" + number_.longValue();
        }
    }

    public boolean isStatic() {
        return true;
    }

    public boolean isNumber() {
        return true;
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitNumber(this);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitNumber(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitNumber(this);
    }
}
