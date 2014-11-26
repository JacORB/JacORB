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

package org.jacorb.notification.filter.etcl;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

/**
 * @author Alphonse Bendt
 */
public abstract class BinaryOperator extends UnaryOperator
{
    public BinaryOperator(Token tok)
    {
        super(tok);
    }
 
    protected final EvaluationResult evaluate(EvaluationContext context, 
            EvaluationResult left)
            throws EvaluationException
    {
        EvaluationResult _right = right().evaluate(context);
        
        return evaluate(context, left, _right);
    }
    
    protected abstract EvaluationResult evaluate(EvaluationContext context, 
            EvaluationResult left, EvaluationResult rightNode) throws EvaluationException;

    public final void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptInOrder(visitor);
        visitThis(visitor);
        right().acceptInOrder(visitor);
    }

    public final void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException {
        left().acceptPostOrder(visitor);
        right().acceptPostOrder(visitor);
        visitThis(visitor);
    }

    public final void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException {
        visitThis(visitor);
        left().acceptPreOrder(visitor);
        right().acceptPreOrder(visitor);
    }
    
    protected abstract void visitThis(AbstractTCLVisitor visitor) throws VisitorException;
}
