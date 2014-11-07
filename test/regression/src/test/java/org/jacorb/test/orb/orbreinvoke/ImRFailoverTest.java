package org.jacorb.test.orb.orbreinvoke;

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
public class ImRFailoverTest extends FixedPortORBTestCase
{
    private static final boolean IMR_1_ON = true;
    private static final boolean IMR_2_ON = true;
    private static final boolean IMR_1_OFF = false;
    private static final boolean IMR_2_OFF = false;

    private static final boolean SVR_1_ON = true;
    private static final boolean SVR_2_ON = true;
    private static final boolean SVR_1_OFF = false;
    private static final boolean SVR_2_OFF = false;

    private static final String IMPLNAME = "EchoServer";
    private static final String SERVER_1_LEP = "iiop://:" + getNextAvailablePort();
    private static final String SERVER_2_LEP = "iiop://:" + getNextAvailablePort();

    // ImR endpoints
    private static final String IMR_1_LEP = Integer.toString(getNextAvailablePort());
    private static final String IMR_2_LEP = Integer.toString(getNextAvailablePort());

    private Properties imrProp_1 = null;
    private Properties imrProp_2 = null;
    private File imrIOR_1 = null;
    private File imrIOR_2 = null;
    private File imrTable_1 = null;
    private File imrTable_2 = null;
    private ImRServiceSetup imrSetup_1 = null;
    private ImRServiceSetup imrSetup_2 = null;
    private ServerSetup serverSetup_1 = null;
    private ServerSetup serverSetup_2 = null;
    private final java.lang.Object syncTest = new java.lang.Object();
    private boolean testComplete;


    @After
    public void tearDown() throws Exception
    {
        teardownMyServers(SVR_1_OFF, SVR_2_OFF);
        teardownMyImRs(IMR_1_OFF, IMR_2_OFF);

        Thread.sleep (5000);
    }

    public void teardownMyImRs(boolean imrOff_1, boolean imrOff_2) throws Exception
    {
        if (imrOff_1 == IMR_1_OFF && imrSetup_1 != null)
        {
            imrSetup_1.tearDown();
            imrSetup_1 = null;
        }

        if (imrOff_2 == IMR_2_OFF && imrSetup_2 != null)
        {
            imrSetup_2.tearDown();
            imrSetup_2 = null;
        }
        Thread.sleep (3000);
    }

    public void setupMyImRs (boolean imrOn_1, boolean imrOn_2)
    {
        if (imrOn_1 == IMR_1_ON && imrSetup_1 == null)
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

        if (imrOn_2 == IMR_2_ON && imrSetup_2 == null)
        {
            try
            {
                TestUtils.getLogger().debug("++++ setting ImR #2"==null? "null" : "++++ setting ImR #2");
                if (imrProp_2 == null)
                {
                    // initialize ImR #1 properties
                    imrProp_2 = new Properties();
                    imrProp_2.setProperty ("jacorb.imr.endpoint_port_number", IMR_2_LEP);
                    imrProp_2.setProperty ("jacorb.imr.allow_auto_register", "on");

                    imrIOR_2 = File.createTempFile("MyImR2", ".ior");
                    imrIOR_2.deleteOnExit();
                    imrTable_2 = File.createTempFile("MyImR2_table", ".dat");
                    imrTable_2.deleteOnExit();

                    imrProp_2.setProperty ("jacorb.imr.ior_file", imrIOR_2.toString());
                    imrProp_2.setProperty ("jacorb.imr.table_file", imrTable_2.toString());
                    imrProp_2.setProperty ("jacorb.imr.connection_timeout", "5000");
                    imrProp_2.setProperty ("jacorb.test.timeout.server", Long.toString(10000));
                    imrProp_2.setProperty ("jacorb.connection.server.reuse_address", "true");

                    imrProp_2.setProperty("jacorb.log.showThread", "on");
                    imrProp_2.setProperty("jacorb.log.showSrcInfo", "on");
                }

                // initiate the ImR service
                imrSetup_2 = new ImRServiceSetup (imrProp_2, 2);
                imrSetup_2.setUp();
                TestUtils.getLogger().debug("++++ setting ImR #2 - complete"==null? "null" : "++++ setting ImR #2 - complete");
            }
            catch (Exception e)
            {
                //
            }
        }
    }

