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

package org.jacorb.test.bugs.bugjac503;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.jacorb.test.orb.etf.AbstractWIOPTestCase;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.RTCORBA.Protocol;

/**
 * @author Alphonse Bendt
 */
public class BugJac503Test extends AbstractWIOPTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        // client ORB from setup is not used.
        Properties clientProps = new Properties();

        clientProps.setProperty("jacorb.transport.factories",
                "org.jacorb.orb.iiop.IIOPFactories," +
                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        clientProps.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.init1", ClientORBInit.class.getName());

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        setup = new ClientServerSetup(
                                 BasicServerImpl.class.getName(),
                                 clientProps,
                                 serverProps);
    }

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        server._release();
        // need a unbound delegate for every testrun.
        server = BasicServerHelper.narrow(setup.getClientOrb().string_to_object(setup.getClientOrb().object_to_string(server)));
    }

    @Test
    public void testShouldUseIIOPByDefault() throws Exception
    {
        server.ping();

        assertFalse(WIOPFactories.isTransportInUse());
    }

    @Test
    public void testForwardRequestInClientInterceptor() throws Exception
    {
        ForwardInterceptor.protocols = new Protocol[] {new Protocol(new WIOPFactories().profile_tag(), null, null)};
        server.ping();

        assertTrue(WIOPFactories.isTransportInUse());
    }
}
