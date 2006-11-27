package org.jacorb.test.orb;

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

import junit.framework.*;
import org.jacorb.test.common.*;
import org.jacorb.test.*;
import org.jacorb.test.RecursiveParamServerPackage.*;
import org.jacorb.test.RecursiveParamServerPackage.ParmPackage.*;

public class RecursiveParam extends ClientServerTestCase
{
    private RecursiveParamServer server;

    public RecursiveParam(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = RecursiveParamServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite
            ( "Client/server recursiveparam tests" );
        ClientServerSetup setup = new ClientServerSetup
            ( suite, RecursiveParamServerImpl.class.getName() );

        suite.addTest( new RecursiveParam( "test_param1", setup ) );

        return setup;
    }

    public void test_param1()
    {
        ParmValue pv = new ParmValue();
        pv.string_value("inner");
        Parm p = new Parm("v", pv );

        ParmValue pvi = new ParmValue();
        Parm[][] pp = new Parm[1][1];
        pp[0] = new Parm[]{p};
        pvi.nested_value( pp );

        Parm outerParm = new Parm("outer", pvi  );
        server.passParm( outerParm );

        org.omg.CORBA.Any any = setup.getClientOrb().create_any();

        blubT union = new blubT();
        blubT[] blubs = new blubT[0];

        union.b( blubs );
        blubTHelper.insert( any, union );

        server.passAny( any );
    }
}
