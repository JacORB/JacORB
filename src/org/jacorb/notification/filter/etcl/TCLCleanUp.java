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

import antlr.collections.AST;

/**
 * Visitor for TCL Trees. Does some Restructuration of a TCL Tree.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TCLCleanUp extends AbstractTCLVisitor implements TCLParserTokenTypes
{

    public void fix( AbstractTCLNode node )
    {
        try
        {
            node.acceptPostOrder( this );
        }
        catch ( VisitorException e )
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void visitComponent( ETCLComponentName component )
        throws VisitorException
    {
        insertComponentName( component );
    }

    public void visitUnionPosition( UnionPositionOperator op )
        throws VisitorException
    {
        fixUnionPosition( op );
    }

    /**
     * insert the Complete Name of a Component in the
     * ComponentOperator node.
     */
    void insertComponentName( ETCLComponentName comp )
    {
        StringBuffer _name =
            new StringBuffer( comp.toString() );

        AbstractTCLNode _cursor =
            ( AbstractTCLNode ) comp.left();

        while ( _cursor != null )
        {
            _name.append( _cursor.toString() );
            _cursor = ( AbstractTCLNode ) _cursor.getNextSibling();
        }

        comp.setComponentName( _name.toString() );
    }

    void fixUnionPosition( UnionPositionOperator node )
    {
        AST _nextSibling = node.getNextSibling();

        if ( _nextSibling == null )
        {
            node.setDefault();
        }
        else
        {
            switch ( _nextSibling.getType() )
            {
                case NUMBER:
                    Double _position = ( ( NumberValue ) _nextSibling ).getNumber();
                    node.setPosition( _position );
                    node.setNextSibling( _nextSibling.getNextSibling() );

                    // fallthrough
                case PLUS:
                    // fallthrough
                case MINUS:
                    // fallthrough
                case STRING:
                    break;

                default:
                    node.setDefault();
                    break;
            }
        }
    }
}
