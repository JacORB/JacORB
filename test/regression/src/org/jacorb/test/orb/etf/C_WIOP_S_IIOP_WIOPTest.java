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
 * A test that uses IIOP/WIOP on the server side, but only WIOP on the client
 * side.  Therefore, WIOP should be used for the connection, even though it
 * is only the second profile published by the server.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class C_WIOP_S_IIOP_WIOPTest extends AbstractWIOPTestCase
{
    public C_WIOP_S_IIOP_WIOPTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Client WIOP Server IIOP WIOP");

        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories,"
                              + "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        // WIOP does not support SSL.
        clientProps.setProperty("jacorb.regression.disable_security",
                                "true");


        ClientServerSetup setup =
          new ClientServerSetup (suite,
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);

        suite.addTest (new C_WIOP_S_IIOP_WIOPTest ("testConnection", setup));

        return setup;
    }

    public void testConnection()
    {
        server.ping();
        assertTrue (WIOPFactories.isTransportInUse());
    }



}
