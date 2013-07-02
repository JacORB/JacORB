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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

package org.jacorb.test.orb.etf;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.orb.ORBConstants;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
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

    public SpecificProfileSelectorTest(String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public void doSetUp() throws Exception
    {
        // need a unbound delegate for every testrun.
        clientOrb = setup.getClientOrb();
        basicServer = BasicServerHelper.narrow(clientOrb.string_to_object(clientOrb.object_to_string(server)));
    }

    public void doTearDown() throws Exception
    {
        basicServer._release();
        basicServer = null;
        clientOrb = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Profile Selector");

        // client ORB from setup is not used.
        Properties clientProps = new Properties();

        // WIOP does not support SSL.
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        clientProps.setProperty("jacorb.transport.factories",
                "org.jacorb.orb.iiop.IIOPFactories," +
                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        ClientServerSetup setup =
          new ClientServerSetup (suite,
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);

        TestUtils.addToSuite(suite, setup, SpecificProfileSelectorTest.class);

        return setup;
    }

    public void testClientProtocolPolicyWIOP() throws Exception
    {
        RTORB rtORB = RTORBHelper.narrow(clientOrb.resolve_initial_references("RTORB"));

        ClientProtocolPolicy policy = rtORB.create_client_protocol_policy(new Protocol[] {new Protocol(new WIOPFactories().profile_tag(), null, null)});

        basicServer = BasicServerHelper.narrow (basicServer._set_policy_override
                (new Policy[] {policy}, SetOverrideType.SET_OVERRIDE));

        basicServer.ping();
        assertTrue("should use WIOP as transport", WIOPFactories.isTransportInUse());
    }

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
