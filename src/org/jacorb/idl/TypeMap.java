/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

import java.util.*;
import java.io.*;


class TypeMap 
{
    static java.util.Hashtable typemap = new java.util.Hashtable(5000);

    static
    {
	typemap.put( "org.omg.CORBA.Object", new ObjectTypeSpec( IdlSymbol.new_num()) );
	typemap.put( "org.omg.CORBA.TypeCode", new TypeCodeTypeSpec( IdlSymbol.new_num()) );
	typemap.put( "CORBA.Object", new ObjectTypeSpec( IdlSymbol.new_num()) );
	typemap.put( "CORBA.TypeCode", new TypeCodeTypeSpec( IdlSymbol.new_num()) );
    }


    // return the type spec associated with a name, if any

    public static TypeSpec map( String name )
    {
	return (TypeSpec)typemap.get(name);
    }

    /**
     * define a new name for a type spec
     */

    public static void typedef( String name, TypeSpec type ) 
	throws NameAlreadyDefined
    {
	Environment.output(3,"Typedef'ing " + name + " for " + 
                           type.typeName() + " , hash: " + type.hashCode() );

	if( typemap.containsKey( name ))
	{
	    // actually: throw new NameAlreadyDefined();
	    // but we get better error messages if we leave 
	    // this to later stages 
	    ;
	}
	else
	{
	    if( type.typeSpec() instanceof ScopedName )
	    {
		if(  ((ScopedName)type.typeSpec()).resolvedTypeSpec() != null )
		    typemap.put( name, ((ScopedName)type.typeSpec()).resolvedTypeSpec() );
		else
		    typemap.put( name, type.typeSpec() );
		Environment.output(3," resolved " + ((ScopedName)type.typeSpec()).resolvedTypeSpec()); 
	    } 
	    else 
	    {
		typemap.put( name, type.typeSpec() );
		Environment.output(3,""+ type.typeSpec() ); 
	    }
	}
    }

    public static void replaceForwardDeclaration( String name, TypeSpec type ) 
    {
	if( typemap.containsKey( name ))
	{
	    typemap.remove( name );
	    try
	    {
		typedef( name, type );
	    } 
	    catch ( NameAlreadyDefined nad )
	    {
		// serious error, should never happen
		System.err.println("TypeMap.replaceForwardDeclaration, serious error!");
		nad.printStackTrace();
		System.exit(1);
	    }
	}
	else
	    throw new RuntimeException("Could not find forward declaration!");
    }

}	




















