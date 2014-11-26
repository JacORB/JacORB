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

/** A simple node to represent NOT operation */
public class NotOperator extends UnaryOperator
{
    public NotOperator(Token tok)
    {
        super(tok);
        setName(NAME);
    }

    public String toString()
    {
        return " not";
    }
    
    public EvaluationResult evaluate(EvaluationContext context, EvaluationResult left)
            throws EvaluationException
    {

        boolean _b = left.getBool();

        return (_b ? EvaluationResult.BOOL_FALSE : EvaluationResult.BOOL_TRUE);
    }

    static final String NAME = "NotOperator";

    

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptInOrder(visitor);
        visitor.visitNot(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitNot(this);
        left().acceptPreOrder(visitor);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        left().acceptInOrder(visitor);
        visitor.visitNot(this);
    }
}