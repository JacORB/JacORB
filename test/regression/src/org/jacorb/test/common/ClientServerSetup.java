package org.jacorb.test.common;

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

import java.io.*;
import java.util.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import junit.framework.*;
import junit.extensions.*;

/**
 * A special TestSetup that creates a separate CORBA server process,
 * and allows JUnit test cases to talk to a CORBA object supplied
 * by that server.
 * <p>
 * A <code>ClientServerSetup</code> should be used together with a 
 * {@link ClientServerTestCase}, which provides an easy way so that
 * the individual test cases can actually see the setup.  
 * The following example shows how to set this up in the static 
 * <code>suite</code> method:
 *
 * <p><blockquote><pre>
 * public class MyTest extends ClientServerTestCase
 * {
 *     ...
 *
 *     public static Test suite()
 *     {
 *         TestSuite suite = new TestSuite ("My CORBA Test");
 *
 *         // Wrap the setup around the suite, specifying 
 *         // the name of the servant class that should be
 *         // instantiated by the server process.
 * 
 *         ClientServerSetup setup = 
 *             new ClientServerSetup (suite,
 *                                    "my.corba.ServerImpl");
 *
 *         // Add test cases, passing the setup as an
 *         // additional constructor parameter.
 *
 *         suite.addTest (new MyTest ("testSomething", setup));
 *         ...
 *
 *         // Return the setup, not the suite!
 *         return setup;
 *     }
 * }
 * </pre></blockquote><p> 
 * 
 * The individual test cases can then access the setup in a convenient way.
 * For details, see {@link ClientServerTestCase}.
 * 
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class ClientServerSetup extends TestSetup {

    protected String                     servantName;
    protected Process                    serverProcess;
    protected org.omg.CORBA.Object       serverObject;
    protected org.omg.CORBA.ORB          clientOrb;
    protected org.omg.PortableServer.POA clientRootPOA;

    private Properties clientOrbProperties = null;
    private Properties serverOrbProperties = null;

    /**
     * Constructs a new ClientServerSetup that is wrapped
     * around the specified Test.  When the test is run,
     * the setup spawns a server process in which an instance
     * of the class servantName is created and registered 
     * with the ORB.
     * @param test The test around which the new setup 
     * should be wrapped.
     * @param servantName The fully qualified name of the 
     * servant class that should be instantiated in the
     * server process.
     */    
    public ClientServerSetup ( Test test, String servantName )
    {
        super ( test );
        this.servantName = servantName;
    }

    public ClientServerSetup( Test test, 
                              String servantName,
                              Properties clientOrbProperties,
                              Properties serverOrbProperties )
    {
        this( test, servantName );
        
        this.clientOrbProperties = clientOrbProperties;
        this.serverOrbProperties = serverOrbProperties;
    }

    public void setUp() throws Exception
    {
        clientOrb = ORB.init (new String[0], clientOrbProperties );
        clientRootPOA = POAHelper.narrow 
                          ( clientOrb.resolve_initial_references( "RootPOA" ) );
        clientRootPOA.the_POAManager().activate();

        serverProcess = Runtime.getRuntime().exec(   
            "jaco -Djacorb.verbosity=0 "
            + "-Djacorb.orb.print_version=off "
            + propsToCommandLineArgs( serverOrbProperties )
            + " -classpath " 
            + System.getProperty ("java.class.path")
            + " " + getTestServerMain() + " "
            + servantName );
        BufferedReader input = 
            new BufferedReader
                ( new InputStreamReader( serverProcess.getInputStream() ) );
        String ior = input.readLine();
        if (ior == null)
        {
           BufferedReader error = new BufferedReader
              ( new InputStreamReader( serverProcess.getErrorStream() ) );
           throw new RuntimeException ( "SPAWN ERROR: " + error.readLine() );
        } 
        else if ( ior.startsWith ( "ERROR:" ) )
            throw new RuntimeException ( "SERVER " + ior );
        else
            serverObject = clientOrb.string_to_object( ior );
    }

    public void tearDown() throws Exception
    {
        serverProcess.destroy();
    }

    public String getTestServerMain()
    {
        return "org.jacorb.test.common.TestServer";
    }

    /**
     * Gets a reference to the object that was instantiated in the
     * server process.
     */  
    public org.omg.CORBA.Object getServerObject()
    {
        return serverObject;
    }

    /**
     * Gets the client ORB that is used to communicate with the server.
     */
    public org.omg.CORBA.ORB getClientOrb()
    {
        return clientOrb;
    }

    /**
     * Gets the fully qualified name of the servant class that
     * is instantiated in the server.
     */
    public String getServantName()
    {
        return servantName;
    }

    /**
     * Gets the server process.
     */
    public Process getServerProcess()
    {
        return serverProcess;
    }

    public POA getClientRootPOA()
    {
        return clientRootPOA;
    }

    private static String propsToCommandLineArgs( Properties props )
    {
        if( props == null )
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for( Iterator keyIterator = props.keySet().iterator();
             keyIterator.hasNext();
            )
        {
            String key = (String) keyIterator.next();
            sb.append( "-D" );
            sb.append( key );
            sb.append( '=' );
            
            String value = props.getProperty( key );
            if( value == null )
            {
                value = "";
            }

            sb.append( value );

            if( keyIterator.hasNext() )
            {
                sb.append( ' ' );
            }
        }

        return sb.toString();
    }
}
