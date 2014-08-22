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
package org.jacorb.notification.filter.etcl;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

/** A simple node to represent LT operation */
public class LtOperator extends BinaryOperator
{
    public LtOperator(Token tok)
    {
        super(tok);
        setName("LtOperator");
    }

    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left,
            EvaluationResult rightVal) throws EvaluationException
    {
        int _comp = left.compareTo(rightVal);

        return (_comp >= 0) ? EvaluationResult.BOOL_FALSE : EvaluationResult.BOOL_TRUE;
    }

    public String toString()
    {
        return "<";
    }

    protected void visitThis(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitLt(this);
    }
}