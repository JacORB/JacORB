package org.jacorb.ir;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.StringTokenizer;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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


/**
 * The main server that starts the Interface Repository
 *
 * @author Gerald Brose
 */

public class IRServer
{
    /**
     * @param  args a vector  of commandline arguments,  where args[1]
     * needs to be a filename string and args[0] a classpath string
     */
    public static void main( String args[] )
    {
        if( args.length != 2)
        {
            System.err.println("Usage: jaco org.jacorb.ir.IRServer <classpath> <IOR filename>");
            System.exit(1);
        }

        try
        {
            StringTokenizer strtok =
                new StringTokenizer( args[0], java.io.File.pathSeparator );

            URL[] urls = new URL[strtok.countTokens()];
            for( int i = 0; strtok.hasMoreTokens(); i++ )
            {
                urls[i] = new java.io.File( strtok.nextToken() ).toURL();
            }

            URLClassLoader classLoader = new URLClassLoader( urls );

            Class repositoryClass = classLoader.loadClass("org.jacorb.ir.RepositoryImpl");


            Properties props = new Properties();
            props.setProperty("jacorb.orb.objectKeyMap.InterfaceRepository",
                              "InterfaceRepository/InterfaceRepositoryPOA/IfR");
            props.setProperty ("jacorb.implname", "InterfaceRepository");
            
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props );
            
            Object repository =
                repositoryClass.getConstructors()[0].newInstance(
                        new Object[]{ args[0], args[1], classLoader, orb });

            repositoryClass.getDeclaredMethod("loadContents", (Class[]) null ).invoke( repository, (Object[]) null );

            orb.run();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
