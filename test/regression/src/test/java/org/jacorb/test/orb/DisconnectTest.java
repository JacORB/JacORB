package org.jacorb.test.orb;

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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.TIMEOUT;
import org.omg.ETF.Profile;

/**
 * @author Nick Cross
 *
 * Verifies that the connection map is cleared after a timeout and disconnect.
 */
public class DisconnectTest extends ClientServerTestCase
{
    protected BasicServer server = null;


    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.retries", "0");
        client_props.setProperty ("jacorb.retry_interval", "50");

        client_props.setProperty
            ("jacorb.connection.client.pending_reply_timeout", "5000");

        Properties server_props = new Properties();
        server_props.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                  + DisconnectInitializer.class.getName(), "" );



        setup = new ClientServerSetup(
                                                         "org.jacorb.test.orb.BasicServerImpl",
                                                         client_props,
                                                         server_props);
    }

    @Test
    public void test_disconnect() throws Exception
    {
        boolean timeout = false;
        try
        {
            server.ping();
        }
        catch (TIMEOUT e)
        {
            timeout = true;

            Field fconnmgr = Delegate.class.getDeclaredField("conn_mg");
            fconnmgr.setAccessible(true);
            Delegate d = (Delegate) ((org.omg.CORBA.portable.ObjectImpl)server)._get_delegate();
            ClientConnectionManager ccm = (ClientConnectionManager) fconnmgr.get(d);
            Field connections = ClientConnectionManager.class.getDeclaredField("connections");
            connections.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<Profile, ClientConnection> c = (HashMap<Profile, ClientConnection>) connections.get(ccm);

            assertTrue (c.size() == 0);
        }
        if ( ! timeout )
        {
            fail ("Did not catch a timeout");
        }
    }

   public static class DisconnectInitializer
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ORBInitializer
    {
        /**
         * Called before init of the actual ORB.
         *
         * @param info The ORB init info.
         */
        public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info)
        {
            try
            {
                info.add_server_request_interceptor (new ServerInterceptorA());
            }
            catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
            {
                fail ("unexpected exception received: " + ex);
            }
        }

        /**
         * Called after init of the actual ORB.
         *
         * @param info The ORB init info.
         */
        public void post_init (org.omg.PortableInterceptor.ORBInitInfo info)
        {
        }
    }

    static class ServerInterceptorA
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ServerRequestInterceptor
    {
        public java.lang.String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void receive_request_service_contexts(
            org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            TestUtils.getLogger().debug ("LocalServerInterceptorA - receive_request_service_contexts");
        }

        public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            TestUtils.getLogger().debug ("LocalServerInterceptorA - receive_request");
            try
            {
                Thread.sleep (10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
            }
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
            TestUtils.getLogger().debug ("LocalServerInterceptorA - send_reply");
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            TestUtils.getLogger().debug ("LocalServerInterceptorA - send_exception");
        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            TestUtils.getLogger().debug ("LocalServerInterceptorA - send_other");
        }
    }
}
