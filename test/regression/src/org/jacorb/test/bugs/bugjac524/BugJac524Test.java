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

package org.jacorb.test.bugs.bugjac524;

import java.util.Properties;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.orb.iiop.ClientIIOPConnection;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;

/**
 * @author Alphonse Bendt
 */
public class BugJac524Test extends ORBTestCase
{
    private static int PORT = 4810;
    private final int port = PORT++;
    private String ior;

    private Properties props;

    protected void patchORBProperties(String testName, Properties props) throws Exception
    {
        if (testName.indexOf("SSL") >= 0)
        {
            Properties serverProps = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");

            props.putAll(serverProps);

            props.setProperty("jacorb.security.ssl.server.required_options", "60");

            props.setProperty("OASSLPort", Integer.toString(port));
        }
        else
        {
            props.setProperty("OAPort", Integer.toString(port));
        }

        props.setProperty("jacorb.log.default.verbosity", "4");

        props.setProperty("jacorb.iiop.enable_loopback", "on");

        BugJac524TCPConnectionListener.reset();
        props.setProperty(SocketFactoryManager.TCP_LISTENER, BugJac524TCPConnectionListener.class.getName());

        if (testName.indexOf("ConnectToLoopbackShouldNotOpenConnection") >= 0)
        {
            props.setProperty("jacorb.ior_proxy_host", "127.0.0.1");
            props.setProperty("jacorb.ior_proxy_port", Integer.toString(port));
        }

        this.props = props;
    }

    protected final void doSetUp() throws Exception
    {
        org.omg.CORBA.Object server = rootPOA.servant_to_reference(new BasicServerImpl());

        ior = orb.object_to_string(server);
    }

    protected final void doTest()
    {
        int before = ClientIIOPConnection.openTransports;

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);

        BasicServer server = BasicServerHelper.narrow(orb.string_to_object (ior));

        long now = System.currentTimeMillis();

        assertEquals(now, server.bounce_long_long(now));
        assertTrue(BugJac524TCPConnectionListener.open.isEmpty());
        assertEquals(before + 1, ClientIIOPConnection.openTransports);

        server._release();
        orb.shutdown(true);
    }

    public void testConnectToLoopbackShouldNotOpenConnection() throws Exception
    {
        doTest();
    }

    public void testConnectToLoopbackShouldNotOpenConnectionSSL() throws Exception
    {
        doTest();
    }

    public void testConnectShouldNotOpenConnection() throws Exception
    {
        doTest();
    }

    public void testConnectShouldNotOpenConnectionSSL() throws Exception
    {
        doTest();
    }
}
