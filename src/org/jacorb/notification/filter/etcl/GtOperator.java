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
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

/**
 * A node to represent GT (>) operation
 */
public class GtOperator extends AbstractTCLNode
{
    public GtOperator(Token tok)
    {
        super(tok);
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws EvaluationException
    {
        EvaluationResult _left, _right;

        _left = left().evaluate(context);
        _right = right().evaluate(context);

        int _comp = _left.compareTo(_right);

        if (_comp == -1 || _comp == 0)
        {
            return EvaluationResult.BOOL_FALSE;
        }
        return EvaluationResult.BOOL_TRUE;
    }

    public String toString()
    {
        return ">";
    }

    public String getName()
    {
        return "GtOperator";
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptInOrder(visitor);
        visitor.visitGt(this);
        right().acceptInOrder(visitor);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitGt(this);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitor.visitGt(this);
    }
}
