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
import org.jacorb.notification.filter.EvaluationResult;

import antlr.Token;

public class IdentValue extends AbstractTCLNode
{
    private final String value_;
    private final EvaluationResult evalValue_;

    public IdentValue( Token tok )
    {
        super( tok );
        
        value_ = tok.getText();
        setName( "IdentValue" );
        EvaluationResult _result = new EvaluationResult();
        _result.setString( value_ );
        evalValue_ = EvaluationResult.wrapImmutable( _result );
    }

    public EvaluationResult evaluate( EvaluationContext c )
    {
        return evalValue_;
    }

    public String getIdentifier()
    {
        return value_;
    }

    public String toString()
    {
        return value_;
    }

    public boolean isStatic()
    {
        return true;
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitIdent( this );
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitIdent( this );
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitIdent( this );
    }
}
