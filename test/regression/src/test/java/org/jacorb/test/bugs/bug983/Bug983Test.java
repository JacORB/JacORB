package org.jacorb.test.bugs.bug983;

/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 2000-2014 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Bug983Test extends FixedPortClientServerTestCase
{
    private Properties serverProps = new Properties();
    private ORBTestCase clientORBTestCase = new ORBTestCase()
    {
        @Override
        protected void patchORBProperties(Properties props) throws Exception
        {
            props.setProperty("jacorb.connection.client.pending_reply_timeout", "2000");
            props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                    + "ORBInit", Initializer.class.getName());
        }
    };

    @Before
    public void setUp() throws Exception
    {
        clientORBTestCase.ORBSetUp();

        int port = getNextAvailablePort();
        serverProps.setProperty("OAPort", Integer.toString(port));
        serverProps.setProperty("jacorb.implname", "myimpl");
        serverProps.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                + "ORBInit", Initializer.class.getName());

        setup = new ClientServerSetup(Bug983Test.class.getName(),
                "org.jacorb.test.bugs.bug983.HelloImpl",
                new String[] { "-ORBListenEndpoints", "'iiop://:" + port + ",iiop://:" +
                getNextAvailablePort() + "'"},
                null,
                serverProps);
    }

    @After
    public void tearDown() throws Exception
    {
        setup.tearDown();
        clientORBTestCase.ORBTearDown();
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    public static void main(String[] args) throws Exception
    {
        // init ORB
        ORB orb = ORB.init(args, null);

        // init POA
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];
        policies[0] = rootPOA
                .create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
        policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);

        POA childPOA = rootPOA.create_POA("Child", rootPOA.the_POAManager(), policies);
        childPOA.the_POAManager().activate();

        HelloImpl server = new HelloImpl();
        childPOA.activate_object_with_id("Hello".getBytes(), server);
        org.omg.CORBA.Object obj = childPOA.servant_to_reference(server);

        TestUtils.getLogger().debug("IOR is " + orb.object_to_string(obj));
        System.out.println("SERVER IOR: " + orb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        orb.run();
    }

    @Test
    public void test_reconnect_restarted_server_forward_request() throws Exception
    {
        org.omg.CORBA.Object obj = clientORBTestCase.getORB().string_to_object(
                setup.getServerIOR());

        Hello reference = HelloHelper.narrow(obj);

        reference._non_existent();
        reference.sayHello();
        reference.sayGoodbye();

        TestUtils.getLogger().debug("waiting for reactivation...");

        setup.tearDown();
        setup = new ClientServerSetup(Bug983Test.class.getName(),
                "org.jacorb.test.bugs.bug983.HelloImpl", null, serverProps);

        try
        {
            reference.sayHello();
            reference.sayGoodbye();
            TestUtils.getLogger().debug("Worked fine!");
        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("Failed to call server", e);
            fail("Failed to call restarted server" + e);
        }

        setup.tearDown();

        try
        {
            reference.sayHello();

            fail ("Should have been down");
        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("Ok, it was down", e);
        }

        TestUtils.getLogger().debug("waiting for reactivation...");

        setup = new ClientServerSetup(Bug983Test.class.getName(),
                "org.jacorb.test.bugs.bug983.HelloImpl", null, serverProps);

        try
        {
            reference.sayHello();
            TestUtils.getLogger().debug("### Worked fine!");
        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("Why didn't worked?", e);
            fail("Failed to call restarted server" + e);
        }

    }
}
