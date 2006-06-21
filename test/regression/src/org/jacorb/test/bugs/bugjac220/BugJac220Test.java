package org.jacorb.test.bugs.bugjac220;

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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac220Test extends ClientServerTestCase
{
    private TestEnum server;

    /**
     * Creates a new <code>TestCase</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public BugJac220Test (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> is used by Junit for initialising the tests.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = TestEnumHelper.narrow( setup.getServerObject() );
    }


    /**
     * <code>suite</code> sets up the server/client tests.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Enum TypeCode tests" );

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        /* Compact typecodes needs to be set to perform the test*/
        client_props.setProperty ("jacorb.compactTypecodes", "2");
        server_props.setProperty ("jacorb.compactTypecodes", "2");

        ClientServerSetup setup =
            new ClientServerSetup
               ( suite,
                 TestEnumImpl.class.getName(),
                 client_props,
                 server_props);

        TestUtils.addToSuite(suite, setup, BugJac220Test.class);

        return setup;
    }


    /**
     * <code>test_enum_tc</code>
     */
    public void test_enum_tc()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

        org.omg.CORBA.Any any = orb.create_any();

        any.insert_TypeCode
            (orb.create_enum_tc
                 ("IDL:org/jacorb/test/bugs/bugjac220/TestEnum:1.0",
                  "TestCase",
                  new String [] {"Test"}));

        server.push (any);
    }
}
