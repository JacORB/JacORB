package org.jacorb.test.bugs.bug1091;

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

import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.test.bugs.bug983.Hello;
import org.jacorb.test.bugs.bug983.HelloHelper;
import org.jacorb.test.bugs.bug983.HelloImpl;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.IIOPAddressInterceptor;
import org.jacorb.test.orb.IIOPProfileORBInitializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.portable.Delegate;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedProfile;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test exposes a bug when an IOR has two identical profiles and a
 * TRANSIENT exception is received because the server is offline. JacORB
 * sees that more than one profile is available and attempts to change
 * profiles. Since they're equal, another TRANSIENT should be received on the
 * next call but instead we get a COMM_FAILURE because ParsedIOR's
 * effectiveProfile gets nullified.
 */
public class Bug1091Test extends FixedPortClientServerTestCase {
    private static final String host = "127.0.0.1";
    private Hello server;
    private String firstIOR;

    public static void main(String[] args) throws Exception {
        //init ORB
        ORB orb = ORB.init(args, null);

        IIOPAddressInterceptor.alternateAddresses.add(new IIOPAddress(host,
            Integer.parseInt(args[1])));

        //init POA
        POA rootPOA =
            POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        org.omg.CORBA.Policy[] policies = new org.omg.CORBA.Policy[2];
        policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue
            .USER_ID);
        policies[1] = rootPOA.create_lifespan_policy(LifespanPolicyValue
            .PERSISTENT);
        POA persistentPOA = rootPOA.create_POA("PersistentPOA", rootPOA
            .the_POAManager(), policies);
        persistentPOA.the_POAManager().activate();

        HelloImpl server = new HelloImpl();
        persistentPOA.activate_object_with_id("Hello".getBytes(), server);
        org.omg.CORBA.Object obj = persistentPOA.servant_to_reference(server);

        System.out.println("SERVER IOR: " + orb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        orb.run();
    }

    /**
     * Tests the number of IOP profiles in given IOR.
     */
    private static void testNumberOfIIOPProfiles(int numberExpected, org.omg
        .CORBA.Object obj) {
        // try to get ORB delegate to object
        org.jacorb.orb.Delegate jacOrbDelegate;
        Delegate localObj = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate();
        jacOrbDelegate = (org.jacorb.orb.Delegate) localObj;

        org.omg.IOP.IOR ior = jacOrbDelegate.getIOR();

        TaggedProfile[] profiles = ior.profiles;
        int nrOfIOPProf = 0;
        for (TaggedProfile profile : profiles) {
            if (profile.tag == TAG_INTERNET_IOP.value) {
                nrOfIOPProf++;
            }
        }
        assertEquals(numberExpected, nrOfIOPProf);
    }

    /**
     * Tests if given host and port equal values in given IOR.
     * Since several IOP profiles may be coded in IOR, an position must be specified.
     * Position must be: 0 <= pos < max_number_of_profiles
     */
    private static void testHostAndPortInIIOPProfile(org.omg.CORBA.Object obj,
                                                     int pos, String host, int port) {
        // try to get ORB delegate to object
        org.jacorb.orb.Delegate jacOrbDelegate;
        Delegate localObj = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate();
        jacOrbDelegate = (org.jacorb.orb.Delegate) localObj;

        org.omg.IOP.IOR ior = jacOrbDelegate.getIOR();

        TaggedProfile[] profiles = ior.profiles;
        int cnt = pos;
        boolean found = false;
        for (TaggedProfile profile : profiles) {
            if (profile.tag == TAG_INTERNET_IOP.value) {
                if (cnt == 0) {
                    IIOPProfile prof = new IIOPProfile(profile.profile_data);
                    assertEquals(host, ((IIOPAddress) prof.getAddress()).getIP());
                    assertEquals(port, ((IIOPAddress) prof.getAddress()).getPort());
                    found = true;
                    break;
                }
                cnt--;
            }
        }
        assertTrue(found);
    }

    @Before
    public void setUp() throws Exception {
        Properties serverProps = new Properties();
        int port = getNextAvailablePort();
        serverProps.setProperty("OAAddress", "iiop://" + host + ":" + port);
        serverProps.setProperty("jacorb.implname", "myimpl");
        serverProps.setProperty
            ("org.omg.PortableInterceptor.ORBInitializerClass.IIOPProfileORBInitializer",
                IIOPProfileORBInitializer.class.getName());

        setup = new ClientServerSetup(org.jacorb.test.bugs.bug1091.Bug1091Test.class.getName(),
            HelloImpl.class.getName(), new String[]{"" + port},
            null, serverProps);
        firstIOR = setup.getServerIOR();
        server = HelloHelper.narrow(setup.getClientOrb().string_to_object
            (firstIOR));
        testNumberOfIIOPProfiles(2, server);
        testHostAndPortInIIOPProfile(server, 0, host, port);
        testHostAndPortInIIOPProfile(server, 1, host, port);
    }

    @After
    public void tearDown() throws Exception {
        setup.tearDown();
    }

    @Test
    public void test_ping() throws Exception {
        TestUtils.getLogger().debug("sayHello call, should proceed normally.");
        server.sayHello();
        TestUtils.getLogger().debug("Shutting down server...");
        server.sayGoodbye();
        setup.tearDownServer();
        Thread.sleep(1000);
        TestUtils.getLogger().debug("sayHello again, should fail with TRANSIENT.");
        boolean failed = false;
        try {
            server.sayHello();
        } catch (TRANSIENT ignored) {
            TestUtils.getLogger().debug("Got TRANSIENT as expected.");
            failed = true;
        }
        Assert.assertTrue(failed);
        TestUtils.getLogger().debug("sayHello once more, should fail with " +
            "TRANSIENT. If bug is present, will fail with COMM_FAILURE.");
        failed = false;
        try {
            server.sayHello();
        } catch (TRANSIENT ignored) {
            TestUtils.getLogger().debug("Got TRANSIENT as expected.");
            failed = true;
        } catch (COMM_FAILURE bug) {
            Assert.fail("Bug exposed, error should be TRANSIENT.");
        }
        Assert.assertTrue(failed);
        TestUtils.getLogger().debug("Recreating server...");
        setup.reCreateServer();
        Thread.sleep(1000);
        Assert.assertEquals(firstIOR, setup.getServerIOR());
        TestUtils.getLogger().debug("sayHello call, should proceed normally.");
        server.sayHello();
    }
}
