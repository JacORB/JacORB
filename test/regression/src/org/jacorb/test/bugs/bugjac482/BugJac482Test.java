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

package org.jacorb.test.bugs.bugjac482;

import java.io.File;
import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 */
public class BugJac482Test extends TestCase
{
    private Properties props;

    protected void setUp() throws Exception
    {
        props = new Properties();
        props.setProperty("jacorb.security.support_ssl", "on");
        props.setProperty(SocketFactoryManager.SSL_SOCKET_FACTORY, "org.jacorb.security.ssl.sun_jsse.SSLSocketFactory");
        props.setProperty(SocketFactoryManager.SSL_SERVER_SOCKET_FACTORY, "org.jacorb.security.ssl.sun_jsse.SSLServerSocketFactory");
    }

    public void testMissingKeyStoreShouldCauseException() throws Exception
    {

        ORB orb = ORB.init(new String[0], props);
        try
        {
            orb.resolve_initial_references("RootPOA");
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }
    }

    public void testEmptyKeyStoreShouldCauseException() throws Exception
    {
        File emptyFile = File.createTempFile("non_existing_keystore", ".kst");
        emptyFile.deleteOnExit();

        props.setProperty("jacorb.security.keystore", emptyFile.toString());
        props.setProperty("jacorb.security.keystore_password", "pass");

        ORB orb = ORB.init(new String[0], props);

        try
        {
            orb.resolve_initial_references("RootPOA");
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }
    }

    public void testNonExistingKeyStoreShouldCauseException() throws Exception
    {
        props.setProperty("jacorb.security.keystore", "/not/existing/path");
        props.setProperty("jacorb.security.keystore_password", "pass");

        ORB orb = ORB.init(new String[0], props);

        try
        {
            orb.resolve_initial_references("RootPOA");
            fail();
        }
        catch(INITIALIZE e)
        {
            // expected
        }
    }
}
