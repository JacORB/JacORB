package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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

    public static String className ( String repId )
    {
        // cut "IDL:" and version
        // and swap "org.omg" if necessary

        String id_base = repId.substring(4, repId.lastIndexOf(':'));
        if( id_base.startsWith("omg.org"))
            return ir2scopes("org.omg",id_base.substring(7));
        else
            return ir2scopes( "", id_base );
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
            try
            {
                Class c = null;
                if( sb.toString().length() > 0 )
                    c = RepositoryImpl.loader.loadClass( sb.toString() + "." + sc );
                else
                    c = RepositoryImpl.loader.loadClass( sc );

                if( i < count-1)
                {
                    sb.append( "." + sc + "Package");
                }
                else
                    sb.append( "." + sc );
            }	catch ( ClassNotFoundException cnfe )
            {
                if( sb.toString().length() > 0 )
                    sb.append( "." + sc );
                else
                    sb.append( sc );
            }	
        }

        return sb.toString();
    }

    public static String repId( Class c )
    {
        String class_name = c.getName();
        return toRepositoryID(class_name);		
    }

    private static String scopesToIR( String s )
    {
        if( s.indexOf(".") < 0)
            return s;
        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( s, "." );
        String scopes[] = new String[strtok.countTokens()];
        for( int i = 0; strtok.hasMoreTokens(); i++ ){
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


    public static String toRepositoryID ( String className )
    {
        String head = "";
        String body = "";

        // add "IDL:" and ":1.0"
        // and swap "org.omg" if necessary

        if( className.equals("") || className.startsWith("IDL:"))
            return className;

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
}




