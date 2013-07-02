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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.test.bugs.bugjac513;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;

/**
 * @author Alphonse Bendt
 */
public class GIOP_1_0_SSL_Test extends ClientServerTestCase
{
    public GIOP_1_0_SSL_Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties clientProps = new Properties();
        Properties serverProps = new Properties();

        clientProps.setProperty("jacorb.test.ssl", "true");

        serverProps.setProperty("jacorb.giop_minor_version", "0");
        serverProps.setProperty("jacorb.test.ssl", "true");

        TestSuite suite = new TestSuite(GIOP_1_0_SSL_Test.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), clientProps, serverProps);
        TestUtils.addToSuite(suite, setup, GIOP_1_0_SSL_Test.class);
        return setup;
    }

    public void testCanConnectToGIOP_10_IOR() throws Exception
    {
        BasicServer server = BasicServerHelper.narrow(setup.getServerObject());

        assertEquals(getName(), server.bounce_string(getName()));
    }
}
