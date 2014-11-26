package org.jacorb.test.bugs.bug384;

/*
 *        JacORB  - a free Java ORB
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

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class BImpl extends BPOA
{
   public BImpl()
   {
   }

   public static void main(String[] args) throws Exception
   {
        System.setProperty ("jacorb.implname", "BImpl");

        if( args.length != 0 )
        {
            System.out.println(
                "Usage: jaco org.jacorb.test.bugs.bug384.BImpl");
            System.exit( 1 );
        }

        //init ORB
        ORB orb = ORB.init( args, null );

        //init POA
        POA poa =
        POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

        poa.the_POAManager().activate();

        BImpl s = new BImpl();

        // create the object reference
        org.omg.CORBA.Object obj =
        poa.servant_to_reference( s );

        String ior = orb.object_to_string( obj );

        System.out.println( "IOR is " + ior );

        // wait for requests
        // orb.run();
    }
}
