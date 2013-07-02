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

package org.jacorb.test.bugs.bugjac562;

import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * @author Alphonse Bendt
 */
public class ClientInterceptorTest extends ORBTestCase
{
    BasicServer server;

    protected void patchORBProperties(String testName, Properties props) throws Exception
    {
        props.setProperty("jacorb.orb_initializer.fail_on_error", "true");
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + ClientInterceptorInit.class.getName(), "");
    }

    protected void doSetUp() throws Exception
    {
        POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        server = BasicServerHelper.narrow(poa.servant_to_reference(new BasicServerImpl()));
    }

    /**
     * <code>tearDown</code> us used by Junit for cleaning up after the tests.
     *
     * @exception Exception if an error occurs
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        server._release ();
        server = null;
    }

    public void testRuntimeExceptionInClientInterceptorIsPropagated() throws Exception
    {
        try
        {
            server.ping();
            fail();
        }
        catch(RuntimeException e)
        {
            // expected exception
        }
        try
        {
            server.ping();
            fail();
        }
        catch(org.omg.CORBA.TRANSIENT e)
        {
            // expected exception
        }
        catch(RuntimeException e)
        {
            fail();
        }
    }
}
