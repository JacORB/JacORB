/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.PrintWriter;
import java.util.*;

public class TypeDef
    extends TypeDeclaration
{
    public TypeDeclarator type_declarator;
    private Vector typeSpecs = new Vector();

    public TypeDef( int num )
    {
        super( num );
        pack_name = "";
    }

    public Vector getTypeSpecs()
    {
        return typeSpecs;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        type_declarator.setPackage( s );
    }

    public void set_included( boolean i )
    {
        included = i;
    }

    public String id()
    {
        return type_declarator.id();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            System.err.println( "was " + enclosing_symbol.getClass().getName() +
                    " now: " + s.getClass().getName() );
            throw new RuntimeException( "Compiler Error: trying to reassign container for " +
                    name );
        }
        enclosing_symbol = s;
        type_declarator.setEnclosingSymbol( s );
    }

    public void parse()
    {
        for( Enumeration e = type_declarator.declarators.v.elements();
             e.hasMoreElements(); )
        {
            Declarator d = (Declarator)e.nextElement();
            d.escapeName();

            try
            {
                AliasTypeSpec alias =
                        new AliasTypeSpec( (TypeSpec)type_declarator.type_spec() );

                /* arrays need special treatment */

                if( d.d instanceof ArrayDeclarator )
                {
                    // we don't parse the declarator itself
                    // as that would result in its name getting defined
                    // we define the declarator's name as a type name indirectly
                    // through the cloned type specs.

                    alias = new AliasTypeSpec( new ArrayTypeSpec(
                            new_num(), alias.originalType(),
                            (ArrayDeclarator)d.d, pack_name )
                    );
                    alias.parse();
                }
                else
                {
                    if( !( e.hasMoreElements() ) )
                        alias.parse();
                }
                alias.set_name( d.name() );
                alias.setPackage( pack_name );
                alias.setEnclosingSymbol( enclosing_symbol );
                alias.set_token( d.d.get_token() );
                alias.set_included( included );

                typeSpecs.addElement( alias );
                NameTable.define( d.full_name(), "type" );
                TypeMap.typedef( d.full_name(), alias );
            }
            catch( NameAlreadyDefined n )
            {
                parser.error( "TypeDef'd name " + d.name() +
                        " already defined. ", d.token );
            }
        }
    }

    public void print( PrintWriter ps )
    {
        if( included && !generateIncluded() )
            return;

        for( Enumeration e = typeSpecs.elements();
             e.hasMoreElements(); )
        {
            ( (AliasTypeSpec)e.nextElement() ).print( ps );
        }

    }

    /**
     * @overrides accept in TypeDeclaration
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        for( Enumeration e = typeSpecs.elements();
             e.hasMoreElements(); )
        {
            ( (AliasTypeSpec)e.nextElement() ).accept( visitor );
        }
        visitor.visitTypeDef( this );
    }



}

