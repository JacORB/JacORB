package org.jacorb.test.bugs.bugjac149;

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

import static org.junit.Assert.assertNotNull;
import java.io.Serializable;
import javax.rmi.PortableRemoteObject;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <code>ObjectReplacementTest</code> tests toString and equals generation on the stub.
 * Test supplied by Cisco
 */
public class ObjectReplacementTest extends ClientServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup (
                "org.jacorb.test.bugs.bugjac149.ObjRepServer",
                "org.jacorb.test.bugs.bugjac149.ObjRepServer",
                null,
                null
                );
    }


    /**
     * <code>testObjectReplacement</code>
     */
    @Test
    public void testObjectReplacement ()
    {
        org.omg.CORBA.Object remObj = setup.getServerObject();
        RemoteIPing remRef;

        remRef = (RemoteIPing) PortableRemoteObject.narrow(remObj, org.jacorb.test.bugs.bugjac149.RemoteIPing.class);

        IPing pinger = new PingProxy(remRef);

        Model model = new Model("Hello");
        Serializable result = pinger.ping(model);

        assertNotNull("Result not received", result);
    }
}
