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

public class Value
    extends TypeDeclaration
{
    private Value value;

    public Value( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        return value.clone();
    }

    public void setValue( Value value )
    {
        this.value = value;
    }

    public TypeDeclaration declaration()
    {
        return value;
    };

    public String typeName()
    {
        return value.typeName();
    }


    /**
     * @return a string for an expression of type TypeCode
     * 			that describes this type
     */

    public String getTypeCodeExpression()
    {
        return value.getTypeCodeExpression();
    }

    public boolean basic()
    {
        return value.basic();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        value.setPackage( s );
    }

    public void set_included( boolean i )
    {
        included = i;
        value.set_included( i );
    }

    public void parse()
    {
        value.parse();
    }

    public String holderName()
    {
        return value.holderName();
    }

    public void print( PrintWriter ps )
    {
        value.print( ps );
    }

    public String toString()
    {
        return value.toString();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            System.err.println( "was " + enclosing_symbol.getClass().getName() + " now: " + s.getClass().getName() );
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        }
        if( s == null )
            throw new RuntimeException( "Compiler Error: enclosing symbol is null!");

        enclosing_symbol = s;
        value.setEnclosingSymbol( s );
    }

    public String printReadExpression( String streamname )
    {
        return value.printReadExpression( streamname );
    }

    public String printReadStatement( String var_name, String streamname )
    {
        return value.printReadStatement( var_name, streamname );
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return value.printWriteStatement( var_name, streamname );
    }

    /**
     * @overrides accept in TypeDeclaration
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        value.accept( visitor );
    }


}







