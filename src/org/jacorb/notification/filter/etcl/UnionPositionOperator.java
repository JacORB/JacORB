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
 * UnionPositionOperator.java
 *
 *
 * Created: Thu Sep 26 14:57:58 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class UnionPositionOperator extends AbstractTCLNode
{

    int position_;
    boolean default_ = false;

    public UnionPositionOperator( Token token )
    {
        super( token );

        setName( "UnionPos" );
        setType( UNION_POS );
    }

    void setPosition( Double pos )
    {
        position_ = pos.intValue();
        default_ = false;
    }

    void setDefault()
    {
        default_ = true;
    }

    public boolean isDefault()
    {
        return default_;
    }

    public int getPosition()
    {
        return position_;
    }

    public String toString()
    {
        return "(" + ( default_ ? "default" : "" + position_ ) + ")";
    }

    public void acceptPreOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitUnionPosition( this );

        if ( hasNextSibling() )
        {
            ( ( AbstractTCLNode ) getNextSibling() ).acceptPreOrder( visitor );
        }
    }

    public void acceptPostOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        if ( hasNextSibling() )
        {
            ( ( AbstractTCLNode ) getNextSibling() ).acceptPostOrder( visitor );
        }

        visitor.visitUnionPosition( this );
    }

    public void acceptInOrder( AbstractTCLVisitor visitor ) throws VisitorException
    {
        visitor.visitUnionPosition( this );

        if ( hasNextSibling() )
        {
            ( ( AbstractTCLNode ) getNextSibling() ).acceptInOrder( visitor );
        }
    }

} // UnionPositionOperator
