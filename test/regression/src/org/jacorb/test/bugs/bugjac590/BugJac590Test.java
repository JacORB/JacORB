package org.jacorb.test.bugs.bugjac590;


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


import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.orb.dynany.DynUnion;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;


/**
 * <code>BugJac590Test</code> verifies a null ptr with Unions and DynAny.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac590Test extends ClientServerTestCase
{
    private BooleanUnionInt server = null;

    public BugJac590Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = BooleanUnionIntHelper.narrow (setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "bugjac590" );
        ClientServerSetup setup =
        new ClientServerSetup( suite,
                               "org.jacorb.test.bugs.bugjac590.BooleanUnionIntServerImpl" );

        TestUtils.addToSuite(suite, setup, BugJac590Test.class);

        return setup;
    }

    /**
     * <code>testUnionHelper</code> checks that demarshalling with an enum
     * discriminator sets it correctly.
     *
     * @exception Exception if an error occurs
     */
    public void testUnionNullPtr () throws Exception
    {
        // obtain a reference to the DynAnyFactory
        org.omg.CORBA.Object genFactory = setup.getClientOrb ().resolve_initial_references("DynAnyFactory");
        DynAnyFactory factory = DynAnyFactoryHelper.narrow(genFactory);

        org.jacorb.orb.dynany.DynEnum dynenum1 = (org.jacorb.orb.dynany.DynEnum) factory
        .create_dyn_any_from_type_code(MyEnumHelper.type());
        dynenum1.set_as_ulong(MyEnum._A);

        DynUnion dynany2 = (DynUnion) factory.create_dyn_any_from_type_code(EnumUnionHelper.type());
        dynany2.set_discriminator(dynenum1);

        assertTrue (dynany2.to_any () != null);

        org.omg.CORBA.Any aa = dynany2.to_any ();
        EnumUnion eu = EnumUnionHelper.extract (aa);

        server.e (eu);
        server.g (eu, new EnumUnionHolder (eu));

        org.omg.CORBA.Request r = server._request("g");
        r.set_return_type(setup.getClientOrb ().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
        EnumUnionHelper.insert (r.add_in_arg() , eu);

        r.add_in_arg().insert_any(dynany2.to_any());
        org.omg.CORBA.Any out_arg = r.add_out_arg();
        out_arg.type(EnumUnionHelper.type());

        r.invoke();

        if (r.env().exception() != null)
        {
            throw r.env().exception();
        }
        else
        {
            System.out.println("0: " + r.return_value());
        }
    }
}
