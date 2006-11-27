package org.jacorb.test.orb.etf;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.orb.ORBConstants;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
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
 * @version $Id$
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
        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

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
        clientProps.setProperty("jacorb.regression.disable_security",
                                "true");
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

        basicServer._set_policy_override(new Policy[] {policy}, SetOverrideType.SET_OVERRIDE);

        basicServer.ping();
        assertTrue("should use WIOP as transport", WIOPFactories.isTransportInUse());
    }

    public void testClientProtocolPolicyIIOP() throws Exception
    {
        RTORB rtORB = RTORBHelper.narrow(clientOrb.resolve_initial_references("RTORB"));

        ClientProtocolPolicy policy = rtORB.create_client_protocol_policy(new Protocol[] {new Protocol(ORBConstants.JAC_NOSSL_PROFILE_ID, null, null)});

        basicServer._set_policy_override(new Policy[] {policy}, SetOverrideType.SET_OVERRIDE);

        basicServer.ping();

        assertFalse("shouldn't use WIOP if IIOP is selected", WIOPFactories.isTransportInUse());
    }
}
