/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
 *
 */

import java.util.*;
import java.io.*;

class NameTable 
{
    private static java.util.Hashtable h = 
        new java.util.Hashtable(5000);

    private static java.util.Hashtable shadows = 
        new java.util.Hashtable();

    private static java.util.Hashtable ancestors = 
        new java.util.Hashtable();

    /**
       key: operation name, 
       value: interface this operation was originally defined in 
       necessary to track legal diamond inheritance of operations
    */

    private static java.util.Hashtable operationSources = 
        new java.util.Hashtable();

    private static String s = "";

    public static java.util.Hashtable parsed_interfaces = 
        new java.util.Hashtable();

    static
    {
        h.put("char", "type");
        h.put("boolean", "type");
        h.put("long", "type");
        h.put("long", "type");
        h.put("short", "type");
        h.put("int", "type");
        h.put("float", "type");
        h.put("double", "type");
        h.put("byte", "type");
        h.put("void", "type");
        h.put("org.omg.CORBA.Any", "type");
        h.put("org.omg.CORBA.Object", "interface");
    }


    /**
     *  define a name. If it has already been defined in this scope, 
     *  an exception is thrown 
     * 
     *  @param String name The name to be defined
     *  @param String kind the type of name, e.g. "type"
     *  @throw NameAlreadyDefined if the name is already defined
     */

    public static void define( String name, String kind ) 
        throws NameAlreadyDefined 
    {
        Environment.output( 3,
                            "NameTable.define2: putting " + 
                            name + " kind " + kind + " hash: " + 
                            name.hashCode() );

        if( h.containsKey( name ) )
        {
            // if this name has been inherited, it is "shadowed"
            // in this case, it is redefined if it is not an operation
            // or interface name. If it has been
            // explicitly defined in this scope, we have an error

            if( !shadows.containsKey( name ) || 
                kind.equals("operation") || kind.equals("interface"))
            {
                throw new NameAlreadyDefined( name );
            }
            else 
            {
                // redefine
                shadows.remove( name );
                h.remove( name );
            }
        } 
        h.put( name, kind );
        if( kind.equals("operation"))
            operationSources.put( name, name.substring( 0, name.lastIndexOf(".") ));
    }

    private static void defineInheritedOperation( String name, 
                                                  String inheritedFrom )
        throws NameAlreadyDefined 
    {

        if( h.containsKey( name ) )
        {
            String source = null;
            String opName = 
                ( name.indexOf(".") < 0 ? name : name.substring( name.lastIndexOf("." )+1));
            String presentOpName = name;

            while( (source = (String)operationSources.get( presentOpName )) != null )
            {
                if( presentOpName.equals( source + "." + opName ))
                {
                    break;
                }
                presentOpName = source + "." + opName;

            }
            Environment.output( 3,
                                "NameTable source of " + name 
                                + " is " + presentOpName);

            String otherOpName = inheritedFrom  + "." + opName;

            while( ( source = (String)operationSources.get( otherOpName )) != null )
            {
                if( otherOpName.equals( source + "." + opName ))
                {
                    break;
                }  
                otherOpName = source + "." + opName;
            }
            Environment.output( 3,
                                "NameTable other source of " + name 
                                + " is " +  otherOpName);    

            if( otherOpName.equals( presentOpName ))
            {
                // found an operation that is inherited from
                // the same ultimate base via different paths
                // do nothing, as it is already defined for this 
                // interface
                return;
            }
            else    
            {
                // illegal multiple inheritance of a the same op name
                throw new NameAlreadyDefined(name);
            }
        } 
        else
        {
            h.put( name, "operation" );
            operationSources.put( name, inheritedFrom );
        }
    }

    /**
     *  define a shadowed name, i.e. an inherited name
     *  @throw NameAlreadyDefined if a name is already defined
     */

    private static void defineShadows( Hashtable shadowEntries ) 
        throws NameAlreadyDefined 
    {
        String firstViolation = null;
        for( Enumeration e = shadowEntries.keys(); e.hasMoreElements();)
        {
            String name = (String)e.nextElement();
            String kind = (String)shadowEntries.get(name);
            if( h.containsKey( name ) )
            {
                firstViolation = name;
            } 
            else 
            {
                h.put( name, kind );
                Environment.output( 4, "Put shadow " +  name);
                shadows.put( name, "" );
                if( kind.equals("operation"))
                    operationSources.put( name, name.substring( 0, name.lastIndexOf(".") ));
            }
        }
        if( firstViolation != null )
        {
            throw new NameAlreadyDefined( firstViolation );
        }
    }

