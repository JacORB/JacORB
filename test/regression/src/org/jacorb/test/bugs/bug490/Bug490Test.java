package org.jacorb.test.bugs.bug490;

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

public class Bug490Test extends ClientServerTestCase
{
    private GoodDay server;

    public Bug490Test (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    public void setUp()
    {
        server = (GoodDay)GoodDayHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "bug 490 policy op" );
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.bugs.bug490.GoodDayImpl" );

        TestUtils.addToSuite(suite, setup, Bug490Test.class);

        return setup;
    }

    public void testPolicyOp()
    {
        server.policy();
    }
}
