/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 */

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeMap
{
    static final Hashtable typemap = new Hashtable( 5000 );

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
    {
        Logger r2 = parser.getLogger();
        if( parser.logger.isLoggable(Level.FINEST) )
        {
            Logger r = parser.getLogger();
            parser.logger.log(Level.FINEST, "Typedef'ing " + name +
              " , hash: " + type.hashCode());
        }

        if( typemap.containsKey( name ) )
        {
            // actually throw new NameAlreadyDefined()
            // but we get better error messages if we leave
            // this to later stages
            Logger r = parser.getLogger();
            if( parser.logger.isLoggable(Level.ALL) )
            {
                Logger r1 = parser.getLogger();
                parser.logger.log(Level.FINEST, "Typedef'ing " + name +
                 " already in type map!");
            }
        }
        else
        {
            if( type.typeSpec() instanceof ScopedName )
            {
                if( ( (ScopedName)type.typeSpec() ).resolvedTypeSpec() != null )
                    typemap.put( name, ( (ScopedName)type.typeSpec() ).resolvedTypeSpec() );
                else
                    typemap.put( name, type.typeSpec() );
                Logger r = parser.getLogger();

                if( parser.logger.isLoggable(Level.FINEST) )
                {
                    Logger r1 = parser.getLogger();
                    parser.logger.log(Level.FINEST, " resolved " +
                     ((ScopedName)type.typeSpec()).resolvedTypeSpec());
                }
            }
            else
            {
                typemap.put( name, type.typeSpec() );
                Logger r = parser.getLogger();

                if( parser.logger.isLoggable(Level.FINEST) )
                {
                    Logger r1 = parser.getLogger();
                    parser.logger.log(Level.FINEST, " (not a resolved scoped name) "
                      + type.typeSpec().full_name());
                }
            }
        }
    }


    /**
     * remove the definition of a type with a give name, used when
     * inherited definitions are overwritten, called from NameTable only!
     */

    static void removeDefinition( String name )
    {
       if( typemap.containsKey( name ) )
       {
           typemap.remove( name );
       }
       else
       {
           throw new RuntimeException( "Could not find definition of : " + name );
       }
    }


    public static void replaceForwardDeclaration( String name,
                                                  TypeSpec type )
    {
        if( typemap.containsKey( name ) )
        {
            typemap.remove( name );
            typedef( name, type );
        }
        else
        {
            throw new RuntimeException( "Could not find forward declaration!" );
        }
    }


}
