package org.jacorb.test.bugs.bugjac192b;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.Properties;

import junit.framework.TestCase;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


/**
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac192bTest extends TestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private JAC192b server;

    static boolean interceptorCalled;

    private ORB orb;

    /**
     * <code>setUp</code> sets up this test.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        Properties client_props = new Properties();

        client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.CInitializer",
        "org.jacorb.test.bugs.bugjac192b.CInitializer");

        orb = ORB.init (new String[0], client_props );
        POA clientRootPOA = POAHelper.narrow
        ( orb.resolve_initial_references( "RootPOA" ) );
        clientRootPOA.the_POAManager().activate();

        JAC192bImpl servant = new JAC192bImpl();
        byte[] oid = clientRootPOA.servant_to_id (servant);
        org.omg.CORBA.Object serverObject = clientRootPOA.id_to_reference (oid);

        Thread orbRunner = new Thread("ORBStartThread")
                {
                    public void run()
                    {
                        orb.run();
                    }
                };
        orbRunner.start();

        server = JAC192bHelper.narrow( serverObject );
    }

    protected void tearDown() throws Exception
    {
        orb.shutdown(true);
    }

    /**
     * <code>test_interceptorerror</code> tests that if an interceptor throws a
     * system exception following local calls still call interceptors.
     *
     */
    public void test_interceptorerror()
    {
        try
        {
            server.test192bOp();
        }
        // We expect a internal to be thrown from the interceptor.
        catch (INTERNAL e)
        {
            // expected
        }// NOPMD

        server.test192bOp();

        assertTrue ("Interceptor was not called for the second time", interceptorCalled);
    }
}
