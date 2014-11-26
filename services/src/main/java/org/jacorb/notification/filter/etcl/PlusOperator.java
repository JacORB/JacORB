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
 * A simple node to represent PLUS operation
 */

public class PlusOperator extends UnaryOperator
{
    private boolean isUnaryOperator_;

    public PlusOperator(Token tok)
    {
        super(tok);
        setName("PlusOperator");
    }

    public void setType(int type)
    {
        isUnaryOperator_ = (type == UNARY_PLUS);
    }

    public String toString()
    {
        return " +";
    }

    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left)
            throws EvaluationException
    {
        if (isUnaryOperator_)
        {
            return left;
        }

        return EvaluationResult.plus(left, right().evaluate(context));
    }

    public boolean isStatic()
    {
        return (left().isStatic() && right().isStatic());
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptInOrder(visitor);
        visitor.visitPlus(this);
        if (!isUnaryOperator_)
        {
            right().acceptInOrder(visitor);
        }
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptPostOrder(visitor);
        if (!isUnaryOperator_)
        {
            right().acceptPostOrder(visitor);
        }
        visitor.visitPlus(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitPlus(this);
        left().acceptPreOrder(visitor);
        if (!isUnaryOperator_)
        {
            right().acceptPreOrder(visitor);
        }
    }
}