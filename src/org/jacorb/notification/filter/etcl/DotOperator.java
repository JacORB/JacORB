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

import antlr.Token;

/**
 * A simple node to represent DOT
 */

public class DotOperator extends AbstractTCLNode
{
    public DotOperator( Token tok )
    {
        super( tok );
        setName( "DotOperator" );
    }

    public String toString()
    {
        return ".";
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        ( ( AbstractTCLNode ) getNextSibling() ).acceptPostOrder( visitor );
        visitor.visitDot( this );
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        ( ( AbstractTCLNode ) getNextSibling() ).acceptInOrder( visitor );
        visitor.visitDot( this );
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitDot( this );
        ( ( AbstractTCLNode ) getNextSibling() ).acceptPreOrder( visitor );
    }
}
