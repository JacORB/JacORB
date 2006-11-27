package org.jacorb.test.bugs.bugjac262;

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

public class BugJac262Test extends ClientServerTestCase
{
    private ComplexTypeCodesServer server;

    public BugJac262Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = ComplexTypeCodesServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite
            ( "Client/server recursive type codes tests" );
        ClientServerSetup setup = new ClientServerSetup
            ( suite, ComplexTypeCodesServerImpl.class.getName() );

        TestUtils.addToSuite(suite, setup, BugJac262Test.class);

        return setup;
    }

    public void test_mixed_typecodes()
    {
        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        any.insert_TypeCode(MixedStructsHelper.type());
        server.passAny( any );
    }

    public void test_repeated_sequence_typecodes()
    {
        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        any.insert_TypeCode(RepeatedSeqStructHelper.type());
        server.passAny(any);
    }

    public void test_repeated_typecodes()
    {
        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        any.insert_TypeCode(RepeatedStructHelper.type());

        server.passAny(any);
    }

    public void test_array_typecodes()
    {
        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        any.insert_TypeCode(RepeatedArrayStructHelper.type());
        server.passAny(any);
    }

    public void test_object_typecodes()
    {
        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        any.insert_TypeCode(RepeatedObjectStructHelper.type());
        server.passAny(any);
    }
}
