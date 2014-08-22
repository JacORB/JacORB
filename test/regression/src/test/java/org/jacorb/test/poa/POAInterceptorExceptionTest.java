package org.jacorb.test.poa;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.poa.except.POAInternalError;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.UNKNOWN;

/**
 * @author Nick Cross
 *
 * Verify an Error does not kill the requestprocessor.
 */
public class POAInterceptorExceptionTest extends ClientServerTestCase
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
        client_props.setProperty
            ("jacorb.connection.client.pending_reply_timeout", "5000");

        Properties server_props = new Properties();
        server_props.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                  + POAInterceptorExceptionInitializer.class.getName(), "" );



        setup = new ClientServerSetup(
                                                         "org.jacorb.test.orb.BasicServerImpl",
                                                         client_props,
                                                         server_props);
    }

    @Test (expected=UNKNOWN.class)
    public void test_poaex()
    {
        server.ping();
    }

   public static class POAInterceptorExceptionInitializer
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
        }

        public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            throw new POAInternalError("Error");
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
        }
    }
}
