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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

/**
 * A server program that can set up an arbitrary CORBA servant.
 * The program takes the name of the servant class to use from
 * the command line.  It then creates an instance of this class,
 * using its no-arg constructor (which must exist).  It registers
 * the instance with the POA, and prints the resulting IOR to 
 * standard output.  If anything goes wrong, a message starting
 * with the string "ERROR" is instead written to standard output.
 * <p>
 * This program is intended to be used with a 
 * {@link ClientServerSetup ClientServerSetup}.  To read and process
 * the <code>TestServer</code>'s output from another program such as 
 * the above, JacORB's normal diagnostic messages should be completely 
 * silenced.  This must be done using the normal configuration settings, 
 * e.g. from the command line (see 
 * {@link ClientServerSetup#setUp ClientServerSetup.setUp()} for an
 * example).
 * <p>
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class TestServer
{
    public static void main (String[] args)
    {
        try 
        {            
            //init ORB
            ORB orb = ORB.init( args, null );

            //init POA
            POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
            poa.the_POAManager().activate();

            String className = args[0];
            Class servantClass = Class.forName (className);
            Servant servant = ( Servant ) servantClass.newInstance();

            // create the object reference
            org.omg.CORBA.Object obj = poa.servant_to_reference( servant );

            System.out.println( orb.object_to_string( obj ));
            System.out.flush();
    
            // wait for requests
            orb.run();
        }
        catch( Exception e ) 
        {
            System.out.println( "ERROR: " + e );
        }
    }
}
