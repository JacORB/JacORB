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

package org.jacorb.test.bugs.bugjac486;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alphonse Bendt
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugJac486Test extends ClientServerTestCase
{
    private static final String objectKey = "/BugJac486Test/BugJac486POA/BugJac486ID";
    private static final String sslPort = "48120";

    @Test
    public void testSSLIOPCorbalocRequiresGIOP12_1() throws Exception
    {
        try
        {
            IIOPProfile profile = new IIOPProfile("corbaloc:ssliop:localhost:" + sslPort + objectKey);
            profile.configure(((org.jacorb.orb.ORB)setup.getClientOrb()).getConfiguration());
            fail();
        }
        catch(IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testAccessSecureAcceptorWithoutSSLShouldFail() throws Exception
    {
        try
        {
            runTest("corbaloc::1.2@localhost:" + sslPort + objectKey);
            fail();
        }
        catch(Exception e)
        {
        }
    }

    @Test
    public void testAccessSSLWithJacorbSpecificExtension() throws Exception
    {
        runTest("corbaloc:ssliop:1.2@localhost:" + sslPort + objectKey);
    }

    private void runTest(String corbaLoc)
    {
        org.omg.CORBA.Object object = setup.getClientOrb().string_to_object(corbaLoc);
        BasicServer server = BasicServerHelper.narrow(object);
        assertEquals("BugJac486Test", server.bounce_string("BugJac486Test"));
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeTrue(TestUtils.isSSLEnabled);

        Properties clientProps = new Properties();
        Properties serverProps = new Properties();

        serverProps.setProperty("jacorb.test.corbaloc.enable", "true");
        serverProps.setProperty("jacorb.test.corbaloc.sslport", sslPort);
        serverProps.setProperty("jacorb.test.corbaloc.implname", "BugJac486Test");
        serverProps.setProperty("jacorb.test.corbaloc.poaname", "BugJac486POA");
        serverProps.setProperty("jacorb.test.corbaloc.objectid", "BugJac486ID");

        clientProps.setProperty("jacorb.connection.client.disconnect_after_systemexception", "true");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), clientProps, serverProps);
    }
}