    public void teardownMyServers(boolean svrOff_1, boolean svrOff_2) throws Exception
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

    public void setupMyServers(boolean serverOn_1, boolean serverOn_2) throws Exception
    {
        if (serverOn_1 == SVR_1_ON && serverSetup_1 == null)
        {
                    TestUtils.getLogger().debug("++++ setting up server 1"==null? "null" : "++++ setting up server 1");
                    serverSetup_1 = new ServerSetup (
                                        "org.jacorb.test.orb.orbreinvoke.ImRFailoverTestServer",
                                        "",
                                        new String []
                                            {
                                                 "-Djacorb.log.showThread=" + "on",
                                                 "-Djacorb.log.showSrcInfo=" + "on",

                                                 "-ORBListenEndpoints", SERVER_1_LEP,
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
                                        "org.jacorb.test.orb.orbreinvoke.ImRFailoverTestServer",
                                        "",
                                        new String []
                                            {
                                                 "-Djacorb.log.showThread=" + "on",
                                                 "-Djacorb.log.showSrcInfo=" + "on",

                                                 "-ORBListenEndpoints", SERVER_2_LEP,
                                                 "-DORBInitRef.ImplementationRepository=" + "file://" + imrIOR_2.toString(),
                                                 "-Djacorb.connection.server.reuse_address=true",
                                                 "-Djacorb.implname=" + IMPLNAME,
                                                 "-Djacorb.use_imr=" + "true",
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
        setupMyImRs(IMR_1_ON, IMR_2_ON);
        Thread.sleep(5000);
        setupMyServers(SVR_1_ON, SVR_2_ON);
    }

    /**
     * This test will ping the server #1 though the ImR #1 and
     * ping the server #2 through the ImR #2.
     */
    @Test
    public void test_1_basic_ior()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_1 = null;
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_2 = null;

        try
        {
            org.omg.CORBA.Object obj;
            String result;
            String ior_1;
            String ior_2;

            TestUtils.getLogger().debug("++++ test_basic_ior: ping server 1"==null? "null" : "++++ test_basic_ior: ping server 1");
            ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_basic_ior: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            obj = orb.string_to_object(ior_1);
            assertTrue("test_basic_ior: couldn't generate server #1's obj using IOR: < " + ior_1 + " >", obj != null);
            server_1 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            result = server_1.echo_simple();
            String msg = "test_basic_ior: got resp from server #1: <" + result + ">";
            TestUtils.getLogger().debug(msg==null? "null" : msg);
            assertTrue("test_basic_ior: couldn't ping server #1 using IOR: < " + ior_1 + " >", result != null);
            assertTrue("test_basic_ior: got unexpected response from server #1: <" + result + ">",
                    result.startsWith("Simple greeting from"));
            TestUtils.getLogger().debug("++++ test_basic_ior: ping server 1 - complete"==null? "null" : "++++ test_basic_ior: ping server 1 - complete");

            TestUtils.getLogger().debug("++++ test_basic_ior: ping server 2"==null? "null" : "++++ test_basic_ior: ping server 2");
            ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_basic_ior: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            obj = orb.string_to_object(ior_2);
            assertTrue("test_basic_ior: couldn't generate server #2's obj using IOR: < " + ior_2 + " >", obj != null);
            server_2 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            result = server_2.echo_simple();
            String msg1 = "test_basic_ior: got resp from server: <" + result + ">";
            TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            assertTrue("test_basic_ior: couldn't ping server #2 using IOR: < " + ior_2 + " >", result != null);
            assertTrue("test_basic_ior: got unexpected response from server #2: <" + result + ">",
                    result.startsWith("Simple greeting from"));
            TestUtils.getLogger().debug("++++ test_basic_ior: ping server 2 using IOR - complete"==null? "null" : "++++ test_basic_ior: ping server 2 using IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_basic_ior: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_basic_ior: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server_1 = null;
            server_2 = null;
        }
    }

    /**
     * This test will use the corbaloc IOR to send messages to
     * the server #1 though the ImR #1 and to the server #2 through the ImR #2.
     */
    @Test
    public void test_2_basic_corbaloc()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_1 = null;
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_2 = null;

        try
        {
            org.omg.CORBA.Object obj;
            String ior_1;
            String ior_2;
            int cnt;

            ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_basic_corbaloc: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_basic_corbaloc: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_basic_corbaloc: couldn't generate corbaloc IOR using server #1's IOR", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_basic_corbaloc: couldn't generate corbaloc IOR using server #2's IOR", corbaloc2 != null && corbaloc2.length() > 0);
            String msg = "++++ test_basic_corbaloc: hailing server #1 using corbaloc IOR < " + corbaloc1 + " >";

            TestUtils.getLogger().debug(msg==null? "null" : msg);
            obj = orb.string_to_object(corbaloc1);
            assertTrue("test_basic_corbaloc: couldn't generate obj using server #1's IOR < " + corbaloc1 + " >", obj != null);
            server_1 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_basic_corbaloc", "hailing server at " + corbaloc1, server_1);
                    assertTrue("test_basic_corbaloc: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_basic_corbaloc: hailing server 1 using corbaloc IOR - complete"==null? "null" : "++++ test_basic_corbaloc: hailing server 1 using corbaloc IOR - complete");
            String msg1 = "++++ test_basic_corbaloc: haiing server #2 using corbaloc IOR < " + corbaloc2 + " >";

            TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            obj = orb.string_to_object(corbaloc1);
            assertTrue("test_basic_corbaloc: couldn't generate obj using server #2's IOR < " + corbaloc2 + " >", obj != null);
            server_2 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_basic_corbaloc", "hailing server at " + corbaloc2, server_2);
                    assertTrue("test_basic_corbaloc: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_basic_corbaloc: hailing server #2 using corbaloc IOR - complete"==null? "null" : "++++ test_basic_corbaloc: hailing server #2 using corbaloc IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_basic_corbaloc: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_basic_corbaloc: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server_1 = null;
            server_2 = null;
        }
    }

    /**
     * This test checks out the fail-over logic which enables a client ORB to
     * reach an alternate IMR and server when the currently being used IMR is
     * no longer reachable.
     */
    @Test
    public void test_3_failover() throws Exception
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;

        try
        {
            org.omg.CORBA.Object obj;
            String ior_1;
            String ior_2;
            int cnt;

            ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_failover1: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_failover1: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_failover1: couldn't generate corbaloc IOR using server #1's IOR: < " + ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_failover1: couldn't generate corbaloc IOR using server #2's IOR: < " + ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);
            int slash = corbaloc1.indexOf("/");
            int slash2 = corbaloc2.indexOf("/");
            int colon = corbaloc1.indexOf(":");
            int colon2 = corbaloc2.indexOf(":");
            String objref = corbaloc1.substring(slash);
            String combined_corbaloc1 = corbaloc1.substring(0, slash) + "," + corbaloc2.substring(colon2+1, slash2) + objref;
            String combined_corbaloc2 = corbaloc2.substring(0, slash2) + "," + corbaloc1.substring(colon+1, slash) + objref;

            // Drop IMR #1 to force the client to go to IMR #2
            teardownMyImRs(IMR_1_OFF, IMR_2_ON);
            String msg = "++++ test_failover1: hailing a server with IMR #1 down using combined corbaloc IOR: < " + combined_corbaloc1 + " >";
            TestUtils.getLogger().debug(msg==null? "null" : msg);
            obj = orb.string_to_object(combined_corbaloc1);
            assertTrue("test_failover1: couldn't generate obj using combined corbaloc IOR: < " + combined_corbaloc1 + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_failover1", "hailing server at " + combined_corbaloc1, server);
                    assertTrue("test_failover1: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_failover1: hailing a server with IMR #1 down using combined corbaloc IOR - complete"==null? "null" : "++++ test_failover1: hailing a server with IMR #1 down using combined corbaloc IOR - complete");

            // Restore #IMR #1 and drop IMR #2
            // This will force the client to go to IMR #1
            teardownMyImRs(IMR_1_OFF, IMR_2_OFF);
            setupMyImRs(IMR_1_ON, IMR_2_OFF);
            String msg1 = "++++ test_failover1: hailing a server with IMR #2 down using combined corbaloc IOR < " + combined_corbaloc2 + " >";
            TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            obj = orb.string_to_object(combined_corbaloc2);
            assertTrue("test_failover1: couldn't generate obj using combined corbaloc IOR < " + combined_corbaloc2 + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_failover1", "hailing server at " + combined_corbaloc1, server);
                    assertTrue("test_failover1: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_failover1: hailing a server with IMR #2 down using corbaloc IOR - complete"==null? "null" : "++++ test_failover1: hailing a server with IMR #2 down using corbaloc IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_failover1: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_failover1: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

     /**
     * This test checks out the fail-over logic which cycles between ImR
     * endpoints hoping to reach one of them.  Once an ImR is reachable, the
     * data exchange will begin.
     */
    @Test
    public void test_4_failover() throws Exception
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_failover2: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_failover2: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_failover2: couldn't generate corbaloc IOR using server #1's IOR: < " + ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_failover2: couldn't generate corbaloc IOR using server #2's IOR: < " + ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);
            int slash = corbaloc1.indexOf("/");
            int slash2 = corbaloc2.indexOf("/");
            int colon2 = corbaloc2.indexOf(":");
            String objref = corbaloc1.substring(slash);
            String combined_corbaloc1 = corbaloc1.substring(0, slash) + "," + corbaloc2.substring(colon2+1, slash2) + objref;

            // start up a delay thread which waits for a while then
            // goes and restart both ImRs.
            Thread delayStart = new Thread (new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(30000);
                    }
                    catch (Exception e)
                    {
                        //
                    }
                    ImRFailoverTest.this.testComplete = false;
                    ImRFailoverTest.this.setupMyImRs(IMR_1_ON, IMR_2_ON);

                    // then wait for a while for the test to complete.
                    // If it is not completed, then fail the test.
                    synchronized (syncTest)
                    {
                        int n = 1;
                        while (n++ <= 30 && ! ImRFailoverTest.this.testComplete);
                        {
                            try
                            {
                                ImRFailoverTest.this.syncTest.wait(1000);
                            }
                            catch (InterruptedException e)
                            {
                                //
                            }
                        }

                        if (! ImRFailoverTest.this.testComplete) {
                            fail("test_failover2: should have been completed by now");
                        }
                    }
                }
            });

            delayStart.start();

            // Drop both IMRs
            teardownMyImRs(IMR_1_OFF, IMR_2_OFF);
            org.omg.CORBA.Object obj = orb.string_to_object(combined_corbaloc1);
            assertTrue("test_failover2: couldn't generate object reference for combined corbaloc IOR < " + combined_corbaloc1 + " >", obj != null);
            for (int retries = 10; !testComplete && retries > 0; retries--)
            {
                try
                {
                    server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
                    int cnt = send_msg(5, "test_failover2", "hailing server at " + combined_corbaloc1, server);
                    assertTrue("test_failover2: got cnt=" + cnt + " (expected 5)", cnt == 5);

                    synchronized (syncTest)
                    {
                        // indicate the test is completed
                        testComplete = true;
                        syncTest.notifyAll();
                    }

                    TestUtils.getLogger().debug("++++ test_failover2: hailing a server using corbaloc IOR - complete");

                }
                catch (org.omg.CORBA.TRANSIENT e)
                {
                    // retry on a transient until count
                }
            }

        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_failover2: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_failover2: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

    /**
     * This test uses an unknown object reference,
     * so it will be rejected with an exception.
     */
    @Test
    public void test_5_wrong_objref()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #1's IOR: < " + ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #2's IOR: < " + ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);
            int slash = corbaloc1.indexOf("/");
            int slash2 = corbaloc2.indexOf("/");
            int colon2 = corbaloc2.indexOf(":");
            String objref = "/" + IMPLNAME + "/AnUnknownObjectID";
            String combined_corbaloc1 = corbaloc1.substring(0, slash) + "," + corbaloc2.substring(colon2+1, slash2) + objref;

            org.omg.CORBA.Object obj = orb.string_to_object(combined_corbaloc1);
            assertTrue("test_wrong_objref: couldn't generate obj using combined corbaloc IOR < " + combined_corbaloc1 + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            assertTrue("test_wrong_objref: should have been rejected by the servers", server == null);
            TestUtils.getLogger().debug("++++ test_wrong_objref: hailing a server using corbaloc IOR - complete"==null? "null" : "++++ test_wrong_objref: hailing a server using corbaloc IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            // OK
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            // OK
        }
        catch (org.omg.CORBA.BAD_PARAM e)
        {
            // OK
        }
        catch (org.omg.CORBA.OBJ_ADAPTER e)
        {
            // OK
        }
        finally
        {
            server = null;
        }
    }

    /**
     * This test uses an invalid endpoint,
     * so it will be rejected with an exception.
     */
    @Test
    public void test_6_wrong_endpoint()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String ior_1 = serverSetup_1.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = serverSetup_2.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #1's IOR: < " + ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #2's IOR: < " + ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);
            int slash = corbaloc1.indexOf("/");
            String objref = corbaloc1.substring(slash);

            String corbaloc = "corbaloc:iiop:255.255.254.253:12345/" + objref;
            org.omg.CORBA.Object obj = orb.string_to_object(corbaloc);
            assertTrue("test_wrong_endpoint: couldn't generate obj < " + corbaloc + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            assertTrue("test_wrong_endpoint: should have been rejected by the servers", server == null);
            TestUtils.getLogger().debug("++++ test_wrong_endpoint: hailing a server using a malformed corbaloc IOR - complete"==null? "null" : "++++ test_wrong_endpoint: hailing a server using a malformed corbaloc IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            // OK
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            // OK
        }
        catch (org.omg.CORBA.BAD_PARAM e)
        {
            // OK
        }
        catch (org.omg.CORBA.OBJ_ADAPTER e)
        {
            // OK
        }
        finally
        {
            server = null;
        }
    }

    private int send_msg(int ntimes, String testName, String msg,
            org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage s)
    {

        String echoMsg = new String ((testName != null ? ": " + testName : "")
                                        + (msg != null ? ": " + msg : "") );

        int successCnt = 0;
        for (int n = 1; n <= ntimes; n++)
        {
            String outMsg = new String(Integer.toString(n) + echoMsg);
            long tms_out = System.currentTimeMillis();

            String inMsg = s.echo_string(outMsg);
            long tms_in = System.currentTimeMillis();
            long tms_dif = tms_in - tms_out;
            if (inMsg != null && inMsg.equals(outMsg))
            {
                successCnt++;
                String msg1 = "++++ OK: " + tms_dif + "mSec <" + inMsg + ">";
                TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            }
            else
            {
                String msg1 = "++++ ERR: out: <" + outMsg + "> in: <"
                    + (inMsg == null? "null" : inMsg) + ">";
                TestUtils.getLogger().debug(msg1==null? "null" : msg1);

            }

        }
        return successCnt;
    }
}
