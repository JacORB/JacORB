package org.jacorb.notification.node;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.evaluate.EvaluationException;

import antlr.Token;

/**
 * a simple node to represent COMPONENT Name
 *
 * @version $Id$
 */

public class ComponentName extends AbstractTCLNode
{

    String value_;
    String componentName_;

    protected ComponentName() {
    }

    public ComponentName( Token tok )
    {
        super( tok );
        setName( "ComponentName" );
        value_ = tok.getText();
    }

    public EvaluationResult evaluate( EvaluationContext context )
        throws EvaluationException,
               DynamicTypeException
    {
        EvaluationResult _ret;
        Message _event = context.getNotificationEvent();

        AbstractTCLNode _left = (AbstractTCLNode) left();

        if (_left == null) {
            return EvaluationResult.fromAny( _event.toAny() );
        }

        switch (_left.getType()) {

        case AbstractTCLNode.RUNTIME_VAR:
            RuntimeVariableNode _var = ( RuntimeVariableNode ) _left;

            _ret = _event.extractValue( context,
                                        this,
                                        _var );

            break;

        case AbstractTCLNode.DOT:
        case AbstractTCLNode.ASSOC:
            _ret = _event.extractValue(context,
                                       this );

            break;
        default:
            throw new RuntimeException("Unexpected Nodetype: "
                                       + AbstractTCLNode.getNameForType(_left.getType()));
        }

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "Result: " + _ret );
        }

        return _ret;
    }

    public String toString()
    {
        return value_;
    }

    public void setComponentName( String name )
    {
        componentName_ = name;
    }

    /**
     * access the complete ComponentName.
     */
    public String getComponentName()
    {
        return componentName_;
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        if (getFirstChild() != null) {
            ( ( AbstractTCLNode ) getFirstChild() ).acceptPostOrder( visitor );
        }
        visitor.visitComponent( this );
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitComponent( this );
        ( ( AbstractTCLNode ) getFirstChild() ).acceptPreOrder( visitor );
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        ( ( AbstractTCLNode ) getFirstChild() ).acceptInOrder( visitor );
        visitor.visitComponent( this );
    }
}
