/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

class BaseType 
    extends SimpleTypeSpec
{
    public BaseType(int num) 
    {
	super(num);
    }

    /** ignore, these types don't need to know their package */
    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
    }

    public TypeSpec typeSpec()
    {
	return type_spec.typeSpec();
    }	

    public boolean basic()
    {
	return type_spec.basic();
    }

    public boolean isSwitchType ()
    {
	return
        (
            type_spec instanceof SwitchTypeSpec &&
            ((SwitchTypeSpec)type_spec).isSwitchable ()
        );
    }

    public void parse()         
    {}

    public static boolean isBasicName( String typeName )
    {
        return ( typeName.startsWith( "long" ) ||
                 typeName.startsWith( "int" ) ||
                 typeName.startsWith( "short" ) ||
                 typeName.startsWith( "float" ) ||
                 typeName.startsWith( "double" ) ||
                 typeName.startsWith( "byte" ) ||
                 typeName.startsWith( "boolean" ) ||
                 typeName.startsWith( "char" ) ||
                 typeName.startsWith( "string" ) );
    }

    public String signature()
    {
	return type_spec.signature();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	enclosing_symbol = s;
    }

    /** the "kind of TypeCode" for this type */

    public int getTCKind()
    {
	return ((BaseType)type_spec).getTCKind();
    }

    protected String typeCodeExpressionSkeleton(int kind)
    {
	return "org.omg.CORBA.ORB.init().get_primitive_tc(" 
	    + "org.omg.CORBA.TCKind.from_int(" + kind + "))";
    }

    public String getTypeCodeExpression()
    {
	return typeCodeExpressionSkeleton(getTCKind());
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

    public void print(PrintWriter ps) 
    {
    }


}
