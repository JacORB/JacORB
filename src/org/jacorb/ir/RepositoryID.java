package org.jacorb.ir;

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

import java.util.StringTokenizer;
import org.omg.CORBA.INTF_REPOS;

/**
 * This class builds CORBA repository IDs from Java classes
 * or class names, or builds Java class names from repository
 * IDs
 */
public class RepositoryID
{
    /**
     * Returns the fully qualified name of the Java class to which
     * the given Repository ID is mapped.
     */
    public static String className (String repId)
    {
        return className (repId, null);
    }

    /**
     * Returns the fully qualified name of the Java class to which
     * the given Repository ID is mapped, with a given suffix appended
     * to the class name.  For example, the string "Helper" can be used
     * as the suffix to find the helper class for a given Repository ID.
     */
    public static String className (String repId, String suffix)
    {
        if (repId.startsWith ("RMI:"))
        {
            return repId.substring (4, repId.indexOf (':', 4))
                   + ( suffix != null ? suffix : "" );
        }
        else if (repId.startsWith ("IDL:"))
        {
            String id = repId.substring (4, repId.lastIndexOf(':'))
                        + ( suffix != null ? suffix : "" );
            if (id.equals ("omg.org/CORBA/WStringValue"))
                return "java.lang.String";
            else
            {
                int    firstSlash = id.indexOf ("/");
                String prefix     = id.substring (0, firstSlash);

                if (prefix.equals ("omg.org"))
                    return ir2scopes ("org.omg",
                                      id.substring (firstSlash + 1));
                else if (prefix.indexOf ('.') != -1)
                    return ir2scopes (reversePrefix (prefix),
                                      id.substring (firstSlash + 1));
                else
                    return ir2scopes ("", id);
            }
        }
        else
        {
           throw new INTF_REPOS ("Unrecognized RepositoryID: " + repId);
        }
    }

    private static final String reversePrefix (String prefix)
    {
        StringTokenizer tok    = new StringTokenizer (prefix, ".");
        String          result = tok.nextToken();

        while (tok.hasMoreTokens())
        {
            result = tok.nextToken() + '.' + result;
        }
        return result;
    }

    /**
     * FIXME: This method needs documentation.
     * What does this algorithm do, and why is it necessary?  AS.
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
     * Converts a class name to a Repository ID.
     * @param classname the class name to convert
     * @param resolveClass indicates whether the method should try to
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
