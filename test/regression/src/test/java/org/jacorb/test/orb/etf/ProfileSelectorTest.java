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
 * A test that uses the {@link org.jacorb.test.orb.etf.WIOPSelector}
 * on the client side.  This means the client will always
 * select the WIOP transport, even though the server publishes IIOP as the
 * primary profile, and WIOP only as the secondary profile.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ProfileSelectorTest extends AbstractWIOPTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        // WIOP does not support SSL.
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");
        clientProps.setProperty("jacorb.transport.client.selector",
                                "org.jacorb.test.orb.etf.WIOPSelector");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        setup = new ClientServerSetup(
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);
    }

    @Test
    public void testConnection()
    {
        server.ping();
        assertTrue (WIOPFactories.isTransportInUse());
    }
}
