package org.jacorb.test.orb.etf;

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
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test that uses only WIOP as a transport both on the client side and
 * the server side.  Thus, WIOP should be used for the connection, since it
 * is the only transport available.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class C_WIOP_S_WIOPTest extends AbstractWIOPTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // WIOP does not support SSL.
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties props = new Properties();
        props.setProperty("jacorb.transport.factories",
                          "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        setup = new ClientServerSetup(
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 props, props);
    }

    @Test
    public void testConnection()
    {
        server.ping();
        assertTrue (WIOPFactories.isTransportInUse());
    }
}
