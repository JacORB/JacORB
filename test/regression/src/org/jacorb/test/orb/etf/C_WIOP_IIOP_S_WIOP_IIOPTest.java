package org.jacorb.test.orb.etf;

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

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;

/**
 * A test that uses WIOP and IIOP both on the client side and the server
 * side, in that order.  Therefore, WIOP should be used for the connection.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class C_WIOP_IIOP_S_WIOP_IIOPTest extends AbstractWIOPTestCase
{
    public C_WIOP_IIOP_S_WIOP_IIOPTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Client WIOP IIOP Server WIOP IIOP");

        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories,"
                              + "org.jacorb.orb.iiop.IIOPFactories");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories," +                                "org.jacorb.orb.iiop.IIOPFactories");

        // WIOP does not support SSL.
        clientProps.setProperty("jacorb.regression.disable_security",
                                "true");

        ClientServerSetup setup =
          new ClientServerSetup (suite,
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);

        suite.addTest (new C_WIOP_IIOP_S_WIOP_IIOPTest ("testConnection", setup));

        return setup;
    }

    public void testConnection()
    {
        server.ping();
        assertTrue (WIOPFactories.isTransportInUse());
    }
}
