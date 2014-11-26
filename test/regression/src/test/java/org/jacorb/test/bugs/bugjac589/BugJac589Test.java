package org.jacorb.test.bugs.bugjac589;


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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.TRANSIENT;


/**
 * <code>BugJac589Test</code> tests that _get_component operation from
 * CORBA 3 (omg 03-01-02 spec) is available.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac589Test extends ClientServerTestCase
{
    private final static String ior = "IOR:000000000000001B49444C3A64656D6F2F68656C6C6F2F476F6F644461793A312E300000000000010000000000000068000102000000000931302E312E302E340000DC4700000018666F6F2F54657374536572766572504F412F4F626A656374000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001";

    /** The server. */
    private BasicServer server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientProperties = new Properties();
        clientProperties.put("jacorb.connection.client.connect_timeout", "20000");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), clientProperties, clientProperties);
    }

    @Before
    public void setUp() throws Exception
    {
        server = BasicServerHelper.narrow(setup.getServerObject());
    }

    /**
     * <code>testGetComponent</code> verifies that get_component is available.
     *
     * @exception Exception if an error occurs
     */
    @Test
    public void testGetComponent() throws Exception
    {
        org.omg.CORBA.Object o = setup.getClientOrb().string_to_object (ior);

        try
        {
            o._get_component();

            // Fail. Would expect the above IOR to give transient
            fail ("testGetComponent should throw transient");
        }
        catch (TRANSIENT e)
        {
            // Pass
        }
    }


    /**
     * <code>testGetComponentwithServer</code> verifies that get_component is
     * available on the server side.
     *
     * @exception Exception if an error occurs
     */
    @Test
    public void testGetComponentWithServer() throws Exception
    {
        Object result = server._get_component();

        // Pass
        assertTrue (result == null);
    }
}
