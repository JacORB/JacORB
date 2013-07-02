/*
 *        JacORB  - a free Java ORB
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

package org.jacorb.test.ir;

import junit.framework.Test;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;

/**
 * an example how IR tests should look like
 *
 * @author Alphonse Bendt
 */
public class IRServerTest extends AbstractIRServerTestCase
{
    public IRServerTest(String name, IFRServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        // the IR will be started and get the contents of the specified IDL file fed.
        // NOTE: there are various suite methods available in AbstractIRServerTestCase
        // that should help setup a test quickly.
        return suite("BasicServer.idl", IRServerTest.class);
    }

    public void testStart() throws Exception
    {
        assertFalse(repository._non_existent());
    }

    public void testQueryIFR() throws Exception
    {
        assertNotNull(repository.lookup_id(BasicServerHelper.id()));
    }

    public void testAccessIFR() throws Exception
    {
        // we're registering a BasicServer in the client ORB/POA here. the client ORB is properly configured
        // so that it knows how to contact the running IR.
        BasicServer server = BasicServerHelper.narrow(getClientRootPOA().servant_to_reference(new BasicServerImpl()));

        InterfaceDef interfaceDef = InterfaceDefHelper.narrow(server._get_interface_def());

        assertNotNull(interfaceDef);
    }
}
