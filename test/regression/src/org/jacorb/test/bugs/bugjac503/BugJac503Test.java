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

package org.jacorb.test.bugs.bugjac503;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.jacorb.test.orb.etf.AbstractWIOPTestCase;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.omg.CORBA.ORB;
import org.omg.RTCORBA.Protocol;

/**
 * @author Alphonse Bendt
 */
public class BugJac503Test extends AbstractWIOPTestCase
{
    private BasicServer basicServer;
    private ORB clientOrb;

    public BugJac503Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        return new TestSuite();
    }
    
    // temporary disabled
    public static Test _suite()
    {
        TestSuite suite = new TestSuite ("Profile Selector");

        // client ORB from setup is not used.
        Properties clientProps = new Properties();

        // WIOP does not support SSL.
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        clientProps.setProperty("jacorb.transport.factories",
                "org.jacorb.orb.iiop.IIOPFactories," +
                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        clientProps.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.init1", ClientORBInit.class.getName());

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.transport.factories",
                                "org.jacorb.orb.iiop.IIOPFactories," +
                                "org.jacorb.test.orb.etf.wiop.WIOPFactories");

        ClientServerSetup setup =
          new ClientServerSetup (suite,
                                 BasicServerImpl.class.getName(),
                                 clientProps,
                                 serverProps);

        TestUtils.addToSuite(suite, setup, BugJac503Test.class);

        return setup;
    }

    protected void doSetUp() throws Exception
    {
        // need a unbound delegate for every testrun.
        clientOrb = setup.getClientOrb();
        basicServer = BasicServerHelper.narrow(clientOrb.string_to_object(clientOrb.object_to_string(server)));
    }

    public void testShouldUseIIOPByDefault() throws Exception
    {
        basicServer.ping();

        assertFalse(WIOPFactories.isTransportInUse());
    }

    public void testForwardRequestInClientInterceptor() throws Exception
    {
        WIOPFactories factories = new WIOPFactories();
        factories.configure (((org.jacorb.orb.ORB)clientOrb).getConfiguration ());
        ForwardInterceptor.protocols = new Protocol[] {new Protocol(factories.profile_tag(), null, null)};

        basicServer.ping();

        assertTrue(WIOPFactories.isTransportInUse());
    }
}
