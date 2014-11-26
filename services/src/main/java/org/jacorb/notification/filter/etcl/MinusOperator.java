package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

/**
 * A simple node to represent MINUS operation
 */

public class MinusOperator extends UnaryOperator {

    private boolean unary_;
    private static final String NAME = "MinusOperator";

    public MinusOperator(Token tok) {
        super(tok);
        setName(NAME);
    }

    public void setType(int type) {
        unary_ = (type == UNARY_MINUS);
    }

    public String toString() {
        return " -";
    }

    public EvaluationResult evaluate(EvaluationContext context, 
            EvaluationResult left)
        throws EvaluationException {

        if (unary_) {

            return EvaluationResult.unaryMinus(left);

        } 
            return EvaluationResult.minus(left,
                                          right().evaluate(context));
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
