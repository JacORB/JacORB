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
import org.jacorb.test.harness.NameServiceSetup;
import org.jacorb.test.harness.FixedPortORBTestCase;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.slf4j.Logger;

public class Bug1018Test extends FixedPortORBTestCase
{
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    private static int nsPort = getNextAvailablePort();
    private static String ior = "";
    private static NameServiceSetup nsSetup = null;
    private static ServerSetup serverSetup = null;

    @After
    public void tearDown() throws Exception
    {
      if (serverSetup != null)
      {
        serverSetup.tearDown ();
      }
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception
    {
        if (nsSetup != null)
        {
            nsSetup.tearDown();
        }
    }


    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
        props.setProperty("jacorb.connection.client.pending_reply_timeout","100");
        props.setProperty("jacorb.retries", "2");
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
        Properties nsProps = new Properties ();
        nsProps.setProperty ("OAPort",Integer.toString (nsPort));
        nsProps.setProperty("OAIAddr","localhost");
        ior = "corbaname:iiop:localhost:" + nsPort + "/NameService#bug1018/echo";
        nsSetup = new NameServiceSetup (folder,nsProps,1);
        nsSetup.setUp();
    }

    @Before
    public void setUp() throws Exception
    {
        Properties serverprops = new Properties();
        serverprops.setProperty ("ORBInitRef.NameService", nsSetup.getServerIOR());
        serverprops.setProperty("jacorb.test.timeout.server", Long.toString(15000));
        serverprops.setProperty("OAIAddr","localhost");

        serverSetup = new ServerSetup (
                "org.jacorb.test.bugs.bug1018.Bug1018TestServer",
                "",
                serverprops);

        serverSetup.setUp();
    }

    /**
     *
     *
     */
    @Test
    public void testServerIgnores ()
    {
        doTest(false);
    }

    /**
     *
     *
     */
    @Test
    public void testServerMissing ()
    {
        try {
            serverSetup.tearDown();
        }
        catch (Exception e) {
        }
        serverSetup = null;
        doTest(false);
     }

    private void doTest (boolean expectReply)
    {
        try
        {
            EchoMessage em = EchoMessageHelper.narrow(orb.string_to_object(ior));
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
            ((org.jacorb.orb.ORB)orb).getLogger().debug("dotest",e);
            fail("got unexpected " + e);
        }

    }

}
