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

import org.jacorb.notification.parser.TCLParserTokenTypes;

import antlr.collections.AST;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Visitor for TCL Trees. Does some Restructuration of a TCL Tree.
 *
 * Created: Wed Sep 18 02:07:17 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TCLCleanUp extends AbstractTCLVisitor implements TCLParserTokenTypes
{
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor( getClass().getName() );

    public void fix( AbstractTCLNode node )
    {
        try
        {
            node.acceptPostOrder( this );
        }
        catch ( VisitorException e )
        {
            logger_.fatalError("error during fix", e);
        }

    }

    public void visitComponentPosition( ComponentPositionOperator componentPositionOperator )
        throws VisitorException
    {
        // fixCompPos(componentPositionOperator);
    }

    public void visitComponent( ComponentName component )
        throws VisitorException
    {
        // component.left().acceptInOrder(this);

        insertComponentName( component );
    }

    public void visitUnionPosition( UnionPositionOperator op )
        throws VisitorException
    {
        fixUnionPosition( op );
        // fixCompPos(op);
    }

    /**
     * insert the Complete Name of a Component in the
     * ComponentOperator node.
     */
    void insertComponentName( ComponentName comp )
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
