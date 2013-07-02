package org.jacorb.test.bugs.bugjac192b;

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

import java.util.Properties;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.INTERNAL;

/**
 * @author Nick Cross
 */
public class BugJac192bTest extends ORBTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private JAC192b server;

    static boolean interceptorCalled;

    /**
     * <code>setUp</code> sets up this test.
     *
     * @exception Exception if an error occurs
     */
    public void doSetUp() throws Exception
    {
        JAC192bImpl servant = new JAC192bImpl();
        byte[] oid = rootPOA.servant_to_id (servant);
        org.omg.CORBA.Object serverObject = rootPOA.id_to_reference (oid);

        server = JAC192bHelper.narrow( serverObject );
    }

    protected void patchORBProperties(String testName, Properties client_props) throws Exception
    {
        client_props.put("org.omg.PortableInterceptor.ORBInitializerClass.CInitializer",
                         "org.jacorb.test.bugs.bugjac192b.CInitializer");
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
