package org.jacorb.idl;

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


import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class TypeDeclarator
        extends IdlSymbol
{

    public TypeSpec type_spec;
    public SymbolList declarators;

    public TypeDeclarator( int num )
    {
        super( num );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        type_spec.setPackage( s );
        for( Enumeration e = declarators.v.elements();
             e.hasMoreElements(); )
        {
            Declarator d = (Declarator)e.nextElement();
            d.setPackage( s );
        }
    }

    public void parse()
    {
        throw new RuntimeException( "This method may not be used!" );
        /*
          declarators.parse();
          type_spec.parse();
        */
    }

    public TypeSpec type_spec()
    {
        return type_spec.typeSpec();
    }

    public String typeName()
    {
        return type_spec.typeName();
    }

    public void print( PrintWriter ps )
    {
        type_spec.print( ps );
        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
            ( (Declarator)e.nextElement() ).print( ps );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        enclosing_symbol = s;
        type_spec.setEnclosingSymbol( s );
        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
            ( (Declarator)e.nextElement() ).setEnclosingSymbol( s );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( type_spec.toString() );
        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
            sb.append( (Declarator)e.nextElement() );
        return sb.toString();
    }

}




















