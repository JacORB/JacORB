package org.jacorb.ir;

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

import org.jacorb.orb.TypeCode;

/**
 * This class builds CORBA repository IDs from Java classes 
 * or class names, or builds Java class names from repository
 * IDs
 */

public class RepositoryID 
{

    public static String className (String repId)
    {
        if (repId.equals("IDL:omg.org/CORBA/WStringValue:1.0"))
        {
	    return "java.lang.String";
        }
        else if (repId.startsWith ("IDL:"))
        {
            // cut "IDL:" and version
            // and swap "org.omg" and "org.jacorb" if necessary
            
            String id_base = repId.substring(4, repId.lastIndexOf(':'));
            if( id_base.startsWith("omg.org"))
                return ir2scopes("org.omg",id_base.substring(7));
            else if ( id_base.startsWith( "jacorb.org" ))
                return ir2scopes("org.jacorb", id_base.substring(10));
            else
                return ir2scopes( "", id_base );
        }
        else if (repId.startsWith ("RMI:"))
        {
            return repId.substring (4, repId.indexOf (':', 4));
        }
        else
        {
            throw new RuntimeException ("unrecognized RepositoryID: " + repId);
        }
    }

    /**
     * @return java.lang.String
     */

    private static String ir2scopes (String prefix, String s) 
    {
        if( s.indexOf("/") < 0)
            return s;
        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( s, "/" );
		
        int count = strtok.countTokens();
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
		
        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            String sc = strtok.nextToken();
            Class c = null;
            if( sb.toString().length() > 0 )
                c = loadClass (sb.toString() + "." + sc);
            else
                c = loadClass (sc);
            if (c == null)
                if( sb.toString().length() > 0 )
                    sb.append( "." + sc );
                else
                    sb.append( sc );
            else
                if( i < count-1)
                    sb.append( "." + sc + "Package");
                else
                    sb.append( "." + sc );
        }

        return sb.toString();
    }

    public static String repId (Class c)
    {
        if (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom (c))
        {
            String className = c.getName();
            String head = "";
            String body = "";

            // add "IDL:" and ":1.0"
            // and swap "org.omg" if necessary

            if( className.startsWith("org.omg")  ||
                className.startsWith("org/omg") )
            {
                if( className.length() > 7 ) 
                    body = className.substring(7);
                return "IDL:omg.org/" + scopesToIR(body) + ":1.0";
            }
            else
                return "IDL:" + scopesToIR(className) + ":1.0" ;
        }
	else    
            return org.jacorb.util.ValueHandler.getRMIRepositoryID (c);
    }


    private static String scopesToIR( String s )
    {
        if( s.indexOf(".") < 0)
            return s;
        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( s, "." );

        String scopes[] = new String[strtok.countTokens()];

        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            String sc = strtok.nextToken();
            if( sc.endsWith("Package"))
                scopes[i] = sc.substring(0,sc.indexOf("Package"));
            else
                scopes[i] = sc;
        }

        StringBuffer sb = new StringBuffer();
        if( scopes.length > 1 )
        {
            for( int i = 0; i < scopes.length-1; i++)
                sb.append( scopes[i] + "/" );
        }

        sb.append( scopes[scopes.length-1] );
        return sb.toString();
    }

    /**
     * convert a class name to a Repository ID<BR>
     * classname - the class name to convert
     * resolveClass - indicates whether the method should try to
     * resolve and load the class. If true and the class could
     * not be loaded, an IllegalArgumentException will be thrown
     */

    public static String toRepositoryID ( String className,
                                          boolean resolveClass )
    {
        if( className.equals("") ||
            className.startsWith("IDL:") ||
            className.startsWith ("RMI:"))
            return className;
        else
        {
            if( resolveClass )
            {

                Class c = loadClass (className);
                if (c == null)
                    throw new  IllegalArgumentException("cannot find class: " + className);
                else
                    return repId (c);
            }
            return "IDL:" + className + ":1.0";
        }
    }



    public static String toRepositoryID( String className )
    {
        return toRepositoryID( className, true );
    }

    /**
     * Loads class `name' using an appropriate class loader.
     * Returns the corresponding class object, or null if the class loader
     * cannot find a class by that name.
     */
    private static Class loadClass (String name)
    {
        try
        {
            if (RepositoryImpl.loader != null)
                return RepositoryImpl.loader.loadClass (name);
            else
                //#ifjdk 1.2
                    return Thread.currentThread().getContextClassLoader()
                                                 .loadClass (name);
                //#else
                //# return Class.forName (name);
                //#endif
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }
}
