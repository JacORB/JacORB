package org.jacorb.test.bugs.bug1013;

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
import java.io.File;
import java.util.Properties;
import org.jacorb.orb.util.PrintIOR;
import org.jacorb.test.harness.FixedPortORBTestCase;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Bug1013Test extends FixedPortORBTestCase
{
    private static final int SVR_1_ON = 1;
    private static final int SVR_2_ON = 1;
    private static final int SVR_1_ALT = 2;
    private static final int SVR_1_OFF = 0;
    private static final int SVR_2_OFF = 0;

    private static final String IMPLNAME = "EchoServer";
    private static final String SERVER_1_LEP = "iiop://:" + getNextAvailablePort();
    private static final String SERVER_2_LEP = "iiop://:" + getNextAvailablePort();

    // ImR endpoints
    private static final String IMR_1_LEP = Integer.toString(getNextAvailablePort());

    private Properties imrProp_1 = null;
    private File imrIOR_1 = null;
    private File imrTable_1 = null;
    private ImRServiceSetup imrSetup_1 = null;
    private ServerSetup serverSetup_1 = null;
    private ServerSetup serverSetup_2 = null;
    private final java.lang.Object syncTest = new java.lang.Object();
    private boolean testComplete;


    @After
    public void tearDown() throws Exception
    {
        teardownMyServers(SVR_1_OFF, SVR_2_OFF);
        teardownMyImR();

        Thread.sleep (5000);
    }

    public void teardownMyImR() throws Exception
    {
        if (imrSetup_1 != null)
        {
            imrSetup_1.tearDown();
            imrSetup_1 = null;
            Thread.sleep (3000);
        }
    }

    public void setupMyImR ()
    {
        if (imrSetup_1 == null)
        {
            try
            {
                TestUtils.getLogger().debug("++++ setting ImR #1"==null? "null" : "++++ setting ImR #1");
                if (imrProp_1 == null)
                {
                    // initialize ImR #1 properties
                    imrProp_1 = new Properties();
                    imrProp_1.setProperty ("jacorb.imr.endpoint_port_number", IMR_1_LEP);
                    imrProp_1.setProperty ("jacorb.imr.allow_auto_register", "on");

                    imrIOR_1 = File.createTempFile("MyImR1", ".ior");
                    imrIOR_1.deleteOnExit();
                    imrTable_1 = File.createTempFile("MyImR1_table", ".dat");
                    imrTable_1.deleteOnExit();

                    imrProp_1.setProperty ("jacorb.imr.ior_file", imrIOR_1.toString());
                    imrProp_1.setProperty ("jacorb.imr.table_file", imrTable_1.toString());
                    imrProp_1.setProperty ("jacorb.imr.connection_timeout", "5000");
                    imrProp_1.setProperty ("jacorb.test.timeout.server", Long.toString(10000));
                    imrProp_1.setProperty ("jacorb.connection.server.reuse_address", "true");

                    imrProp_1.setProperty("jacorb.log.showThread", "on");
                    imrProp_1.setProperty("jacorb.log.showSrcInfo", "on");
                }

                // initiate the ImR service
                imrSetup_1 = new ImRServiceSetup (imrProp_1, 1);
                imrSetup_1.setUp();
                TestUtils.getLogger().debug("++++ setting ImR #1 - complete"==null? "null" : "++++ setting ImR #1 - complete");
            }
            catch (Exception e)
            {
                // ignored
            }
        }
    }

    public void teardownMyServers(int svrOff_1, int svrOff_2) throws Exception
    {
        if (svrOff_1 == SVR_1_OFF && serverSetup_1 != null)
        {
            serverSetup_1.tearDown();
            serverSetup_1 = null;
        }

        if (svrOff_2 == SVR_1_OFF && serverSetup_2 != null)
        {
            serverSetup_2.tearDown();
            serverSetup_2 = null;
        }
        Thread.sleep (3000);
    }

    public void setupMyServers(int serverOn_1, int serverOn_2) throws Exception
    {
        if (serverOn_1 != SVR_1_OFF && serverSetup_1 == null)
        {
            TestUtils.getLogger().debug("++++ setting up server 1"==null? "null" : "++++ setting up server 1");
            serverSetup_1 = new ServerSetup (
                "org.jacorb.test.bugs.bug1013.Bug1013TestServer",
                "",
                new String []
                {
                    "-Djacorb.log.showThread=" + "on",
                    "-Djacorb.log.showSrcInfo=" + "on",
                    "-ORBListenEndpoints", (serverOn_1 == SVR_1_ON ? SERVER_1_LEP : SERVER_2_LEP),
                    "-DORBInitRef.ImplementationRepository=" + "file://" + imrIOR_1.toString(),
                    "-Djacorb.implname=" + IMPLNAME,
                    "-Djacorb.use_imr=" + "true",
                    "-Djacorb.connection.server.reuse_address=true",
                    "-Djacorb.use_tao_imr=" + "false",
                    "-Djacorb.test.timeout.server=" + Long.toString(10000)
                },
                null);

            serverSetup_1.setUp();
            TestUtils.getLogger().debug("++++ setting up server 1 - complete"==null? "null" : "++++ setting up server 1 - complete");
        }
        if (serverOn_2 == SVR_2_ON && serverSetup_2 == null)
        {
            TestUtils.getLogger().debug("++++ setting up server 2"==null? "null" : "++++ setting up server 2");
            serverSetup_2 = new ServerSetup (
                "org.jacorb.test.bugs.bug1013.Bug1013TestServer",
                "",
                new String []
                {
                    "-Djacorb.log.showThread=" + "on",
                    "-Djacorb.log.showSrcInfo=" + "on",
                    "-ORBListenEndpoints", SERVER_1_LEP,
                    "-Djacorb.connection.server.reuse_address=true",
                    "-Djacorb.use_imr=" + "false",
                    "-Djacorb.use_tao_imr=" + "false",
                    "-Djacorb.test.timeout.server=" + Long.toString(10000)
                },
                null);

            serverSetup_2.setUp();
            TestUtils.getLogger().debug("++++ setting up server 2 - complete"==null? "null" : "++++ setting up server 2 - complete");
        }
    }

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("jacorb.use_imr", "true");
        props.setProperty ("jacorb.use_tao_imr", "false");
        props.setProperty ("jacorb.retries", "3");
        props.setProperty ("jacorb.retry_interval", "500");
        props.setProperty ("jacorb.connection.client.connect_timeout","3000");
        props.setProperty ("jacorb.test.timeout.server", Long.toString(10000));
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Before
    public void setUp() throws Exception
    {
        // initiate ImR's
        setupMyImR();
        Thread.sleep(5000);
        setupMyServers(SVR_1_ON, SVR_2_OFF);
    }

    /**
     * This test will start server 1 on port 1 and connect via the IMR,
     * client forces a server-raised OBJECT_NOT_EXIST exception. Client
     * expects to catch the exception
     */
    @Test
    public void test_1_force_ONE()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_1 = null;

        try
        {
            org.omg.CORBA.Object obj;
            String ior_1;

            TestUtils.getLogger().debug("++++ test_force_ONE: ping server 1"==null? "null" : "++++ test_force_ONE: ping server 1");
            ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_force_ONE: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            obj = orb.string_to_object(ior_1);
            assertTrue("test_force_ONE: couldn't generate server #1's obj using IOR: < " + ior_1 + " >", obj != null);
            server_1 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);

            server_1.force_ONE ();
            fail("test_force_ONE: expected OBJECT_NOT_EXIST");
        }
        catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
        {
            assertTrue ("test_force_ONE: expected minor code of 0", e.minor == 0);
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_force_ONE: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_force_ONE: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server_1 = null;
        }
    }

    /**
     * This test will start server 1 on port 1 and connect via the IMR,
     * client forces a server-raised OBJECT_NOT_EXIST exception. Client
     * expects to catch the exception
     */
    @Test
    public void test_1_rebind()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_1 = null;

        try
        {
            org.omg.CORBA.Object obj;
            String ior_1;

            TestUtils.getLogger().debug("++++ test_rebind: ping server 1"==null? "null" : "++++ test_rebind: ping server 1");
            ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_rebind: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            obj = orb.string_to_object(ior_1);
            assertTrue("test_rebind: couldn't generate server #1's obj using IOR: < " + ior_1 + " >", obj != null);
            server_1 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);

            server_1.ping ();

            teardownMyServers (SVR_1_OFF, SVR_2_OFF);
            Thread.sleep(2000);
            setupMyServers(SVR_1_ALT, SVR_2_ON);
            Thread.sleep(2000);

            TestUtils.getLogger().debug("++++ test_rebind: second ping server 1"==null? "null" : "++++ test_rebind: second ping server 1");
            server_1.ping ();
        }
        catch (org.omg.CORBA.OBJECT_NOT_EXIST e)
        {
            fail("test_rebind: got an OBJECT_NOT_EXIST exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_rebind: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_rebind: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        catch (Exception e)
        {
            fail("test_rebind: caught " + e);
        }
        finally
        {
            server_1 = null;
        }
    }

}
