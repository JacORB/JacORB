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
import org.jacorb.notification.filter.RuntimeVariable;

import antlr.Token;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class RuntimeVariableNode
    extends AbstractTCLNode
    implements RuntimeVariable
{
    private String value_;
    private AbstractTCLNode strategy_;

    public RuntimeVariableNode( Token token )
    {
        super(token);

        value_ = token.getText();

        strategy_ = newStrategy(value_);
    }

    public EvaluationResult evaluate(EvaluationContext context)
        throws EvaluationException
    {
        return strategy_.evaluate(context);
    }

    private AbstractTCLNode newStrategy(String variable) {
        if (DomainNameShorthandNode.SHORT_NAME.equals(variable)) {
            return new DomainNameShorthandNode();
        } else if (TypeNameShorthandNode.SHORT_NAME.equals(variable)) {
            return new TypeNameShorthandNode();
        } else if (EventNameShorthandNode.SHORT_NAME.equals(variable)) {
            return new EventNameShorthandNode();
        } else if (CurrentTimeNode.SHORT_NAME.equals(variable)) {
            return new CurrentTimeNode();
        } else {
            return new PropertyShorthandNode(variable);
        }
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        if (getFirstChild() != null) {
            ( ( AbstractTCLNode ) getFirstChild() ).acceptPostOrder( visitor );
        }
        visitor.visitRuntimeVariable( this );
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitRuntimeVariable( this );

        if (getFirstChild() != null) {
            ( ( AbstractTCLNode ) getFirstChild() ).acceptPreOrder( visitor );
        }
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        if (getFirstChild() != null) {
            ( ( AbstractTCLNode ) getFirstChild() ).acceptInOrder( visitor );
        }

        visitor.visitRuntimeVariable(this);
    }

    public String toString() {
        return value_;
    }

    public String getIdentifier() {
        return value_;
    }

}
