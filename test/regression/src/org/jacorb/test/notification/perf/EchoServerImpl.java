package org.jacorb.test.notification.perf;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */

import org.jacorb.test.notification.EchoServerPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * EchoServerImpl.java
 *
 *
 * Created: Mon Apr  7 15:38:18 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EchoServerImpl extends EchoServerPOA
{

    public void acceptAny( Any any )
    {}

    public static void main( String[] args ) throws Exception
    {
        ORB _orb = ORB.init( args, null );
        POA _poa = POAHelper.narrow( _orb.resolve_initial_references( "RootPOA" ) );

        _poa.the_POAManager().activate();

        EchoServerImpl _server = new EchoServerImpl();

        // create the object reference
        org.omg.CORBA.Object obj = _poa.servant_to_reference( _server );


        //         NamingContextExt _nc =
        //             NamingContextExtHelper.narrow( _orb.resolve_initial_references( "NameService" ) );

        //         String _factoryName = "EchoServer";

        //         _nc.rebind( _nc.to_name( _factoryName ), obj );



        System.out.println( _orb.object_to_string( obj ) );
        System.out.flush();

        // wait for requests
        _orb.run();
    }

} // EchoServerImpl
