package org.jacorb.test.orb;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.POAManager;


/**
 * @author Nick Cross
 *
 * Verify non_existent works correctly with local calls
 */
public class NonExistentLocalTest extends ORBTestCase
{
    private POAManager poaManager;

    @Before
    public void setUp() throws Exception
    {
        poaManager = rootPOA.the_POAManager();
    }

    @After
    public void tearDown() throws Exception
    {
        poaManager = null;
    }

    @Test
    public void testNonExist() throws Exception
    {
        poaManager.activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        assertEquals(42, server.bounce_long(42));

        rootPOA.deactivate_object(rootPOA.servant_to_id(servant));

        try
        {
            boolean result = server._non_existent();

            assertTrue (result == true);
        }
        catch (OBJECT_NOT_EXIST e)
        {
            fail ("Should not have thrown an exception");
        }
    }
}
