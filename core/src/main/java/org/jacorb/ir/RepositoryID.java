package org.jacorb.ir;

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

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.StringValueHelper;
import org.omg.CORBA.WStringValueHelper;

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
    public static String className (String repId,
                                    ClassLoader loader)
    {
        return className (repId, null, loader);
    }

    /**
     * Returns the fully qualified name of the Java class to which
     * the given Repository ID is mapped, with a given suffix appended
     * to the class name.  For example, the string "Helper" can be used
     * as the suffix to find the helper class for a given Repository ID.
     */
    public static String className (String repId,
                                    String suffix,
                                    ClassLoader loader)
    {
        if (repId.startsWith ("RMI:"))
        {
            // TODO should convertFromISOLatin1 be called in else branch also?
            repId = convertFromISOLatin1(repId);
            return repId.substring (4, repId.indexOf (':', 4))
                   + ( suffix != null ? suffix : "" );
        }
        else if (repId.startsWith ("IDL:"))
        {
            if (repId.equals(WStringValueHelper.id()))
            {
                return "java.lang.String";
            }

            if (repId.equals(StringValueHelper.id()))
            {
                return "java.lang.String";
            }

            final String id = repId.substring (4, repId.lastIndexOf(':'))
                        + ( suffix != null ? suffix : "" );

            int    firstSlash = id.indexOf ("/");
            final String prefix;
            if (firstSlash == -1)
            {
                prefix = "";
            }
            else
            {
                prefix = id.substring (0, firstSlash);
            }

            if (prefix.equals ("omg.org"))
            {
                return ir2scopes ("org.omg",
                        id.substring (firstSlash + 1),
                        loader);
            }
            else if (prefix.indexOf ('.') != -1)
            {
                return ir2scopes (reversePrefix (prefix),
                        id.substring (firstSlash + 1),
                        loader);
            }
            else
            {
                return ir2scopes ("", id, loader);
            }
        }
        else
        {
           throw new INTF_REPOS ("Unrecognized RepositoryID: " + repId);
        }
    }

    /**
     * Convert the repository ID with escape sequences back to original strings.
     *
     * com.sun.corba.se.internal.orbutil.RepositoryId
     * contains an implementation of this conversion.
     * however the method is private and its an internal sun class.
     */
    private static String convertFromISOLatin1 (String id) {
        StringBuffer dest = new StringBuffer(id.length());
        int pos = 0;
        int index = -1;

        while ((index = id.indexOf("\\U", pos)) != -1)
        {
            dest.append(id.substring(pos, index));
            pos = index + 6;
            // convert the hexa val in 4 char into one char
            char theChar = (char)(Integer.parseInt(id.substring(index+2, index+6), 16));
            dest.append(theChar);
        }

        if (pos < id.length())
        {
           dest.append(id.substring(pos));
        }

        return dest.toString();
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
     * Convert a repoID path string to a fully qualified class name.
     * Check to see if the final name in the class is actually nested within
     * a "intfPackage" package, and modify the FQ class name appropriately.
     */
    private static String ir2scopes (String prefix,
                                     String s,
                                     ClassLoader loader)
    {
        if( s.indexOf("/") < 0)
        {
            return s;
        }

        StringBuffer sb = new StringBuffer();
        if (prefix != null && prefix.length() > 0)
          {
            sb.append(prefix + ".");
          }
        sb.append (s);
        int lastdot = -1;
        for (int dotpos = sb.indexOf ("/"); dotpos >= 0;  dotpos = sb.indexOf ("/", dotpos))
          {
            sb.setCharAt (dotpos, '.');
            lastdot = dotpos;
          }

        String scope = sb.toString ();
        if (loadClass (scope, loader) == null)
          {
            sb.insert (lastdot,"Package");
            String pkgscope = sb.toString ();
            if (loadClass (pkgscope, loader) != null)
              {
                scope = pkgscope;
              }
          }

        return scope;
    }

    public static String repId (Class c)
    {
        if (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom (c))
        {
            String className = c.getName();
            String body = "";

            // add "IDL:" and ":1.0"
            // and swap "org.omg" if necessary

            if( className.startsWith("org.omg")  ||
                className.startsWith("org/omg") )
            {
                if( className.length() > 7 )
                {
                    body = className.substring(7);
                }
                return "IDL:omg.org/" + scopesToIR(body) + ":1.0";
            }
            return "IDL:" + scopesToIR(className) + ":1.0" ;
        }
        return org.jacorb.util.ValueHandler.getRMIRepositoryID (c);
    }


    /**
     * Convert a fully qualified class name to a repoID by first eliminating any
     * "Package" extensions then replacing all dots with slashes. Note that this
     * function is an imperfect compliment of ir2scopes in that it does not have
     * a way to distinguish any prefix that was explicitly added to a repository
     * ID to make a class name.
     */
    private static String scopesToIR( String s )
    {
        if( s.indexOf(".") < 0)
        {
            return s;
        }

        StringBuffer sb = new StringBuffer (s);
        String pkgstr = "Package";
        for (int i = sb.indexOf (pkgstr); i != -1; i = sb.indexOf (pkgstr, i))
          {
            sb.delete (i, i+pkgstr.length());
          }

        for (int i = sb.indexOf ("."); i != -1; i = sb.indexOf (".", i))
          {
            sb.setCharAt (i,'/');
          }

        return sb.toString();
    }

    /**
     * Converts a class name to a Repository ID.
     * @param className the class name to convert
     * @param resolveClass indicates whether the method should try to
     * resolve and load the class. If true and the class could
     * not be loaded, an IllegalArgumentException will be thrown
     */
    public static String toRepositoryID ( String className,
                                          boolean resolveClass,
                                          ClassLoader loader )
    {
        if( className.equals("") ||
            className.startsWith("IDL:") ||
            className.startsWith ("RMI:"))
        {
            return className;
        }

        if( resolveClass )
        {

            Class c = loadClass(className, loader);
            if (c == null)
            {
                throw new  IllegalArgumentException("cannot find class: " + className);
            }
            return repId (c);
        }
        return "IDL:" + className + ":1.0";
    }

    public static String toRepositoryID( String className, ClassLoader loader )
    {
        return toRepositoryID( className, true, loader );
    }

    /**
     * Loads class `name' using an appropriate class loader.
     * Returns the corresponding class object, or null if the class loader
     * cannot find a class by that name.
     */
    private static Class loadClass (String name, ClassLoader loader)
    {
        try
        {
            if (loader != null)
            {
                return loader.loadClass (name);
            }
            return org.jacorb.util.ObjectUtil.classForName(name);
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

    /**
     * Creates a BoxedValueHelper instance for a given repository ID.
     * @param repId the repository ID of the boxed value type
     * @return a newly created BoxedValueHelper, or null if no
     *         BoxedValueHelper class can be found for that ID
     * @throws RuntimeException if creation of the Helper instance fails
     */
    public static BoxedValueHelper createBoxedValueHelper(String repId,
                                                          ClassLoader loader)
    {
        String className = className(repId, "Helper", loader);
        Class clazz = loadClass(className, loader);
        if (clazz != null)
        {
            try
            {
                return (BoxedValueHelper)clazz.newInstance();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
