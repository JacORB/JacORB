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

import static org.junit.Assert.assertFalse;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ServerListenerTest extends AbstractWIOPTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories,"
                              + "org.jacorb.orb.iiop.IIOPFactories");
        clientProps.setProperty("jacorb.transport.client.selector",
                                "org.jacorb.orb.DefaultProfileSelector");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories," +                                "org.jacorb.orb.iiop.IIOPFactories");
        // only listen on IIOP
        serverProps.setProperty("jacorb.transport.server.listeners", "0");

        setup = new ClientServerSetup(
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);
    }

    @Test
    public void testConnection()
    {
        server.ping();
        assertFalse (WIOPFactories.isTransportInUse());
    }
}
