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

import java.util.*;

class TypeMap
{
    static Hashtable typemap = new Hashtable( 5000 );

    public static void init()
    {
        typemap.clear();
        typemap.put( "org.omg.CORBA.Object", new ObjectTypeSpec( IdlSymbol.new_num() ) );
        typemap.put( "org.omg.CORBA.TypeCode", new TypeCodeTypeSpec( IdlSymbol.new_num() ) );
        typemap.put( "CORBA.Object", new ObjectTypeSpec( IdlSymbol.new_num() ) );
        typemap.put( "CORBA.TypeCode", new TypeCodeTypeSpec( IdlSymbol.new_num() ) );
    }

    // return the type spec associated with a name, if any

    public static TypeSpec map( String name )
    {
        return (TypeSpec)typemap.get( name );
    }

    /**
     * define a new name for a type spec
     */

    public static void typedef( String name, TypeSpec type )
        throws NameAlreadyDefined
    {
        if( parser.getLogger().isInfoEnabled() )
            parser.getLogger().info( "Typedef'ing " + name +
                                          " , hash: " + type.hashCode() );

        if( typemap.containsKey( name ) )
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
                if( ( (ScopedName)type.typeSpec() ).resolvedTypeSpec() != null )
                    typemap.put( name, ( (ScopedName)type.typeSpec() ).resolvedTypeSpec() );
                else
                    typemap.put( name, type.typeSpec() );

                if( parser.getLogger().isInfoEnabled() )
                    parser.getLogger().info(" resolved " +
                                                 ((ScopedName)type.typeSpec()).resolvedTypeSpec());
            }
            else
            {
                typemap.put( name, type.typeSpec() );

                if( parser.getLogger().isInfoEnabled() )
                    parser.getLogger().info( " (not a resolved scoped name) "
                                                  + type.typeSpec().full_name() );
            }
        }
    }

    public static void replaceForwardDeclaration( String name,
                                                  TypeSpec type )
    {
        if( typemap.containsKey( name ) )
        {
            typemap.remove( name );
            try
            {
                typedef( name, type );
            }
            catch( NameAlreadyDefined nad )
            {
                // serious error, should never happen
                nad.printStackTrace();
                org.jacorb.idl.parser.fatal_error( "TypeMap.replaceForwardDeclaration, serious error!", null );
            }
        }
        else
            throw new RuntimeException( "Could not find forward declaration!" );
    }


}
