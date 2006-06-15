package org.jacorb.test.orb.etf;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.orb.ORBConstants;
import org.jacorb.orb.iiop.IIOPFactories;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
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
public class SpecificProfileSelectorTest extends ClientServerTestCase
{
    private BasicServer server;
    private ORB clientOrb;

    public SpecificProfileSelectorTest(String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public void setUp() throws Exception
    {
        WIOPFactories.setTransportInUse(false);
        final Object serverObject = setup.getServerObject();

        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        // need a unbound delegate for every testrun.
        clientOrb = ORB.init(new String[0], clientProps);
        server = BasicServerHelper.narrow(clientOrb.string_to_object(clientOrb.object_to_string(serverObject)));
    }

    public void tearDown() throws Exception
    {
        clientOrb.shutdown(true);
        WIOPFactories.setTransportInUse(false);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Profile Selector");

        // client ORB from setup is not used.
        Properties clientProps = null;

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

        server._set_policy_override(new Policy[] {policy}, SetOverrideType.SET_OVERRIDE);

        server.ping();
        assertTrue("should use WIOP as transport", WIOPFactories.isTransportInUse());
    }

    public void testClientProtocolPolicyIIOP() throws Exception
    {
        RTORB rtORB = RTORBHelper.narrow(clientOrb.resolve_initial_references("RTORB"));

        ClientProtocolPolicy policy = rtORB.create_client_protocol_policy(new Protocol[] {new Protocol(ORBConstants.JAC_NOSSL_PROFILE_ID, null, null)});

        server._set_policy_override(new Policy[] {policy}, SetOverrideType.SET_OVERRIDE);

        server.ping();

        assertFalse("shouldn't use WIOP if IIOP is selected", WIOPFactories.isTransportInUse());
    }
}
