package org.jacorb.util;

/*
 *        JacORB  - a free Java ORB
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

import java.net.URL;
import java.util.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ObjectUtil
{
    private static Class identityHashMapClass = null;
    //for byte -> hexchar
    private static final char[] lookup =
        new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    /**
     * @return the contents of the resource as a string, or null
     * if the contents of the resource could not be located using url
     */
    public static final String readURL( String url )
        throws java.io.IOException
    {
        String token = "file://";
        String line = "";
        java.io.InputStreamReader isr = null;
        if (url.startsWith (token))
            try
            {
                isr = new java.io.FileReader (url.substring(token.length()));
            }
            catch (Exception e)
            {
                System.out.println ("Tried and failed to open file: " +
                                    url.substring(token.length()));
                // no worries, let the URL handle it
            }
        if (isr == null)
        {
            java.net.URL u = new java.net.URL(url);
            isr = new java.io.InputStreamReader(u.openStream());
        }

        java.io.BufferedReader in = new java.io.BufferedReader(isr);
        line = in.readLine();

        in.close();
        return line;
    }

    /**
     * Returns the <code>Class</code> object for the class or interface
     * with the given string name. This method is a replacement for
     * <code>Class.forName(String name)</code>. Unlike
     * <code>Class.forName(String name)</code> (which always uses the
     * caller's loader or one of its ancestors), <code>classForName</code>
     * uses a thread-specific loader that has no delegation relationship
     * with the caller's loader. It attempts the load the desired class
     * with the thread-specific context class loader and falls back to
     * <code>Class.forName(String name)</code> only if the context class
     * loader cannot load the class.
     * <p>
     * Loading a class with a loader that is not necessarily an ancestor
     * of the caller's loader is a crucial thing in many scenarios. As an
     * example, assume that JacORB was loaded by the boot class loader,
     * and suppose that some code in JacORB contains a call
     * <code>Class.forName(someUserClass)</code>. Such usage of
     * <code>Class.forName</code> effectively forces the user to place
     * <code>someUserClass</code> in the boot class path. If
     * <code>classForName(someUserClass)</code> were used instead, the user
     * class would be loaded by the context class loader, which by default
     * is set to the system (CLASSPATH) classloader.
     * <p>
     * In this simple example above, the default setting of the context class
     * loader allows classes in the boot classpath to reach classes in the
     * system classpath. In other scenarios, the context class loader might
     * be different from the system classloader. Middleware systems like
     * servlet containers or EJB containers set the context class loader so
     * that a given thread can reach user-provided classes that are not in
     * the system classpath.
     * <p>
     * For maximum flexibility, <code>classForName</code> should replace
     * <code>Class.forName(String name)</code> in nearly all cases.
     *
     * @param name the fully qualified name of a class
     *
     * @return the Class object for that class
     *
     * @throws IllegalArgumentException if <code>name</code> is null
     * @throws ClassNotFoundException if the named class cannot be found
     * @throws LinkageError if the linkage fails
     * @throws ExceptionInInitializerError if the class initialization fails
     */

    public static Class classForName(String name)
        throws ClassNotFoundException, IllegalArgumentException
    {
        if (name == null)
            throw new IllegalArgumentException("Class name must not be null!");
        try
        {
            // Here we prefer classLoader.loadClass() over the three-argument
            // form of Class.forName(), as the latter is reported to cause
            // caching of stale Class instances (due to a buggy cache of
            // loaded classes).
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        catch (Exception e)
        {
            // As a fallback, we prefer Class.forName(name) because it loads
            // array classes (i.e., it handles arguments like
            // "[Lsome.class.Name;" or "[[I;", which classLoader.loadClass()
            // does not handle).
            return Class.forName(name);
        }
    }

    /**
     * Creates an IdentityHashMap, using either the JDK 1.4 class or
     * JacORB's drop-in replacement class if the former is not available.
     *
     * @return a newly created IdentityHashMap instance
     */
    public static Map createIdentityHashMap()
    {
        if (identityHashMapClass == null)
        {
            try
            {
                identityHashMapClass =
                    ObjectUtil.classForName("java.util.IdentityHashMap");
            }
            catch (ClassNotFoundException ex)
            {
                try
                {
                    identityHashMapClass =
                        ObjectUtil.classForName("org.jacorb.util.IdentityHashMap");
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }
        try
        {
            return (Map)identityHashMapClass.newInstance();
        }
        catch (Exception exc)
        {
            throw new RuntimeException(exc.toString());
        }
    }

    public static String bufToString( byte bs[],
            int start,
            int len)
    {
        final StringBuffer result = new StringBuffer();
        final StringBuffer chars = new StringBuffer();

        for ( int i = start; i < (start + len); i++ )
        {
            if ((i % 16 ) == 0)
            {
                result.append( chars.toString() );
                result.append( "\n" );
                chars.setLength(0);
            }

            chars.append( toAscii( bs[i] ));
            result.append(  toHex( bs[i] ));

            if ( (i % 4) == 3 )
            {
                chars.append( ' ' );
                result.append( ' ' );
            }
        }

        if ( len % 16 != 0 )
        {
            int pad = 0;
            int delta_bytes = 16 - (len % 16);

            //rest of line (no of bytes)
            //each byte takes two chars plus one ws
            pad = delta_bytes * 3;

            //additional whitespaces after four bytes
            pad += (delta_bytes / 4);

            // additional whitespace after completed 4 byte block
            if ((delta_bytes % 4) > 0)
            {
                pad += 1;
            }

            for ( int i = 0; i < pad; i++ )
            {
                chars.insert( 0, ' ' );
            }
        }

        result.append( chars.toString());
        return result.toString();
    }


    /**
     * <code>toHex</code> converts a byte into a readable string.
     *
     * @param b a <code>byte</code> value
     * @return a <code>String</code> value
     */

    public static final String toHex(byte b)
    {
        StringBuffer sb = new StringBuffer();

        int upper = (b >> 4) & 0x0F;
        sb.append( lookup[upper] );

        int lower = b & 0x0F;
        sb.append( lookup[lower] );

        sb.append( ' ' );

        return sb.toString();
    }


    public static final char toAscii(byte b)
    {
        if ( b > (byte) 31 &&  b < (byte) 127)
        {
            return (char) b;
        }

        return '.';
    }

    /**
     * Convenience method to parse an argument vector (typically from
     * the command line) and sets any arguments of the form "-Dy=x"
     * as values in a properties object.
     */

    public static java.util.Properties argsToProps(String[] args)
    {
        java.util.Properties props = new java.util.Properties();

        for( int i = 0; i < args.length; i++ )
        {
            if (args[i].startsWith("-D"))
            {
                int idx = args[i].indexOf('=');
                if (idx < 3 )
                    continue;
                String key = args[i].substring(2,idx);

                System.out.println("putting: " + key + "," + args[i].substring(idx+1));
                props.put(key, args[i].substring(idx+1));
            }
        }
        return props;
    }

    public static URL getResource(String name)
    {
        if (Thread.currentThread().getContextClassLoader() != null)
        {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }
        return ObjectUtil.class.getResource(name);
    }
}
