package org.jacorb.test.bugs.bugjac257;

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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.BAD_INV_ORDER;

/**
 * <code>TestCase</code> test whether the ORB correctly throws
 * an exception after it has been shutdown. The test covers calling
 * an operation on an object.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac257Test extends ClientServerTestCase
{
    /**
     * The hello world <code>server</code>.
     *
     */
    private JAC257 server;


    /**
     * Creates a new <code>TestCase</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public BugJac257Test (String name, ClientServerSetup setup)
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
        server = JAC257Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>suite</code> sets up the server/client tests.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Client/server TypeCode tests" );

        ClientServerSetup setup = new ClientServerSetup( suite, JAC257Impl.class.getName());

        TestUtils.addToSuite(suite, setup, BugJac257Test.class);

        return setup;
    }

    /**
     * <code>test_orb_destroy</code> tests ORB shutdown/destroy
     */
    public void test_orb_destroy()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

        server.hello("First call");
        server.hello("Second call");

        orb.shutdown (true);
        orb.destroy ();

        try
        {
            server.hello("Should not work call");
        }
        catch (BAD_INV_ORDER e)
        {
            // expected
        }
    }
}
