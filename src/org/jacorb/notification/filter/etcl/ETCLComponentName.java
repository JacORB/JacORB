package org.jacorb.notification.filter.etcl;

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

import org.jacorb.notification.filter.ComponentName;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.interfaces.Message;

import antlr.Token;

/**
 * node that represents a COMPONENT Name
 *
 * @author Alphonse Bendt
 */

public class ETCLComponentName extends AbstractTCLNode implements ComponentName
{
    private final String value_;

    private String componentName_;
    
    ////////////////////////////////////////
    
    protected ETCLComponentName() {
        super();
        
        value_ = null;
    }

    public ETCLComponentName( Token tok )
    {
        super( tok );

        setName( "ComponentName" );
        value_ = tok.getText();
    }

    ////////////////////////////////////////

    public EvaluationResult evaluate( EvaluationContext context )
        throws EvaluationException
    {
        final EvaluationResult _result;

        Message _event = context.getCurrentMessage();

        AbstractTCLNode _left = left();

        if (_left == null) {
            // this is the case when the expression just consists of
            // '$'. $ denotes the current Message.

            return EvaluationResult.fromAny( _event.toAny() );
        }

        switch (_left.getType()) {

        case TCLParserTokenTypes.RUNTIME_VAR:
            RuntimeVariableNode _var = ( RuntimeVariableNode ) _left;

            _result = _event.extractValue( context,
                                        this,
                                        _var );

            break;

        case TCLParserTokenTypes.DOT:
            // fallthrough
        case TCLParserTokenTypes.ASSOC:
            _result = _event.extractValue(context,
                                       this );

            break;
        default:
            // coming here means, we've hit a bug
            throw new RuntimeException("Unexpected Nodetype: "
                                       + getNameForType(_left.getType()));
        }

        return _result;
    }


    public String toString()
    {
        return value_;
    }


    public void setComponentName( String name )
    {
        componentName_ = name;
    }


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
