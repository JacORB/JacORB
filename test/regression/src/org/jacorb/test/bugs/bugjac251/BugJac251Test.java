package org.jacorb.test.bugs.bugjac251;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;

/**
 * <code>TestCase</code> tests extract of a IDL structure using DynamicAny.
 *
 * @author Nick Cross
 */
public class BugJac251Test extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private JAC251 server;

    /**
     * Creates a new <code>TestCase</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public BugJac251Test (String name, ClientServerSetup setup)
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
        server = JAC251Helper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    /**
     * <code>suite</code> sets up the server/client tests.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "DynAny Tests" );

        ClientServerSetup setup = new ClientServerSetup
            (suite, JAC251Impl.class.getName());

        TestUtils.addToSuite(suite, setup, BugJac251Test.class);

        return setup;
    }


    /**
     * <code>test_dynstruct</code> ensures we do not get a MARSHAL exception
     * when using DynamicAnys on the server side.
     */
    public void test_dynstruct()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

//        BasicConfiguration bc = BasicConfigurationHelper.narrow
//        (orb.string_to_object (basicConfig));

        NameComponent n1 = new NameComponent ("id", null);

        MOidpair ref1 = new MOidpair ();
        ref1.name (new NameComponent[]{n1});

        NameComponent n2 = new NameComponent ("id2", null);
        MOidpair ref2 = new MOidpair ();
        ref2.name (new NameComponent[]{n2});

        InternalStruct element2 = new InternalStruct (ref1, ref2);
        Struct3 tosend = new Struct3 (3, element2 , true);

        Any tosendany = orb.create_any ();
        Struct3Helper.insert (tosendany, tosend);

        server.pass_any ("struct", tosendany);
    }


    /**
     * <code>test_dynarray</code> ensures we do not get a MARSHAL exception
     * when using DynamicAnys on the server side.
     */
    public void test_dynarray()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

//        BasicConfiguration bc = BasicConfigurationHelper.narrow
//        (orb.string_to_object (basicConfig));

        NameComponent n1 = new NameComponent ("id", null);
        NameComponent n2 = new NameComponent ("id2", null);

        NameComponent []tosend = new NameComponent[2];
        tosend[0] = n1;
        tosend[1] = n2;

        Any tosendany = orb.create_any ();
        NameArrayHelper.insert (tosendany, tosend);

        server.pass_any ("array", tosendany);
    }


    /**
     * <code>test_dynunion</code> ensures we do not get a MARSHAL exception
     * when using DynamicAnys on the server side.
     */
    public void test_dynunion()
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();

//        BasicConfiguration bc = BasicConfigurationHelper.narrow
//        (orb.string_to_object (basicConfig));

        MORef mo = new MORef (1000, null, "MORef");
        MOidpair tosend = new MOidpair();
        tosend.ref(mo);

        Any tosendany = orb.create_any ();
        MOidpairHelper.insert (tosendany, tosend);

        server.pass_any ("union", tosendany);
    }
}
