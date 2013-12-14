package org.jacorb.test.bugs.bug401;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.Serializable;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Test for bug 401, TypeCode problem when putting values into Anys.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 */
public class Bug401Test extends ClientServerTestCase
{
    private AnyServer server;

    @Before
    public void setUp()
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
        org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB)setup.getClientOrb();
        orb.register_value_factory(AHelper.id(), new AFactory());
        orb.register_value_factory(BHelper.id(), new BFactory());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.bugs.bug401.AnyServant");
    }

    @Test
    public void test_getA()
    {
        Any aa = server.getAnyA();
        assertTrue ("expected TypeCode of A, got: " + aa.type().toString(),
                    aa.type().equivalent(AHelper.type()));
        A a = AHelper.extract(aa);
        assertEquals (0xAA, a.aa);
    }

    @Test
    public void test_getB()
    {
        Any bb = server.getAnyB();
        assertTrue ("expected TypeCode of B, got: " + bb.type().toString(),
                    bb.type().equivalent(BHelper.type()));
        B b = BHelper.extract(bb);
        assertEquals (0xAA, b.aa);
        assertEquals (0xBB, b.bb);
    }

    @Test
    public void test_getAB()
    {
        Any[] ab = server.getAnyAB();
        assertTrue ("expected TypeCode of A, got: " + ab[0].type().toString(),
                    ab[0].type().equivalent(AHelper.type()));
        assertTrue ("expected TypeCode of B, got: " + ab[1].type().toString(),
                    ab[1].type().equivalent(BHelper.type()));
        A a = AHelper.extract(ab[0]);
        B b = BHelper.extract(ab[1]);
        assertEquals (0xAA, a.aa);
        assertEquals (0xAA, b.aa);
        assertEquals (0xBB, b.bb);
    }

    public static class AFactory implements ValueFactory
    {
        public Serializable read_value
                                 (org.omg.CORBA_2_3.portable.InputStream is)
        {
            A a = new A(){};
            return is.read_value(a);
        }
    }

    public static class BFactory implements ValueFactory
    {
        public Serializable read_value
                                 (org.omg.CORBA_2_3.portable.InputStream is)
        {
            B b = new B(){};
            return is.read_value(b);
        }
    }

}
