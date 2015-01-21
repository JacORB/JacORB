package org.jacorb.test.bugs.bugjac257;

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

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.BAD_INV_ORDER;

/**
 * <code>TestCase</code> test whether the ORB correctly throws
 * an exception after it has been shutdown. The test covers calling
 * an operation on an object.
 *
 * @author Nick Cross
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

    /**
     * <code>setUp</code> is used by Junit for initialising the tests.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        server = JAC257Helper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        setup = new ClientServerSetup(JAC257Impl.class.getName());
    }

    /**
     * <code>test_orb_destroy</code> tests ORB shutdown/destroy
     */
    @Test
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
