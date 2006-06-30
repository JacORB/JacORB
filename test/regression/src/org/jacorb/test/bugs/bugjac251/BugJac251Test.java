package org.jacorb.test.bugs.bugjac251;

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

import org.jacorb.test.bugs.bugpt251.BasicConfiguration;
import org.jacorb.test.bugs.bugpt251.BasicConfigurationHelper;
import org.jacorb.test.bugs.bugpt251.InternalStruct;
import org.jacorb.test.bugs.bugpt251.MORef;
import org.jacorb.test.bugs.bugpt251.MOidpair;
import org.jacorb.test.bugs.bugpt251.MOidpairHelper;
import org.jacorb.test.bugs.bugpt251.NameArrayHelper;
import org.jacorb.test.bugs.bugpt251.NameComponent;
import org.jacorb.test.bugs.bugpt251.PT251Helper;
import org.jacorb.test.bugs.bugpt251.Struct3;
import org.jacorb.test.bugs.bugpt251.Struct3Helper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;

/**
 * <code>TestCase</code> tests extract of a IDL structure using DynamicAny.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac251Test extends ClientServerTestCase
{
    /**
     * <code>basicConfig</code> allows me to create a BasicConfiguration object.
     * via string_to_object to pass through.
     */
    private static final String basicConfig = "IOR:000000000000003949444C3A6F72672F6A61636F72622F746573742F627567732F62756770743235312F4261736963436F6E66696775726174696F6E3A312E3000000000000000020000000000000068000102000000000931302E312E302E340000923000000015393534343536353234312F00141B10034C30113320000000000000020000000000000008000000004A414300000000010000001C0000000000010001000000010501000100010109000000010501000100000001000000500000000000000002000000010000001C00000000000100010000000105010001000101090000000105010001000000010000001C00000000000100010000000105010001000101090000000105010001";

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

        BasicConfiguration bc = BasicConfigurationHelper.narrow
        (orb.string_to_object (basicConfig));

        NameComponent n1 = new NameComponent ("id", bc);

        MOidpair ref1 = new MOidpair ();
        ref1.name (new NameComponent[]{n1});

        NameComponent n2 = new NameComponent ("id2", bc);
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

        BasicConfiguration bc = BasicConfigurationHelper.narrow
        (orb.string_to_object (basicConfig));

        NameComponent n1 = new NameComponent ("id", bc);
        NameComponent n2 = new NameComponent ("id2", bc);

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

        BasicConfiguration bc = BasicConfigurationHelper.narrow
        (orb.string_to_object (basicConfig));

        MORef mo = new MORef (1000, bc, "MORef");
        MOidpair tosend = new MOidpair();
        tosend.ref(mo);

        Any tosendany = orb.create_any ();
        MOidpairHelper.insert (tosendany, tosend);

        server.pass_any ("union", tosendany);
    }
}
