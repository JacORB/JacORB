package org.jacorb.test.notification;

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

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.jacorb.util.Debug;

import org.apache.avalon.framework.logger.Logger;


/**
 * TestServer.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TestServer
{
    public static void main (String[] args) throws Exception
    {
        String _servantClassName = args[0];
        int _portToSendIorTo = Integer.parseInt(args[1]);

        Logger _logger = Debug.getNamedLogger(TestServer.class.getName());

        _logger.debug("Remote debug");
        _logger.info("Remote info");

        try
            {
                //init ORB
                ORB orb = ORB.init( args, null );

                //init POA
                POA poa =
                    POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
                poa.the_POAManager().activate();

                Class _servantClass = Class.forName (_servantClassName);

                Servant _servant = ( Servant ) _servantClass.newInstance();

                // create the object reference
                org.omg.CORBA.Object o = poa.servant_to_reference( _servant );

                Socket _socket = new Socket("localhost", _portToSendIorTo);

                PrintWriter _out = new PrintWriter(_socket.getOutputStream(), true);

                _out.println( orb.object_to_string(o));
                _out.flush();
                _out.close();
                _socket.close();

                // wait for requests
                orb.run();
        }
        catch( Throwable e )
            {
                _logger.fatalError("Error starting TestServer", e);
            }
    }
}

