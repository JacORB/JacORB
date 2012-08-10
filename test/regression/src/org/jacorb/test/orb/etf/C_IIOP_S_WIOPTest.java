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

/**
 * A test that uses IIOP on the client side and WIOP on the server side.
 * Hence, no connection will be possible.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class C_IIOP_S_WIOPTest extends AbstractWIOPTestCase
{
    public C_IIOP_S_WIOPTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Client IIOP Server WIOP");

        Properties clientProps = new Properties();
        clientProps.setProperty ("jacorb.transport.factories",
                                 "org.jacorb.orb.iiop.IIOPFactories");

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                          "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        // WIOP does not support SSL.
        clientProps.setProperty("jacorb.regression.disable_security",
                                "true");


        ClientServerSetup setup =
          new ClientServerSetup (suite,
                                 "org.jacorb.test.orb.BasicServerImpl",
                                 clientProps, serverProps);

        suite.addTest (new C_IIOP_S_WIOPTest ("testConnection", setup));

        return setup;
    }

    public void testConnection()
    {
        try
        {
            server.ping();
            fail ("should have been a COMM_FAILURE");
        }
        catch (org.omg.CORBA.COMM_FAILURE ex)
        {
            // ok
        }
        catch (Exception ex)
        {
            fail ("expected COMM_FAILURE, got " + ex);
        }
    }
}
