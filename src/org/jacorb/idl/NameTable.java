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
 * A table of defined names
 *
 * @author Gerald Brose
 * @version $Id$
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

public class NameTable
{
    private static final Hashtable names = new Hashtable( 10000 );

    private static final Map shadows = new Hashtable();

    private static final Map ancestors = new Hashtable();

    /**
     key: operation name,
     value: interface this operation was originally defined in
     necessary to track legal diamond inheritance of operations
     */

    private static final Map operationSources = new Hashtable();

    public static final Map parsed_interfaces = new Hashtable();

    static org.apache.log.Logger logger;

    public static void init()
    {
        names.clear();
        operationSources.clear();
        shadows.clear();
        ancestors.clear();

        operationSources.clear();
        parsed_interfaces.clear();

        names.put( "char", "type" );
        names.put( "boolean", "type" );
        names.put( "long", "type" );
        names.put( "long", "type" );
        names.put( "short", "type" );
        names.put( "int", "type" );
        names.put( "float", "type" );
        names.put( "double", "type" );
        names.put( "byte", "type" );
        names.put( "void", "type" );
        names.put( "org.omg.CORBA.Any", "type" );
        names.put( "org.omg.CORBA.Object", "interface" );

        logger = parser.getLogger();
    }

    /**
     * check IDL scoping rules
     * @throws NameAlreadyDefined, or the derived IllegalRedefinition
     */

    private static void checkScopingRules( String name, String kind )
        throws NameAlreadyDefined
    {
        if( logger.isDebugEnabled() )
        {
            logger.debug("NameTable.checkScopingRules:  " +
                         name + " kind: " + kind );
        }

        if( kind.equals( "argument" ) )
        {
            return; // no checks in outer scopes ???
        }

        StringTokenizer strtok =
            new StringTokenizer( name.toUpperCase(), "." );

        String scopes[] = new String[ strtok.countTokens() ];

        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            scopes[ i ] = strtok.nextToken();
        }

        if( logger.isDebugEnabled() )
        {
            logger.debug(
                         "NameTable.checkScopingRules2:  " +
                         name + " kind: " + kind );
        }

