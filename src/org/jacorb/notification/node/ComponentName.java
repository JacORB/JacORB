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

import antlr.Token;
import org.jacorb.notification.EvaluationContext;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.NotificationEvent;
import org.jacorb.notification.NotificationEventUtils;

/** 
 * a simple node to represent COMPONENT Name
 *
 * @version $Id$
 */

public class ComponentName extends TCLNode
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
    throws DynamicTypeException,
                TypeMismatch,
                InconsistentTypeCode,
                InvalidValue,
                EvaluationException
    {
	EvaluationResult _ret;
        NotificationEvent _event = context.getNotificationEvent();

	TCLNode _left = (TCLNode) left();

	if (_left == null) {
	    return context.getResultExtractor().extractFromAny( _event.toAny() );
	}

	switch (_left.getType()) {

	case TCLNode.RUNTIME_VAR:
	    RuntimeVariableNode _var = ( RuntimeVariableNode ) _left;
	    
	    _ret = _event.extractValue( context,
					this,
					_var );
	    
	    break;
	    
	case TCLNode.DOT:
	case TCLNode.ASSOC:
	    _ret = _event.extractValue(context,
				       this );

	    break;
	default:
	    throw new RuntimeException("Unexpected Nodetype: " 
				       + TCLNode.getNameForType(_left.getType()));
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

    public void acceptPostOrder( TCLVisitor visitor ) throws VisitorException
    {
	if (getFirstChild() != null) {
	    ( ( TCLNode ) getFirstChild() ).acceptPostOrder( visitor );
	}
        visitor.visitComponent( this );
    }

    public void acceptPreOrder( TCLVisitor visitor ) throws VisitorException
    {
        visitor.visitComponent( this );
        ( ( TCLNode ) getFirstChild() ).acceptPreOrder( visitor );
    }

    public void acceptInOrder( TCLVisitor visitor ) throws VisitorException
    {
        ( ( TCLNode ) getFirstChild() ).acceptInOrder( visitor );
        visitor.visitComponent( this );
    }
}
