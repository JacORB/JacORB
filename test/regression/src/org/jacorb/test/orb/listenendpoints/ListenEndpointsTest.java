package org.jacorb.test.orb.listenendpoints;

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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.JacORBTestSuite;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessage;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageHelper;

 /**
 * Tests -ORBListenEndpoints feature
 *
 *
 */
public class ListenEndpointsTest extends ClientServerTestCase
{
    //String ior = null;
    //String corbalocObjId = null;

    private static final String DEFAULT_LISTEN_EP = "iiop://:45000";

    // wildcard listen endpoint
    private static final String LISTEN_EP = "'iiop://:32999,iiop://:44999;iiop://:45999'";

    private static final String PROTOCOL = "iiop:";

    private static final int CORRECT_PORT_1 = 32999;
    private static final int CORRECT_PORT_2 = 44999;
    private static final int CORRECT_PORT_3 = 45999;
    private static final int WRONG_PORT   = 55555;
    private static final int WRONG_PORT_2 = 45000;

    public ListenEndpointsTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        // server = test.listenendpoints.echo_corbaloc.EchoMessageHelper.narrow (setup.getServerObject());
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite(ListenEndpointsTest.class.getName(),
                                              ListenEndpointsTest.class);

        Properties clientProps = new Properties();
        clientProps.setProperty ("jacorb.retries", "3");
        clientProps.setProperty ("jacorb.retry_interval", "500");
        clientProps.setProperty ("jacorb.connection.client.connect_timeout","1000");
        // clientProps.setProperty ("jacorb.test.timeout.server", Long.toString(10000));
        // If security is not disabled it will not use the above host/port
        // combinations.
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

        Properties serverProps = new Properties();
        serverProps.setProperty ("OAAddress", DEFAULT_LISTEN_EP);
        serverProps.put ("OAPort","0");
        serverProps.put ("OASSLPort", "0");
        // serverProps.put ("jacorb.test.timeout.server", Long.toString(10000));

        ClientServerSetup setup =
                new ClientServerSetup (suite,
                                   "org.jacorb.test.listenendpoints.echo_corbaloc.Server",
                                   new String[] {"-testmode", "P", "-ORBListenEndpoints", LISTEN_EP},
                                   clientProps,
                                   serverProps);

