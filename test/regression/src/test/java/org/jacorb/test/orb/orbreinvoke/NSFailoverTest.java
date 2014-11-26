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
import org.jacorb.test.harness.NameServiceSetup;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NSFailoverTest extends FixedPortORBTestCase
{
    @Rule
    public TemporaryFolder folder1 = new TemporaryFolder();

    @Rule
    public TemporaryFolder folder2 = new TemporaryFolder();

    private static final boolean NS_1_ON = true;
    private static final boolean NS_2_ON = true;
    private static final boolean NS_1_OFF = false;
    private static final boolean NS_2_OFF = false;

    private static final boolean SVR_1_ON = true;
    private static final boolean SVR_2_ON = true;
    private static final boolean SVR_1_OFF = false;
    private static final boolean SVR_2_OFF = false;

    private static final String IMPLNAME = "EchoServer";
    private static final String SERVER_1_LEP = "iiop://:" + getNextAvailablePort();
    private static final String SERVER_2_LEP = "iiop://:"+ getNextAvailablePort();

    // NameServer endpoints
    private static final String NS_1_LEP = Integer.toString(getNextAvailablePort());
    private static final String NS_2_LEP = Integer.toString(getNextAvailablePort());

    private Properties nsProp_1 = null;
    private Properties nsProp_2 = null;
    private File nsIOR_1 = null;
    private File nsIOR_2 = null;
    private NameServiceSetup nsSetup_1 = null;
    private NameServiceSetup nsSetup_2 = null;
    private ServerSetup serverSetup_1 = null;
    private ServerSetup serverSetup_2 = null;
    private final java.lang.Object syncTest = new java.lang.Object();
    private boolean testComplete;

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("jacorb.retries", "3");
        props.setProperty ("jacorb.retry_interval", "1000");
        props.setProperty ("jacorb.connection.client.connect_timeout","5000");
        props.setProperty ("jacorb.test.timeout.server", Long.toString(10000));
    }


    @After
    public void tearDown() throws Exception
    {
        teardownMyServers(SVR_1_OFF, SVR_2_OFF);
        teardownMyNS(NS_1_OFF, NS_2_OFF);
    }

    public void teardownMyNS(boolean nsOff_1, boolean nsOff_2) throws Exception
    {
        if (nsOff_1 == NS_1_OFF && nsSetup_1 != null)
        {
            nsSetup_1.tearDown();
            nsSetup_1 = null;
        }

        if (nsOff_2 == NS_2_OFF && nsSetup_2 != null)
        {
            nsSetup_2.tearDown();
            nsSetup_2 = null;
        }
    }

    public void setupMyNS (boolean nsOn_1, boolean nsON_2)
    {
        if (nsOn_1 == NS_1_ON && nsSetup_1 == null)
        {
            try
            {
                TestUtils.getLogger().debug("++++ setting NameServer #1"==null? "null" : "++++ setting NameServer #1");
                if (nsProp_1 == null)
                {
                    // initialize NameServer #1 properties
                    nsProp_1 = new Properties();
                    nsProp_1.setProperty ("OAAddress","iiop://:" + NS_1_LEP);
                    nsProp_1.setProperty ("jacorb.naming.print_ior", "on");

                    nsIOR_1 = File.createTempFile("MyNS1", ".ior");
                    nsIOR_1.deleteOnExit();

                    nsProp_1.setProperty ("jacorb.naming.ior_filename", nsIOR_1.toString());
                    nsProp_1.setProperty ("jacorb.naming.time out", "5000");
                    nsProp_1.setProperty ("jacorb.test.timeout.server", Long.toString(10000));

                    nsProp_1.setProperty("jacorb.log.showThread", "on");
                    nsProp_1.setProperty("jacorb.log.showSrcInfo", "on");
                }

                // initiate the NameServer service
                nsSetup_1 = new NameServiceSetup (folder1, nsProp_1, 1);
                nsSetup_1.setUp();
                TestUtils.getLogger().debug("++++ setting NameServer #1 - complete"==null? "null" : "++++ setting NameServer #1 - complete");
            }
            catch (Exception e)
            {
                // ignored
            }
        }

        if (nsON_2 == NS_2_ON && nsSetup_2 == null)
        {
            try
            {
                TestUtils.getLogger().debug("++++ setting NameServer #2"==null? "null" : "++++ setting NameServer #2");
                if (nsProp_2 == null)
                {
                    // initialize NameServer #1 properties
                    nsProp_2 = new Properties();
                    nsProp_2.setProperty ("OAAddress","iiop://:" + NS_2_LEP);
                    nsProp_2.setProperty ("jacorb.naming.print_ior", "on");

                    nsIOR_2 = File.createTempFile("MyNS2", ".ior");
                    nsIOR_2.deleteOnExit();

                    nsProp_2.setProperty ("jacorb.naming.ior_filename", nsIOR_2.toString());
                    nsProp_2.setProperty ("jacorb.naming.time out", "5000");
                    nsProp_2.setProperty ("jacorb.test.timeout.server", Long.toString(10000));

                    nsProp_2.setProperty("jacorb.log.showThread", "on");
                    nsProp_2.setProperty("jacorb.log.showSrcInfo", "on");
                }

                // initiate the NameServer service
                nsSetup_2 = new NameServiceSetup (folder2, nsProp_2, 2);
                nsSetup_2.setUp();
                TestUtils.getLogger().debug("++++ setting NameServer #2 - complete"==null? "null" : "++++ setting NameServer #2 - complete");
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
    }

    public void setupMyServers(boolean serverOn_1, boolean serverOn_2)
    {
        if (serverOn_1 == SVR_1_ON && serverSetup_1 == null)
        {
            try
            {
                   TestUtils.getLogger().debug("++++ setting up NSFailoverTestServer 1"==null? "null" : "++++ setting up NSFailoverTestServer 1");
                    serverSetup_1 = new ServerSetup (
                                        "org.jacorb.test.orb.orbreinvoke.NSFailoverTestServer",
                                        "",
                                        new String []
                                            {
                                                 "-Djacorb.log.showThread=" + "on",
                                                 "-Djacorb.log.showSrcInfo=" + "on",

                                                 "-ORBListenEndpoints", SERVER_1_LEP,
                                                 "-DORBInitRef.NameService=" + "file://" + nsIOR_1.toString(),
                                                 "-Djacorb.test.timeout.server=" + Long.toString(10000)
                                            },
                                        null);

                    serverSetup_1.setUp();
                    TestUtils.getLogger().debug("++++ setting up server 1 - complete"==null? "null" : "++++ setting up server 1 - complete");
                }
                catch (Exception e)
                {
                    // ignore
                }
        }
        if (serverOn_2 == SVR_2_ON && serverSetup_2 == null)
        {
            try
            {
                TestUtils.getLogger().debug("++++ setting up NSFailoverTestServer 2"==null? "null" : "++++ setting up NSFailoverTestServer 2");
                serverSetup_2 = new ServerSetup (
                                        "org.jacorb.test.orb.orbreinvoke.NSFailoverTestServer",
                                        "",
                                        new String []
                                            {
                                                 "-Djacorb.log.showThread=" + "on",
                                                 "-Djacorb.log.showSrcInfo=" + "on",

                                                 "-ORBListenEndpoints", SERVER_2_LEP,
                                                 "-DORBInitRef.NameService=" + "file://" + nsIOR_2.toString(),
                                                 "-Djacorb.implname=" + IMPLNAME,
                                                 "-Djacorb.test.timeout.server=" + Long.toString(10000)
                                            },
                                        null);

                serverSetup_2.setUp();
                TestUtils.getLogger().debug("++++ setting up server 2 - complete"==null? "null" : "++++ setting up server 2 - complete");
            }
            catch (Exception e)
            {
                //
            }
        }
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Before
    public void setUp() throws Exception
    {
        try
        {
            // initiate NameServer's
            setupMyNS(NS_1_ON, NS_2_ON);
            setupMyServers(SVR_1_ON, SVR_2_ON);
        }
        catch (Exception e)
        {
            fail("setUp got an exception, " + e.getMessage());
        }
    }

    /**
     * This test will ping the server #1 though the NameServer #1 and
     * ping the server #2 through the NameServer #2.
     */
    @Test
    public void test_1_ping()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_1 = null;
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server_2 = null;

        try
        {
            org.omg.CORBA.Object obj;
            String result;

            TestUtils.getLogger().debug("++++ test_ping: ping server #1"==null? "null" : "++++ test_ping: ping server #1");
            String ior_1 = nsSetup_1.getServerIOR();
            assertTrue("test_ping: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);

            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_ping: couldn't generate corbaloc IOR using server #1's IOR", corbaloc1 != null && corbaloc1.length() > 0);
            int slash = corbaloc1.indexOf("/");
            int colon = corbaloc1.indexOf(":");
            String corbaname = "corbaname:" + corbaloc1.substring(colon+1, slash) + "#" + IMPLNAME + ".context";
            obj = orb.string_to_object(corbaname);
            assertTrue("test_ping: couldn't generate server #1's obj using IOR: < " + corbaname + " >", obj != null);
            server_1 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            result = server_1.echo_simple();
            String msg = "test_ping: got resp from server #1: <" + result + ">";
            TestUtils.getLogger().debug(msg==null? "null" : msg);
            assertTrue("test_ping: couldn't ping server #1 using IOR: < " + corbaname + " >", result != null);
            assertTrue("test_ping: got unexpected response from server #1: <" + result + ">",
                    result.startsWith("Simple greeting from"));
            TestUtils.getLogger().debug("++++ test_ping: ping server #1 - complete"==null? "null" : "++++ test_ping: ping server #1 - complete");

            TestUtils.getLogger().debug("++++ test_ping: ping server #2"==null? "null" : "++++ test_ping: ping server #2");
            String ior_2 = nsSetup_2.getServerIOR();
            assertTrue("test_ping: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_ping: couldn't generate corbaloc IOR using server #1's IOR", corbaloc2 != null && corbaloc2.length() > 0);
            slash = corbaloc2.indexOf("/");
            colon = corbaloc2.indexOf(":");
            corbaname = "corbaname:" + corbaloc2.substring(colon+1, slash) + "#" + IMPLNAME + ".context";
            obj = orb.string_to_object(corbaname);
            assertTrue("test_ping: couldn't generate server #2's obj using IOR: < " + ior_2 + " >", obj != null);
            server_2 = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            result = server_2.echo_simple();
            String msg1 = "test_ping: got resp from server: <" + result + ">";
            TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            assertTrue("test_ping: couldn't ping server #2 using IOR: < " + ior_2 + " >", result != null);
            assertTrue("test_ping: got unexpected response from server #2: <" + result + ">",
                    result.startsWith("Simple greeting from"));
            TestUtils.getLogger().debug("++++ test_ping: ping server #2 using IOR - complete"==null? "null" : "++++ test_ping: ping server #2 using IOR - complete");
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_ping: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_ping: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server_1 = null;
            server_2 = null;
        }
    }

    /**
     * This test checks out the fail-over logic which enables a client ORB to
     * reach an alternate NameService and server when the currently being used NameService is
     * no longer reachable.
     * @throws Exception
     */
    @Test
    public void test_2_failover() throws Exception
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;

        try
        {
            org.omg.CORBA.Object obj;
            int cnt;

            String ior_1 = nsSetup_1.getServerIOR();
            assertTrue("test_failover1: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = nsSetup_2.getServerIOR();
            assertTrue("test_failover1: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_failover1: couldn't generate corbaloc IOR using server #1's IOR: < " +
                    ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_failover1: couldn't generate corbaloc IOR using server #2's IOR: < " +
                    ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);

            int slash = corbaloc1.indexOf("/");
            int colon = corbaloc1.indexOf(":");
            int slash2 = corbaloc2.indexOf("/");
            int colon2 = corbaloc2.indexOf(":");
            String objref = "#" + IMPLNAME + ".context";

            String combined_corbaname = "corbaname:" +
                                        corbaloc1.substring(colon+1, slash) +
                                        "," +
                                        corbaloc2.substring(colon2+1, slash2) +
                                        objref;

            // Drop NameService #1 to force the client to go to NameService #2
            teardownMyNS(NS_1_OFF, NS_2_ON);
            String msg = "++++ test_failover1: hailing a server using IOR: < " + combined_corbaname + " >";
            TestUtils.getLogger().debug(msg==null? "null" : msg);
            // obj = orb.string_to_object(combined_corbaname);
            obj = null;
            for (int retries = 2;  retries > 0; retries--)
            {
                try
                {
                    obj = orb.string_to_object(combined_corbaname);
                    break;
                }
                catch (org.omg.CORBA.SystemException ex)
                {
                    System.out.println ("caught " + ex);
                }
            }
            assertTrue("test_failover2: couldn't generate obj using combined corbaloc IOR: < " +
                    combined_corbaname + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_failover2", "hailing server at " + combined_corbaname, server);
                    assertTrue("test_failover2: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_failover1: hailing a server using IOR - complete"==null? "null" : "++++ test_failover2: hailing a server using IOR - complete");

            // Restore NameService #1 and drop NameService #2
            // This will force the client to go to NameService #1
            teardownMyNS(NS_1_OFF, NS_2_OFF);
            setupMyNS(NS_1_ON, NS_2_OFF);
            String msg1 = "++++ test_failover2: hailing a server using IOR < " + combined_corbaname + " >";
            TestUtils.getLogger().debug(msg1==null? "null" : msg1);
            obj = orb.string_to_object(combined_corbaname);
            assertTrue("test_failover2: couldn't generate obj using IOR < " + combined_corbaname + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            cnt = send_msg(5, "test_failover2", "hailing server at " + combined_corbaname, server);
                    assertTrue("test_failover2: got cnt=" + cnt + " (expected 5)", cnt == 5);
            TestUtils.getLogger().debug("++++ test_failover2: hailing a server using IOR - complete"==null? "null" : "++++ test_failover2: hailing a server using IOR - complete");
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
     * This test checks out the fail-over logic which cycles between NameServer
     * endpoints hoping to reach one of them.  Once an NameServer is reachable, the
     * data exchange will begin.
     */
    @Test
    public void test_3_failover() throws Exception
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String ior_1 = nsSetup_1.getServerIOR();
            assertTrue("test_failover3: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = nsSetup_2.getServerIOR();
            assertTrue("test_failover3: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_failover3: couldn't generate corbaloc IOR using server #1's IOR: < " +
                                ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_failover3: couldn't generate corbaloc IOR using server #2's IOR: < " +
                                ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);

            int slash = corbaloc1.indexOf("/");
            int colon = corbaloc1.indexOf(":");
            int slash2 = corbaloc2.indexOf("/");
            int colon2 = corbaloc2.indexOf(":");
            String objref = "#" + IMPLNAME + ".context";
            String combined_corbaname = "corbaname:" +
                    corbaloc1.substring(colon+1, slash) + "," +
                    corbaloc2.substring(colon2+1, slash2) + objref;
            String msg = "++++ test_failover3: combined_corbaname = < " + combined_corbaname + " >";
            TestUtils.getLogger().debug(msg==null? "null" : msg);

            // start up a delay thread which waits for a while then
            // goes and restart NameServers.
            Thread delayStart = new Thread (new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(40000);
                    }
                    catch (Exception e)
                    {
                        //
                    }

                    NSFailoverTest.this.testComplete = false;

                    TestUtils.getLogger().debug("test_failover3: delayStart is starting NS"==null? "null" : "test_failover3: delayStart is starting NS");
                    NSFailoverTest.this.setupMyNS(NS_1_ON, NS_2_ON);

                    // then wait for a while for the test to complete.
                    // If it is not completed, then fail the test.
                    synchronized (syncTest)
                    {
                        int n = 1;
                        while (n++ <= 60 && ! NSFailoverTest.this.testComplete);
                        {
                            try
                            {
                                NSFailoverTest.this.syncTest.wait(1000);
                            }
                            catch (InterruptedException e)
                            {
                                //
                            }
                        }

                        if (! NSFailoverTest.this.testComplete) {
                            fail("test_failover3: should have been completed by now");
                        }
                    }
                }
            });


            // startup delay starting thread
            // Note: this method must be called first
            delayStart.start();

            // Drop both NameServices
            teardownMyNS(NS_1_OFF, NS_2_OFF);

            for (int retries = 10; !testComplete && retries > 0; retries--)
            {
                try
                {

                    org.omg.CORBA.Object obj = orb.string_to_object(combined_corbaname);
                    assertTrue("test_failover3: couldn't generate object reference for IOR < " +
                               combined_corbaname + " >", obj != null);
                    server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
                    int cnt = send_msg(5, "test_failover3", "hailing server using " + combined_corbaname, server);
                    assertTrue("test_failover3: got cnt=" + cnt + " (expected 5)", cnt == 5);
                    TestUtils.getLogger().debug("++++ test_failover3: hailing a server using IOR - complete"==null? "null" : "++++ test_failover3: hailing a server using IOR - complete");
                    synchronized (syncTest)
                    {
                        // indicate the test is completed
                        testComplete = true;
                        syncTest.notifyAll();
                    }
                }
                catch (org.omg.CORBA.BAD_PARAM e)
                {
                    // result of a wrapped transient caught inside string_to_object
                }
                catch (org.omg.CORBA.TRANSIENT e)
                {
                    // retry on a transient until count
                }
            }
        }
        catch (org.omg.CORBA.TRANSIENT e)
        {
            fail("test_failover3: got a TRANSIENT exception: <" + e.getMessage() + ">");
        }
        catch (org.omg.CORBA.COMM_FAILURE e)
        {
            fail("test_failover3: got a COMM_FAILURE exception: <" + e.getMessage() + ">");
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
    public void test_4_wrong_objref()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String ior_1 = nsSetup_1.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #1's IOR", ior_1 != null && ior_1.length() > 0);
            String ior_2 = nsSetup_2.getServerIOR();
            assertTrue("test_wrong_objref: couldn't pickup server #2's IOR", ior_2 != null && ior_2.length() > 0);
            String corbaloc1 = PrintIOR.printFullCorbalocIOR(orb, ior_1);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #1's IOR: < " +
                    ior_1 +" >", corbaloc1 != null && corbaloc1.length() > 0);
            String corbaloc2 = PrintIOR.printFullCorbalocIOR(orb, ior_2);
            assertTrue("test_wrong_objref: couldn't generate corbaloc IOR using server #2's IOR: < " +
                    ior_2 +" >", corbaloc2 != null && corbaloc2.length() > 0);

            int slash = corbaloc1.indexOf("/");
            int colon = corbaloc1.indexOf(":");
            int slash2 = corbaloc2.indexOf("/");
            int colon2 = corbaloc2.indexOf(":");
            String objref = "#" + IMPLNAME + ".invalid_context";

            String combined_corbaname = "corbaname:" +
                                        corbaloc1.substring(colon+1, slash) + "," +
                                        corbaloc2.substring(colon2+1, slash2) + objref;

            org.omg.CORBA.Object obj = orb.string_to_object(combined_corbaname);
            assertTrue("test_wrong_objref: couldn't generate obj using IOR < " + combined_corbaname + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            assertTrue("test_wrong_objref: should have been rejected by the servers", server == null);
            TestUtils.getLogger().debug("++++ test_wrong_objref: hailing a server using IOR - complete"==null? "null" : "++++ test_wrong_objref: hailing a server using IOR - complete");
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
    public void test_5_wrong_endpoint()
    {
        org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage server = null;
        try
        {
            String objref = "#" + IMPLNAME + ".context";
            String corbaname = "corbaname:iiop:255.255.254.253:12345" + objref;
            org.omg.CORBA.Object obj = orb.string_to_object(corbaname);
            assertTrue("test_wrong_endpoint: couldn't generate obj < " + corbaname + " >", obj != null);
            server = org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow(obj);
            assertTrue("test_wrong_endpoint: should have been rejected by the servers", server == null);
            TestUtils.getLogger().debug("++++ test_wrong_endpoint: hailing a server using a malformed IOR - complete"==null? "null" : "++++ test_wrong_endpoint: hailing a server using a malformed IOR - complete");
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
