package org.jacorb.util;

import java.util.StringTokenizer;

/*
 *        JacORB  - a free Java ORB
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

import java.util.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ObjectUtil
{
    /**
     * @returns the contents of the resource as a string, or null
     * if the contents of the resource could not be located using url
     */
    public static final String readURL( String url ) 
    {
        try
        {
            java.net.URL u = new java.net.URL(url);
            String line  = null;
            java.io.BufferedReader in;      

            in = new java.io.BufferedReader(new java.io.InputStreamReader(u.openStream()) ); 
            line = in.readLine();

            in.close();
            return line;
        }
        catch ( Exception e )
        { 
            Debug.output( 1, "ERROR: Could not read from URL " + url );
            Debug.output( 5, e );           
        }

        return null;
    }

 /**
   * Converts a repository id of the form "IDL:A.B.C/X/Y/Z:1.0"
   * into a java class name of the form "C.B.A.X.Y.Z"
   */

    public static final String reposIdToClassName (String id)
    {
        String name;
        StringBuffer buf = new StringBuffer (id);

        // Strip trailing version ":1.0"

        buf.delete (id.lastIndexOf (':'), id.length ());

        // Strip leading "IDL:"

        buf.delete (0, 4);

        // name is now in the form A.B.C/X/Y/Z

        name = buf.toString ();
        int slash = name.indexOf ('/');
  
        if (slash != -1)
        {
            String reverse = name.substring (0, slash); // "A.B.C"
            String rest = name.substring (slash).replace ('/','.'); // ".X.Y.Z"
  
            StringTokenizer dots = new StringTokenizer (reverse, ".");
            String correct = dots.nextToken ();
            while (dots.hasMoreTokens ())
            {
               correct = dots.nextToken() + '.' + correct; // prepend
            }
            name = correct + rest;
        }

        return name;
   }
}

