package org.jacorb.test.orb.value;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;

/**
 * Tests abstract interface with value types and concrete interface.
 */
public class AbstractInterfaceTest extends ClientServerTestCase
{
    private Passer passer;
    private ORB orb;


    @Before
    public void setUp() throws Exception
    {
        passer = PasserHelper.narrow( setup.getServerObject() );
        orb = setup.getClientOrb();

        ((org.omg.CORBA_2_3.ORB)orb).register_value_factory(StringNodeHelper.id(), new StringNodeDefaultFactory());
    }

    @After
    public void tearDown() throws Exception
    {
        passer = null;
        orb = null;
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.orb.value.PasserImpl" );
    }

    @Test
    public void test_all ()
    {
        BaseHolder pkg = new BaseHolder ();
        passer.pass_state(pkg);
        assertNotNull("passer returned null for state value", pkg.value);

        this.test_state(pkg.value);

        pkg.value = null;
        passer.pass_ops(pkg);
        assertNotNull("passer returned null for interface value", pkg.value);

        this.test_ops(pkg.value);

        this.test_exception(pkg.value);

        passer.pass_nil(pkg);

        assertNull ("passer returned not null for null value", pkg.value);
    }

    private void test_ops (Base base)
    {
        try
        {
            base.base_op ("base_op");
        }
        catch (BadInput e)
        {
            fail (e.toString());
        }

        Foo foo = FooHelper.narrow(base);

        assertNotNull (foo);

        try
        {
            foo.foo_op("foo_op");
        }
        catch (BadInput e)
        {
            fail (e.toString());
        }

        try
        {
            foo.base_op ("base_op");
        }
        catch (BadInput e)
        {
            fail (e.toString());
        }
    }

    private void test_exception (Base base)
    {
        try
        {
            base.base_op ("bad_name");
            fail ("base_op returned without exception on bad call");
        }
        catch (BadInput e)
        {
            // expected exception
        }
    }

    private void test_state (Base base)
    {
        assertTrue (base instanceof TreeController);

        TreeController tc = (TreeController)base;

        assertNotNull(tc.root);
        assertTrue(tc.root instanceof StringNode);
        assertEquals("RootNode", ((StringNode)tc.root).name);
        assertNotNull(tc.root.left);
        assertTrue(tc.root.left instanceof StringNode);
        assertNotNull(tc.root.right);
        assertTrue(tc.root.right instanceof StringNode);
    }

}
