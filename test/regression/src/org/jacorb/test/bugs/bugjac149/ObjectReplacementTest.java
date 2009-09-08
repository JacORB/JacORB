package org.jacorb.test.bugs.bugjac149;

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

import java.io.Serializable;
import java.util.Properties;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * <code>ObjectReplacementTest</code> tests toString and equals generation on the stub.
 * Test supplied by Cisco
 */
public class ObjectReplacementTest extends ClientServerTestCase
{
    /**
     * Creates a new <code>ObjectReplacementTest</code> instance.
     *
     * @param name a <code>String</code> value
     */
    public ObjectReplacementTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }


    /**
     * <code>suite</code> is the suite of tests.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        if (TestUtils.isJ2ME())
        {
            return new TestSuite();
        }

        TestSuite suite = new TestSuite ("ObjectReplacement Test");
        Properties props = new Properties();

        ObjectReplacementSetup setup =
            new ObjectReplacementSetup (suite, props, props);

        TestUtils.addToSuite(suite, setup, ObjectReplacementTest.class);

        return setup;
    }


    /**
     * <code>testObjectReplacement</code>
     *
     */
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
