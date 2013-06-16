package org.jacorb.test.orb.localinterceptors;

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

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.test.orb.localinterceptors.LocalPITest.PIServerImpl;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;

/**
 * Verifies that calling is_local works in portable interceptors.
 */
public class LocalPIIsLocalTest extends TestCase
{
    private static PIServer serverRef = null;
    private static PIServer clientRef = null;

    private ORB orb;
    private POA rootPOA;

    private static String [] args = new String [0];

    public static Test suite ()
    {
        TestSuite suite = new TestSuite (LocalPIIsLocalTest.class);

        return suite;
    }

    private void init (String op)
    {
        try
        {
            Properties props = new java.util.Properties();
            props.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                               + LocalPIInitializer.class.getName(), "" );

            // find the root poa
            orb = ORB.init (args, props);

            rootPOA = (POA) orb.resolve_initial_references ("RootPOA");

            Policy [] policies = new Policy [1];

            policies[0] = rootPOA.create_implicit_activation_policy
                (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

            POA childPOA = rootPOA.create_POA ("childPOA",
                                               rootPOA.the_POAManager (),
                                               policies);

            serverRef = ( new PIServerImpl (childPOA))._this (orb);

            rootPOA.the_POAManager().activate();
            clientRef = PIServerHelper.narrow (serverRef);
        }
        catch ( org.omg.CORBA.UserException ex )
        {
            fail( "exception during setup:" + ex.toString() );
        }
    }


    public void testCompleteCall()
    {
        init ("sendMessage");

        clientRef.sendMessage ("A Message from testCompleteCall()...");

        orb.shutdown(false);
    }


    /**
     * An ORB initializer class.
     */
    public static class LocalPIInitializer
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
                info.add_client_request_interceptor (new LocalClientInterceptorA());
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


    static class LocalClientInterceptorA
        extends org.omg.CORBA.LocalObject
        implements org.omg.PortableInterceptor.ClientRequestInterceptor
    {
        public String name()
        {
            return "";
        }

        public void destroy()
        {
        }

        public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
            ClientRequestInfoImpl cr = (ClientRequestInfoImpl)ri;

            assertTrue (((org.omg.CORBA.portable.ObjectImpl)cr.effective_target())._is_local());

        }

        public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
        }

        public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
        {
        }

        public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
        }

        public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
            throws org.omg.PortableInterceptor.ForwardRequest
        {
        }
    }

}
