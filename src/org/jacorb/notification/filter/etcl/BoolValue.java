package org.jacorb.notification.filter.etcl;

/*
 * JacORB - a free Java ORB
 * 
 * Copyright (C) 1999-2004 Gerald Brose
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Library General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 *  
 */

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

public class BoolValue extends AbstractTCLNode
{

    boolean value_;

    public BoolValue(Token tok)
    {
        super(tok);
        value_ = tok.getText().equals("TRUE");
        setName("BoolValue");
    }

    public EvaluationResult evaluate(EvaluationContext context) throws EvaluationException
    {

        if (value_)
        {
            return EvaluationResult.BOOL_TRUE;
        }

        return EvaluationResult.BOOL_FALSE;
    }

    public boolean isStatic()
    {
        return true;
    }

    public boolean isBoolean()
    {
        return true;
    }

    public String toString()
    {
        return "" + value_;
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitBool(this);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitBool(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitBool(this);
    }
}