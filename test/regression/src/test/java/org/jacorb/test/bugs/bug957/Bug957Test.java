package org.jacorb.test.bugs.bug957;

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

import java.util.Properties;
import org.jacorb.sasPolicy.SASPolicyValues;
import org.jacorb.sasPolicy.SASPolicyValuesHelper;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.jacorb.security.sas.GssUpContext;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * @author Nick Cross
 *
 * Verify SAS.
 */
public class Bug957Test extends ClientServerTestCase
{
    private BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        GssUpContext.setUsernamePassword("testUser", "testPass");

        Properties props = new Properties();

        props.setProperty
            ("jacorb.security.sas.contextClass", "org.jacorb.security.sas.GssUpContext");
        props.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.SAS", "org.jacorb.security.sas.SASInitializer");

        setup = new ClientServerSetup( Bug957Test.class.getName(), "org.jacorb.test.orb.BasicServerImpl", props, props );
    }

    @Test
    public void test_ping()
    {
        server.ping();
    }


   /**
    * @param args a <code>String[]</code> value
    */
   public static void main (String[] args) throws Exception
    {
        //init ORB
        ORB orb = ORB.init( args, null );

        //init POA
        POA rootPOA =
        POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

        org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[3];
        policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
        policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        Any sasAny = orb.create_any();
        SASPolicyValuesHelper.insert( sasAny, new SASPolicyValues(EstablishTrustInClient.value, EstablishTrustInClient.value, true) );
        policies[2] = orb.create_policy(SAS_POLICY_TYPE.value, sasAny);
        POA securePOA = rootPOA.create_POA("SecurePOA", rootPOA.the_POAManager(), policies);
        rootPOA.the_POAManager().activate();

        BasicServerImpl server = new BasicServerImpl(orb);
        securePOA.activate_object_with_id("SecureObject".getBytes(), server);
        org.omg.CORBA.Object obj = securePOA.servant_to_reference(server);

        System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        orb.run();
    }
}
