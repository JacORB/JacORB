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
import org.jacorb.notification.filter.EvaluationException;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;
import org.jacorb.notification.filter.EvaluationResult;

/**
 * A simple node to represent MINUS operation
 * @version $Id$
 */

public class MinusOperator extends AbstractTCLNode {

    boolean unary_;
    static final String NAME = "MinusOperator";

    public String getName() {
        return NAME;
    }

    public MinusOperator(Token tok) {
        super(tok);
    }

    public void setType(int type) {
        unary_ = type == UNARY_MINUS;
    }

    public String toString() {
        return " -";
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws EvaluationException {

        if (unary_) {

            return EvaluationResult.unaryMinus(left().evaluate(context));

        } else {

            return EvaluationResult.minus(left().evaluate(context),
                                          right().evaluate(context));
        }
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitor.visitMinus(this);
        if (!unary_) {
            right().acceptInOrder(visitor);
        }
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        if (!unary_) {
            right().acceptPostOrder(visitor);
        }
        visitor.visitMinus(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitMinus(this);
        left().acceptPreOrder(visitor);
        if (!unary_) {
            right().acceptPreOrder(visitor);
        }
    }

}
