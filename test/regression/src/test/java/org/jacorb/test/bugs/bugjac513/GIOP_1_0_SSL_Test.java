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

package org.jacorb.test.bugs.bugjac513;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class GIOP_1_0_SSL_Test extends ClientServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties clientProps = new Properties();
        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.giop_minor_version", "0");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), clientProps, serverProps);
    }

    @Test
    public void testCanConnectToGIOP_10_IOR() throws Exception
    {
        BasicServer server = BasicServerHelper.narrow(setup.getServerObject());
        assertTrue("Names should match ", name.getMethodName().equals (server.bounce_string(name.getMethodName())));
    }
}
