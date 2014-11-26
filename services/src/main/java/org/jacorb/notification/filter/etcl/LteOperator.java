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

/** A simple node to represent LTE operation */
public class LteOperator extends BinaryOperator
{
    public LteOperator(Token tok)
    {
        super(tok);
        setName(NAME);
    }

    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left,
            EvaluationResult rightNode) throws EvaluationException
    {
        int _comp = left.compareTo(rightNode);

        return (_comp > 0) ? EvaluationResult.BOOL_FALSE : EvaluationResult.BOOL_TRUE;
    }

    public String toString()
    {
        return "<=";
    }

    static final String NAME = "LteOperator";

    protected void visitThis(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitLte(this);
    }
}