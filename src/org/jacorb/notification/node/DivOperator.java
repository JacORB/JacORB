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
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.Token;

/**
 * A simple node to represent DIV operation
 * @version $Id$
 */

public class DivOperator extends AbstractTCLNode {

    public DivOperator(Token tok) {
        super(tok);
    }

    public String toString() {
        return " /";
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws DynamicTypeException,
               EvaluationException {


        try {
            return EvaluationResult.div(left().evaluate(context),
                                        right().evaluate(context));
        } catch (ArithmeticException e) {
            throw new EvaluationException(e.getMessage());
        }

    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitor.visitDiv(this);
        right().acceptInOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitor.visitDiv(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitor.visitDiv(this);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }

    public String getName() {
        return "DivOperator";
    }
}
