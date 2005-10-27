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
package org.jacorb.notification.filter.etcl;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

/** A simple node to represent NEQ operation */
public class NeqOperator extends BinaryOperator
{
    public NeqOperator(Token tok)
    {
        super(tok);
        setName("NeqOperator");
    }

    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left,
            EvaluationResult rightResult) throws EvaluationException
    {
        int _comp = left.compareTo(rightResult);

        return (_comp == 0) ? EvaluationResult.BOOL_FALSE : EvaluationResult.BOOL_TRUE;
    }

    public String toString()
    {
        return "!=";
    }

    protected void visitThis(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitNeq(this);
    }
}