        TestUtils.addToSuite(suite, setup, ListenEndpointsTest.class);
        return setup;
    }

    public void test_ping()
    {
        EchoMessage server = null;
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_ping: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_ping: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_ping: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            server =
                    EchoMessageHelper.narrow (setup.getServerObject());

            server.ping();

        }
        catch (Exception e)
        {
            fail("test_ping: got an unexpected exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

    public void test_echo_simple()
    {
        EchoMessage server = null;
        try
        {
            server =
                    EchoMessageHelper.narrow (setup.getServerObject());
            String result = server.echo_simple();
            log("test_echo_simple: got resp from server: <" + result + ">");
            assertTrue("test_echo_simple: result is null", result != null);
            assertTrue("test_echo_simple: unexpected result <" + result + ">",
                    result.startsWith("Simple greeting from"));
        }
        catch (Exception e)
        {
            fail("test_echo_simple: got an unexpected exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

    public void test_echo_string()
    {
        EchoMessage server = null;
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_ping: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_ping: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_ping: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            server =
                    EchoMessageHelper.narrow (setup.getServerObject());

            String outMsg = new String(Integer.toString(1)
                    + "test_echo_string is hailing server with IOR <" + ior + ">");
            long tms_out = System.currentTimeMillis();
            String inMsg = server.echo_string(outMsg);
            long tms_in = System.currentTimeMillis();
            long tms_dif = tms_in - tms_out;
            if (outMsg.equals(inMsg))
            {
                assertTrue("OK: " + tms_dif + "mSec <" + inMsg + ">", true);
            }
            else
            {
                fail("ERR: " + tms_dif + "mSec send: <" + outMsg + "> recv: <"
                    + (inMsg == null? "null" : inMsg) + ">");
            }

        }
        catch (Exception e)
        {
            fail("test_echo_string: got an unexpected exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

    public void test_echo_wide()
    {
        EchoMessage server = null;
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_ping: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_ping: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_ping: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            server =
                    EchoMessageHelper.narrow (setup.getServerObject());

            String outMsg = new String(Integer.toString(1) + "test_echo_wide is hailing server with IOR <" + ior + ">");
            long tms_out = System.currentTimeMillis();
            String inMsg = server.echo_string(outMsg);
            long tms_in = System.currentTimeMillis();
            long tms_dif = tms_in - tms_out;
            if (outMsg.equals(inMsg))
            {
                assertTrue("OK: " + tms_dif + "mSec <" + inMsg + ">", true);
            }
            else
            {
                fail("ERR: " + tms_dif + "mSec send: <" + outMsg + "> recv: <"
                    + (inMsg == null? "null" : inMsg) + ">");
            }
        }
        catch (Exception e)
        {
            fail("test_echo_wide: got an unexpected exception: <" + e.getMessage() + ">");
        }
        finally
        {
            server = null;
        }
    }

    /**
     * This test would ping all listenable endpoints on CORRECT_PORT_1
     */
    public void test_correct_port_1()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_correct_port_1: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_correct_port_1: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_correct_port_1: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> listen_eps = getListenEndpoints(CORRECT_PORT_1, corbalocObjId);
            for (Iterator<String> x = listen_eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;
                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));

                    // log("test_correct_port_1: ping endpoint: " + endpoint);
                    int cnt = send_msg(10, "test_correct_port_1", "hailing endpoint " + endpoint, server);
                    assertTrue("test_correct_port_1: got cnt=" + cnt + " (expected 10)", cnt == 10);
                }
                catch (Exception e)
                {
                    fail("test_correct_port_1: got an unexpected exception : <" + e.getMessage() + ">");
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }
            }

        }
        catch (Exception e)
        {
            fail("test_correct_port_1: got an unexpected exception : <" + e.getMessage() + ">");
        }
    }

    /**
     * This test would ping all listenable endpoints on CORRECT_PORT_2
     */
    public void test_correct_port_2()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_correct_port_2: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_correct_port_2: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_correct_port_2: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> listen_eps = getListenEndpoints(CORRECT_PORT_2, corbalocObjId);
            for (Iterator<String> x = listen_eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;
                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));

                    // log("test_correct_port_1: ping endpoint: " + endpoint);
                    int cnt = send_msg(10, "test_correct_port_2", "hailing endpoint " + endpoint, server);
                    assertTrue("test_correct_port_2: got cnt=" + cnt + " (expected 10)", cnt == 10);
                }
                catch (Exception e)
                {
                    fail("test_correct_port_2: got an unexpected exception : <" + e.getMessage() + ">");
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }
            }

        }
        catch (Exception e)
        {
            fail("test_correct_port_2: got an unexpected exception : <" + e.getMessage() + ">");
        }
    }

    /**
     * This test would ping all listenable endpoints on CORRECT_PORT_3
     */
    public void test_correct_port_3()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_correct_port_2: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_correct_port_2: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_correct_port_2: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> listen_eps = getListenEndpoints(CORRECT_PORT_3, corbalocObjId);
            for (Iterator<String> x = listen_eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
                props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;
                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));

                    // log("test_correct_port_1: ping endpoint: " + endpoint);
                    int cnt = send_msg(10, "test_correct_port_2", "hailing endpoint " + endpoint, server);
                    assertTrue("test_correct_port_2: got cnt=" + cnt + " (expected 10)", cnt == 10);
                }
                catch (Exception e)
                {
                    fail("test_correct_port_2: got an unexpected exception : <" + e.getMessage() + ">");
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }
            }

        }
        catch (Exception e)
        {
            fail("test_correct_port_2: got an unexpected exception : <" + e.getMessage() + ">");
        }
    }

    private int send_msg(int ntimes, String testName, String msg,
            EchoMessage s)
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
                log("OK: " + tms_dif + "mSec <" + inMsg + ">" );
            }
            else
            {
                log("ERR: out: <" + outMsg + "> in: <"
                    + (inMsg == null? "null" : inMsg) + ">");

            }

        }
        return successCnt;
    }

    /**
     * This test would ping all listenable addresses using a wrong port.
     * It should fail.
     */
    public void test_wrong_port_1()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_wrong_port: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_wrong_port: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_wrong_port: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> listen_eps = getListenEndpoints(WRONG_PORT, corbalocObjId);
            for (Iterator<String> x = listen_eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;

                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));

                    //log("test_wrong_port: ping endpoint: " + endpoint);
                    int cnt = 0;
                    cnt = send_msg(1, "test_wrong_port", "hailing endpoint " + endpoint, server);
                    assertTrue("test_wrong_port: got cnt=" + cnt + " (expected 0)", cnt != 1);
                }
                catch (Exception e)
                {
                    // expected
                    //e.getMessage();
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }

            }

        }
        catch (Exception e)
        {
            fail("test_wrong_port: got an unexpected exception : <" + e.getMessage() + ">");
        }
    }

    /**
     * This test would ping all listenable addresses using the default port.
     * It should fail.
     */
    public void test_wrong_port_2()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_wrong_port: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_wrong_port: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_wrong_port: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> listen_eps = getListenEndpoints(WRONG_PORT_2, corbalocObjId);
            for (Iterator<String> x = listen_eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;

                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));

                    //log("test_wrong_port: ping endpoint: " + endpoint);
                    int cnt = 0;
                    cnt = send_msg(1, "test_wrong_port", "hailing endpoint " + endpoint, server);
                    assertTrue("test_wrong_port: got cnt=" + cnt + " (expected 0)", cnt != 1);
                }
                catch (Exception e)
                {
                    // expected
                    //e.getMessage();
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }

            }

        }
        catch (Exception e)
        {
            fail("test_wrong_port: got an unexpected exception : <" + e.getMessage() + ">");
        }
    }

    /**
     * This test would ping the loopback endpoint which should pass.
     *
     */
    public void test_loopback()
    {
        try
        {
            String ior = setup.getServerIOR();
            assertTrue("test_loopback: couldn't pickup server IOR", ior != null && ior.length() > 0);

            int slash = ior.trim().indexOf("/");
            String corbalocObjId = ior.trim().substring(slash);
            assertTrue("test_loopback: corbaloc objectID is null", corbalocObjId != null);
            assertTrue("test_loopback: corbaloc objID is malformed", corbalocObjId.equals("/EchoServer/EchoPOAP/EchoID") );

            List<String> eps = getIsLoopbackEndpoints(corbalocObjId);
            for (Iterator<String> x = eps.iterator(); x.hasNext();)
            {
                String endpoint = (String)x.next();
                Properties props = new Properties();
                props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
                props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
                EchoMessage server = null;

                try
                {
                    assertTrue(orb instanceof org.jacorb.orb.ORB);

                    server =
                            EchoMessageHelper.narrow(orb.string_to_object(endpoint));
                    log("test_loopback: ping endpoint: " + endpoint);
                    int cnt = 0;
                    cnt = send_msg(5, "test_loopback", "hailing server endpoint " + endpoint, server);
                    assertTrue("test_loopback: got cnt=" + cnt + " (expected 5)", cnt == 5);

                }
                catch (Exception e)
                {
                    // not expected
                    //e.getMessage();
                }
                finally
                {
                    orb.shutdown(true);
                    server = null;
                }

            }

        }
        catch (Exception e)
        {
            fail("test_loopback: got an unexpected exception : <" + e.getMessage() + ">");
        }


    }

    private static void log(String msg)
    {
        System.out.println("AlternateEndpointTest: " + msg);
        System.out.flush();
    }

    private List<InetAddress> getInetAddressList() throws SocketException
    {
        List<InetAddress> inetList = new ArrayList<InetAddress>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(nets))
        {
            for ( InetAddress inetAddress : Collections.list(netint.getInetAddresses()) )
            {
                inetList.add(inetAddress);
            }

        }
        return inetList;
    }

    private List<String> getListenEndpoints(int listen_port, String objId) throws SocketException
    {
        try
        {
            List<InetAddress> inets = getInetAddressList();
            List<String> listen_eps = new ArrayList<String>();
            for (Iterator<InetAddress> x = inets.iterator(); x.hasNext();)
            {
                InetAddress inetAddr = (InetAddress)x.next();
                String ipaddr = inetAddr.toString().substring(1);
                String conHostName = inetAddr.getCanonicalHostName();
                String hostName = inetAddr.getHostName();
                if (!inetAddr.isLoopbackAddress() && !inetAddr.isLinkLocalAddress())
                {
                    if (inetAddr instanceof Inet4Address)
                    {
                        listen_eps.add(
                                new String ("corbaloc:" + PROTOCOL + ipaddr + ":" + listen_port +
                                objId)
                                );
                        if (!hostName.equals(ipaddr))
                        {
                            listen_eps.add(
                                    new String ("corbaloc:" + PROTOCOL + hostName + ":" + listen_port +
                                    objId)
                                    );
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            listen_eps.add(
                                    new String ("corbaloc:" + PROTOCOL + hostName + ":" + listen_port +
                                    objId)
                                    );
                        }

                    }
                    else if (inetAddr instanceof Inet6Address)
                    {
                        String ipv6 = ipaddr;
                        int zoneid_delim = ipv6.indexOf('%');
                        if (zoneid_delim > 0)
                        {
                            ipv6 = ipv6.substring(0, zoneid_delim);
                        }
                        //System.out.println("getListenEndpoints: ipv6=<" + ipv6 + ">");
                        if (!ipv6.startsWith("fe80"))
                        {
                            listen_eps.add(
                                    new String ("corbaloc:" + PROTOCOL + "[" + ipv6 + "]:" + listen_port +
                                    objId)
                                    );
                        }

                        if (!hostName.equals(ipaddr))
                        {
                            zoneid_delim = hostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = hostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add(
                                    new String ("corbaloc:" + PROTOCOL + "[" + ipv6 + "]:" + listen_port +
                                    objId)
                                    );
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            zoneid_delim = conHostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = conHostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add(
                                    new String ("corbaloc:" + PROTOCOL + "[" + ipv6 + "]:" + listen_port +
                                    objId)
                                    );
                        }
                    }
                }
            }

            return listen_eps;

        }
        catch (SocketException e)
        {
            throw new SocketException (e.getMessage());
        }
    }

    private List<String> getIsLoopbackEndpoints(String objId) throws SocketException
    {
        List<String> listen_eps = new ArrayList<String>();

        listen_eps.add(
                new String ("corbaloc:" + PROTOCOL + "127.0.0.1" + ":" + CORRECT_PORT_1 +
                            objId)
                );
        listen_eps.add(
                new String ("corbaloc:" + PROTOCOL + "localhost" + ":" + CORRECT_PORT_1 +
                            objId)
                );

        listen_eps.add(
                new String ("corbaloc:" + PROTOCOL + "127.0.0.1" + ":" + CORRECT_PORT_2 +
                            objId)
                );
        listen_eps.add(
                new String ("corbaloc:" + PROTOCOL + "localhost" + ":" + CORRECT_PORT_2 +
                            objId)
                );

        return listen_eps;
    }

}
