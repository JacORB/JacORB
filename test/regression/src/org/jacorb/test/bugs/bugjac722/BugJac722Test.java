/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.bugs.bugjac722;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.AnyServer;
import org.jacorb.test.orb.AnyServerHelper;
import org.jacorb.test.orb.AnyServerImpl;
import org.omg.CORBA.Any;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BugJac722Test extends ClientServerTestCase
{
    private AnyServer server;

    public BugJac722Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(BugJac722Test.class.getName());

        ClientServerSetup setup = new ClientServerSetup(suite, AnyServerImpl.class.getName());

        TestUtils.addToSuite(suite, setup, BugJac722Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testPart()
    {
        Part part = new PartImpl();
        part.m_value = "test";

        Any any = setup.getClientOrb().create_any();

        PartHelper.insert(any, part);

        Part bounced = PartHelper.extract(server.bounce_any(any));

        assertEquals(part.m_value, bounced.m_value);
    }

    public void testEmptyWhole()
    {
        Whole whole = new WholeImpl();

        Any any = setup.getClientOrb().create_any();

        WholeHelper.insert(any, whole);

        Whole bounced = WholeHelper.extract(server.bounce_any(any));
        assertNull(bounced.m_headPart);
        assertNull(bounced.m_tailPart);
    }

    public void testWholeEmptyParts()
    {
        Whole whole = new WholeImpl();
        whole.m_headPart = new PartImpl();
        whole.m_tailPart = new PartImpl();
        Any any = setup.getClientOrb().create_any();

        WholeHelper.insert(any, whole);

        Whole bounced = WholeHelper.extract(server.bounce_any(any));
        assertNotNull(bounced.m_headPart);
        assertNotNull(bounced.m_tailPart);
    }

   public void testWhole()
   {
       Part headPart = new PartImpl();
       headPart.m_value = "head";

       Part tailPart = new PartImpl();
       tailPart.m_value = "tail";

       Whole whole = new WholeImpl();
       whole.m_headPart = headPart;
       whole.m_tailPart = tailPart;

       Any any = setup.getClientOrb().create_any();

       WholeHelper.insert(any, whole);

       Whole bounced = WholeHelper.extract(server.bounce_any(any));
       assertEquals(headPart.m_value, bounced.m_headPart.m_value);
       assertEquals(tailPart.m_value, bounced.m_tailPart.m_value);
   }

   public void testPartStructInAny()
   {
       Part part = new PartImpl();
       part.m_value = "head";

       PartStruct struct = new PartStruct(part);

       Any any = setup.getClientOrb().create_any();

       PartStructHelper.insert(any, struct);

       PartStruct bounced = PartStructHelper.extract(server.bounce_any(any));
       assertEquals(part.m_value, bounced.m_part.m_value);
   }
}
