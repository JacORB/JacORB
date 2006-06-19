/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 * Represents IDL base types
 *
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.PrintWriter;

public class BaseType
    extends SimpleTypeSpec
{
    public BaseType( int num )
    {
        super( num );
    }

    /**
     * ignore, these types don't need to know their package
     */

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
    }

    public TypeSpec typeSpec()
    {
        return type_spec.typeSpec();
    }

    public boolean basic()
    {
        return type_spec.basic();
    }

    public boolean isSwitchType()
    {
        return (
                type_spec instanceof SwitchTypeSpec &&
                ( (SwitchTypeSpec)type_spec ).isSwitchable()
                );
    }

    /**
     * does nothing, will be overwritten in subclasses
     */

    public void parse()
    {
    }

    public static boolean isBasicName( String typeName )
    {
        // The type may be an array type e.g. float[][] so chop off any array
        // designators for the comparison.
        int index = typeName.indexOf ('[');
        String toMatch = typeName.substring (0, (index == -1 ? typeName.length () : index));

        return
        (
            toMatch.equals ("long")      ||
            toMatch.equals ("int")       ||
            toMatch.equals ("short")     ||
            toMatch.equals ("float")     ||
            toMatch.equals ("double")    ||
            toMatch.equals ("byte")      ||
            toMatch.equals ("boolean")   ||
            toMatch.equals ("char")      ||
            toMatch.equals ("java.lang.String")
        );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    /** the "kind of TypeCode" for this type */

    public int getTCKind()
    {
        return ( (BaseType)type_spec ).getTCKind();
    }

    protected String typeCodeExpressionSkeleton( int kind )
    {
        return "org.omg.CORBA.ORB.init().get_primitive_tc("
                + "org.omg.CORBA.TCKind.from_int(" + kind + "))";
    }

    public String getTypeCodeExpression()
    {
        return typeCodeExpressionSkeleton( getTCKind() );
    }

    public String toString()
    {
        if( type_spec != null )
            return type_spec.toString();
        else
            return "BaseType";
    }

    public String typeName()
    {
        return type_spec.typeName();
    }

    public String id()
    {
        return "IDL:*primitive*:1.0";
    }

    /**
     * does nothing, will be overwritten in subclasses
     */

    public void print( PrintWriter ps )
    {
    }
}