        if( scopes.length > 1 &&
            scopes[ scopes.length - 2 ].equals( scopes[ scopes.length - 1 ] ) )
        {
            throw new IllegalRedefinition( name );
        }
    }


    /**
     *  define a name. If it has already been defined in this scope,
     *  an exception is thrown
     *
     *  @param name The name to be defined
     *  @param kind the type of name, e.g. "type"
     *  @throws NameAlreadyDefined if the name is already defined
     */

    public static void define( String name, String kind )
        throws NameAlreadyDefined
    {
        if( logger.isInfoEnabled() )
        {
            logger.info( "NameTable.define2: putting " +
                        name + " kind " + kind + " hash: " +
                        name.hashCode() );
        }

        /* check also for the all uppercase version of this name,
           (which is also reserved to block identifiers that
           only differ in case) */

        if( names.containsKey( name ) ||
            names.containsKey( name.toUpperCase() ) )
        {
            // if this name has been inherited, it is "shadowed"
            // in this case, it is redefined if it is not an operation
            // or interface name. If it has been
            // explicitly defined in this scope, we have an error

            if( kind.equals( "module" ) )
            {
                // modules may be "reopened", no further checks or table entries
                return;
            }
            else if( !shadows.containsKey( name ) ||
                     kind.equals( "operation" ) ||
                     kind.equals( "interface" ) )
            {
                throw new NameAlreadyDefined( name );
            }
            else
            {
                // redefine
                if( logger.isInfoEnabled() )
                {
                    logger.info( "NameTable.define2: redefining  " + name  );
                }

                shadows.remove( name );
                names.remove( name );

                if( kind.startsWith( "type" ) )
                {
                    // remove the inherited type definition, a new one will be
                    // added soon under this name! Addition of this line fixes
                    // bug #345
                    TypeMap.removeDefinition( name );
                }
            }
        }

        if( org.jacorb.idl.parser.strict_names )
        {
            checkScopingRules( name, kind );
        }

        names.put( name, kind );

        /* block identifiers that only differ in case */
        if( org.jacorb.idl.parser.strict_names )
        {
            names.put( name.toUpperCase(), "dummy" );
        }

        if( kind.equals( "operation" ) )
        {
            operationSources.put( name, name.substring( 0, name.lastIndexOf( "." ) ) );
        }
    }

    private static void defineInheritedOperation( String name,
                                                  String inheritedFrom )
            throws NameAlreadyDefined
    {

        if( names.containsKey( name ) )
        {
            String source = null;
            String opName =
                    ( name.indexOf( "." ) < 0 ? name : name.substring( name.lastIndexOf( "." ) + 1 ) );
            String presentOpName = name;

            while( ( source = (String)operationSources.get( presentOpName ) ) != null )
            {
                if( presentOpName.equals( source + "." + opName ) )
                {
                    break;
                }
                presentOpName = source + "." + opName;

            }
            if( logger.isInfoEnabled() )
            {
                logger.info("NameTable source of " + name
                            + " is " + presentOpName );
            }

            String otherOpName = inheritedFrom + "." + opName;

            while( ( source = (String)operationSources.get( otherOpName ) ) != null )
            {
                if( otherOpName.equals( source + "." + opName ) )
                {
                    break;
                }
                otherOpName = source + "." + opName;
            }
            if( logger.isInfoEnabled() )
            {
                logger.info("NameTable other source of " + name
                            + " is " + otherOpName );
            }

            if( otherOpName.equals( presentOpName ) )
            {
                // found an operation that is inherited from
                // the same ultimate base via different paths
                // do nothing, as it is already defined for this
                // interface
                return;
            }
            // illegal multiple inheritance of a the same op name
            throw new NameAlreadyDefined( name );
        }
        names.put( name, "operation" );
        operationSources.put( name, inheritedFrom );
    }

    /**
     *  define a shadowed name, i.e. an inherited name
     *  @throws NameAlreadyDefined if a name is already defined
     */

    private static void defineShadows( Hashtable shadowEntries )
        throws NameAlreadyDefined
    {
        String firstViolation = null;
        for( Enumeration e = shadowEntries.keys(); e.hasMoreElements(); )
        {
            String name = (String)e.nextElement();
            String kind = (String)shadowEntries.get( name );
            if( names.containsKey( name ) )
            {
                firstViolation = name;
            }
            else
            {
                names.put( name, kind );
                if( logger.isDebugEnabled() )
                {
                    logger.debug( "Put shadow " + name );
                }
                shadows.put( name, "" );
                if( kind.equals( "operation" ) )
                {
                    operationSources.put( name, name.substring( 0, name.lastIndexOf( "." ) ) );
                }
            }
        }
        if( firstViolation != null )
        {
            throw new NameAlreadyDefined( firstViolation );
        }
    }

    /**
     * copy names declared in an ancestor interface to the local scope
     *  @throws NameAlreadyDefined
     */

    public static synchronized void inheritFrom( String name,
                                                 SymbolList ancestors )
        throws NameAlreadyDefined
    {
        Hashtable shadowNames = new Hashtable();
        for( Enumeration e = names.keys(); e.hasMoreElements(); )
        {
            String key = (String)e.nextElement();
            String s = null;
            if( key.indexOf( '.' ) > 0 )
            {
                s = key.substring( 0, key.lastIndexOf( '.' ) );
            }
            else
            {
                continue;
            }

            for( Enumeration i = ancestors.v.elements(); i.hasMoreElements(); )
            {
                String anc = ( (ScopedName)( i.nextElement() ) ).resolvedName();
                if( s.equals( anc ) )
                {
                    String kind = (String)names.get( key );
                    if( logger.isDebugEnabled() )
                    {
                        logger.debug( "NameTable.inheritFrom ancestor " + anc +
                                       " : key " + key + " kind " + kind );
                    }

                    String shadowKey = name + key.substring( key.lastIndexOf( '.' ) );
                    shadowNames.put( shadowKey, kind );

                    // if the name we inherit is a typedef'd name, we need
                    // to typedef the inherited name as well

                    if( kind.startsWith( "type" ) )
                    {
                        if( logger.isDebugEnabled() )
                            logger.debug( "- NameTable.inherit type from:  " + key );

                        TypeSpec t =
                            TypeMap.map( anc + key.substring( key.lastIndexOf( '.' ) ) );

                        // t can be null for some cases where we had to put
                        // Java type names (e.g. for sequence s) into the
                        // name table. These need not be typedef'd again here

                        if( t != null )
                        {
                            TypeMap.typedef( name +
                                    key.substring( key.lastIndexOf( '.' ) ), t );
                        }
                        shadowNames.put( name + key.substring( key.lastIndexOf( '.' ) ), kind );
                    }
                    else if( kind.equals( "operation" ) )
                    {
                        if( logger.isDebugEnabled() )
                            logger.debug( "- NameTable.inherit operation from:  " +
                                                           key );

                        NameTable.defineInheritedOperation( name +
                                                            key.substring( key.lastIndexOf( '.' ) ),
                                                            anc );
                    }
                    else
                    {
                        if( logger.isDebugEnabled() ) {
                            logger.debug("- NameTable.inherit " + kind + " from:  " + key);
                        }
                    }


                    if( !isDefined( key ) )
                    {
                        throw new RuntimeException( "CompilerError!" );
                    }
                }
            }
        }

        /* update the hashtable */

        try
        {
            defineShadows( shadowNames );
        }
        catch( NameAlreadyDefined nad )
        {
            if( logger.isDebugEnabled() )
                logger.debug( "Exception ", nad );
        }
    }

    /**
     * check whether name is already defined
     */

    public static boolean isDefined( String name )
    {
        return ( names.containsKey( name ) );
    }

    public static boolean isDefined( String name, String kind )
    {
        if( !names.containsKey( name ) )
        {
            return false;
        }
        String k = (String)names.get( name );
        return ( k.compareTo( kind ) == 0 );
    }


    static boolean baseType( String _s )
    {
        return ( _s.equals( "int" ) || _s.equals( "short" ) || _s.equals( "long" ) ||
                _s.equals( "float" ) || _s.equals( "boolean" ) ||
                _s.equals( "double" ) || _s.equals( "byte" ) || _s.equals( "char" ) ||
                _s.equals( "void" ) || _s.equals( "org.omg.CORBA.Object" ) ||
                _s.equals( "org.omg.CORBA.Any" ) || _s.equals( "<anon>" ) );
    }
}
