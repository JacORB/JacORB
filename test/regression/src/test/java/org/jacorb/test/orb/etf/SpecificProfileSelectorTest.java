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

package org.jacorb.test.orb.etf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.orb.ORBConstants;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import org.omg.RTCORBA.ClientProtocolPolicy;
import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;

/**
 * @author Alphonse Bendt
 */
public class SpecificProfileSelectorTest extends AbstractWIOPTestCase
{
    private BasicServer basicServer;
    private ORB clientOrb;


    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        // need a unbound delegate for every testrun.
        clientOrb = setup.getClientOrb();
        basicServer = BasicServerHelper.narrow(clientOrb.string_to_object(clientOrb.object_to_string(server)));
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        basicServer._release();
        basicServer = null;
        clientOrb = null;
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // WIOP does not support SSL.
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        // client ORB from setup is not used.
        Properties clientProps = new Properties();

        clientProps.setProperty("jacorb.transport.factories",
                "org.jacorb.orb.iiop.IIOPFactories," +
                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        setup = new ClientServerSetup(
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);

    }

    @Test
    public void testClientProtocolPolicyWIOP() throws Exception
    {
        RTORB rtORB = RTORBHelper.narrow(clientOrb.resolve_initial_references("RTORB"));

        ClientProtocolPolicy policy = rtORB.create_client_protocol_policy(new Protocol[] {new Protocol(new WIOPFactories().profile_tag(), null, null)});

        basicServer = BasicServerHelper.narrow (basicServer._set_policy_override
                (new Policy[] {policy}, SetOverrideType.SET_OVERRIDE));

        basicServer.ping();
        assertTrue("should use WIOP as transport", WIOPFactories.isTransportInUse());
    }

    @Test
    public void testClientProtocolPolicyIIOP() throws Exception
    {
        RTORB rtORB = RTORBHelper.narrow(clientOrb.resolve_initial_references("RTORB"));

        ClientProtocolPolicy policy = rtORB.create_client_protocol_policy(new Protocol[] {new Protocol(ORBConstants.JAC_NOSSL_PROFILE_ID, null, null)});

        basicServer = BasicServerHelper.narrow (basicServer._set_policy_override
                (new Policy[] {policy}, SetOverrideType.SET_OVERRIDE));

        basicServer.ping();

        assertFalse("shouldn't use WIOP if IIOP is selected", WIOPFactories.isTransportInUse());
    }
}