    /**
     * copy names declared in an ancestor interface to the local scope
     *  @throw NameAlreadyDefined 
     */

    public static synchronized void inheritFrom( String name, SymbolList ancestors )
        throws NameAlreadyDefined 
    {
        Hashtable shadowNames = new Hashtable();
        for( Enumeration e = h.keys(); e.hasMoreElements();)
        {
            String key = (String)e.nextElement();
            String s = null;
            if( key.indexOf('.') > 0)
                s = key.substring(0, key.lastIndexOf('.'));
            else
                continue;

            for( Enumeration i = ancestors.v.elements(); i.hasMoreElements() ; )
            {
                String anc = ((ScopedName)(i.nextElement())).resolvedName();
                if( s.equals( anc ))
                {
                    String kind = (String)h.get(key);
                    Environment.output( 4, "NameTable.inheritFrom ancestor " +  
                                        anc +  " : key " + key + " kind " + kind);

                    String shadowKey = name + key.substring( key.lastIndexOf('.'));
                    shadowNames.put( shadowKey, kind );

                    // if the name we inherit is a typedef'd name, we need to
                    // to typedef the inherited name as well

                    if( kind.equals("type"))
                    {
                        Environment.output(4,"- NameTable.inherit type from:  " + key );


                            TypeSpec t = 
                                TypeMap.map( anc + key.substring( key.lastIndexOf('.')) );
                            
                            // t can be null for some cases where we had to put
                            // Java type names (e.g. for sequence s) into the
                            // name table. These need not be typedef'd again here
                            
                            if( t != null )
                            {
                                TypeMap.typedef( name + 
                                                 key.substring(key.lastIndexOf('.')),t);
                            }                        
                            shadowNames.put( name + key.substring( key.lastIndexOf('.')), kind );
//                          try
//                          {
//                              define( name + key.substring( key.lastIndexOf('.')),kind );
//                          } 
//                          catch ( NameAlreadyDefined nad )
//                          {
//                            Environment.output(3,nad);
//                              // Can be ignored: it is legal to inherit multiple 
//                              // type definitions of the same name in IDL
//                              // System.err.println("Problem " + name + " inherits (multiple inh.)");
//                          }
                    }
                    else if( kind.equals("operation"))
                    {
                        Environment.output(4,"- NameTable.inherit operation from:  " +
                                           key );

                        NameTable.defineInheritedOperation( name + 
                                                            key.substring(key.lastIndexOf('.')),
                                                            anc);
                    }

                    if( !defined( key ))
                        throw new RuntimeException("CompilerError!");
                }
            }
        }

        /* update the hashtable */

        try
        {
            defineShadows( shadowNames );
        } 
        catch ( NameAlreadyDefined nad )
        {
            Environment.output(4, nad );
            // System.err.println( nad.getMessage + " already defined, " + name + " inherits (multiple inh.)");
        }
    }

    /** 
     * check whether name is already defined
     */

    public static boolean defined( String name )
    {
        return( h.containsKey( name ));
    }

    public static boolean defined( String name, String kind )
    {
        if( !h.containsKey( name )){
            return false;
        }
        String k = (String)h.get(name);
        return( k.compareTo(kind) == 0 );
    }

    static boolean baseType(String _s)
    {
        return ( _s.equals("int") || _s.equals("short") || _s.equals("long") || 
                 _s.equals("float") || _s.equals("boolean") || 
                 _s.equals("double") || _s.equals("byte") || _s.equals("char") || 
                 _s.equals("void") || _s.equals("<anon>"));
    }

    static Enumeration getGlobalTypes()
    {
        Vector v = new Vector();
        for( Enumeration e = h.keys(); e.hasMoreElements(); )
        {          
            String str =  (String)e.nextElement();
            if( str.indexOf('.')== -1 && !baseType(str) && 
                ( ((String)h.get( str )).equals("type") ) || ((String)h.get( str )).equals("interface"))
                v.addElement(str);
        }

        return v.elements();
    }


    /** 
     * for debugging purposes only
     * 
     */
    public static void print( PrintWriter ps)
    {
        //      for( Enumeration e = h.keys(); e.hasMoreElements();)
        //  ps.println( (String)e.nextElement());
    }

}





