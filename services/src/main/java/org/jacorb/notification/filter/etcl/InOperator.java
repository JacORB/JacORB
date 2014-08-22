package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.omg.CORBA.Any;

import antlr.Token;

/** A simple node to represent IN operation */
public class InOperator extends BinaryOperator
{
    public InOperator(Token tok)
    {
        super(tok);
        setName("InOperator");
    }

    public String toString()
    {
        return " in";
    }

    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left,
            EvaluationResult rightNode) throws EvaluationException
    {
        Any _any = rightNode.getAny();
        ETCLEvaluator _evaluator = context.getETCLEvaluator();

        return _evaluator.evaluateElementInSequence(context, left, _any);
    }

    protected void visitThis(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitIn(this);
    }
}