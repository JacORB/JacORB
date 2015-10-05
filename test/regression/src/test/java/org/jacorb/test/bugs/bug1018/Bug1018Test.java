package org.jacorb.test.bugs.bug1018;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2015 Gerald Brose / The JacORB Team.
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
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.NameServiceSetup;
import org.jacorb.test.harness.FixedPortClientServerTestCase;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageImpl;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.slf4j.Logger;

public class Bug1018Test extends FixedPortClientServerTestCase
{
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    static org.omg.CORBA.Object echoRef = null;

    private static int nsPort = getNextAvailablePort();
    private static int serverPort = getNextAvailablePort();
    private static NameServiceSetup nsSetup = null;
    private static ServerSetup serverSetup = null;
    private Properties serverProps = new Properties();
    private ORBTestCase clientORBTestCase = new ORBTestCase()
    {
        @Override
        protected void patchORBProperties(Properties props) throws Exception
        {
            props.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
            props.setProperty("jacorb.connection.client.pending_reply_timeout","100");
            props.setProperty("jacorb.retries", "2");
            props.setProperty("jacorb.connection.client.connect_timeout","1000");
            props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.ORBInit",
                               Initializer.class.getName());
        }
    };

    @After
    public void tearDown() throws Exception
    {
      setup.tearDown();
      clientORBTestCase.ORBTearDown();
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception
    {
        if (nsSetup != null)
        {
            nsSetup.tearDown();
        }
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
        Properties nsProps = new Properties ();
        nsProps.setProperty ("OAPort",Integer.toString (nsPort));
        nsProps.setProperty("OAIAddr","localhost");
        nsSetup = new NameServiceSetup (folder,nsProps,1);
        nsSetup.setUp();
    }

    @Before
    public void setUp() throws Exception
    {
        clientORBTestCase.ORBSetUp();

        serverProps.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
        serverProps.setProperty ("jacorb.test.timeout.server", Long.toString(15000));
        serverProps.setProperty ("OAIAddr","localhost");
        serverProps.setProperty ("OAPort",Integer.toString (serverPort));
        serverProps.setProperty ("jacorb.ior_proxy_host","127.0.1.1");
        serverProps.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.ORBInit",
                                 Initializer.class.getName());
        setup = new ClientServerSetup (Bug1018Test.class.getName(),
                                       "Bug1018TestServer",
                                       null,
                                       serverProps);
    }


    public static void main(String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init(args, null);

            //init POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            //register Factory with the naming service
            NamingContextExt nc =
                    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            try
            {
                nc.bind_new_context (nc.to_name("bug1018"));
            }
            catch (Exception e) {}

            EchoMessageImpl echoServant = new EchoMessageImpl("test server");

            poa.activate_object(echoServant);
            echoRef = poa.servant_to_reference(echoServant);

            nc.rebind(nc.to_name("bug1018/echo"), echoRef);
            ((org.jacorb.orb.ORB)orb).addObjectKey ("bug1018", echoRef);

            poa.the_POAManager().activate();
            orb.run();
        }
        catch (Exception e)
        {
        }
    }


    /**
     *
     *
     */
    @Test
    public void testServerForwards ()
    {
        doTest ("corbaloc:iiop:localhost:" + serverPort + "/bug1018");
    }

    /**
     *
     *
     */
    @Test
    public void testServerMissing ()
    {
        try {
          setup.tearDown();
        }
        catch (Exception e) {
        }

        doTest("corbaname:iiop:localhost:" + nsPort + "/NameService#bug1018/echo");
     }

    private void doTest (String ior)
    {
        boolean expectReply = false;
        try
        {
            EchoMessage em = EchoMessageHelper.narrow(clientORBTestCase.getORB().string_to_object(ior));
            em.echo_simple();
            if (!expectReply) {
                fail("expected to get a TRANSIENT, got no exception");
            }
        }
        catch (TRANSIENT t)
        {
            if (expectReply) {
                fail("expected a reply, got a TRANSIENT");
            }

        }
        catch (TIMEOUT t)
        {
            if (expectReply) {
                fail("expected a reply, got a TIMEOUT");
            }

        }
        catch (Exception e)
        {
            TestUtils.getLogger().debug("dotest",e);
            fail("got unexpected " + e);
        }

    }

}
