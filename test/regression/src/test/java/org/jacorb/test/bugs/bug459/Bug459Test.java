/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
 *
 */

package org.jacorb.test.bugs.bug459;

import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.orb.AnyServerPOA;
import org.junit.Test;
import org.omg.CORBA.Any;

public class Bug459Test extends ORBTestCase
{
    class MyAnyServer extends AnyServerPOA
    {
        @Override
        public Any bounce_any(Any inAny)
        {
            return inAny;
        }
    }

    @Test
    public void testIt() throws Exception
    {
        MyAnyServer myServer = new MyAnyServer();

        for (int run = 0; run < 5; run++)
        {
            org.omg.CORBA.Object o = myServer._this(getAnotherORB(null));
            o._release();
        }
    }


    @Test
    public void testVerifyMultipleThisCalls() throws Exception
    {
        MyAnyServer myServer = new MyAnyServer();
        org.omg.CORBA.Object o = myServer._this(orb);
        o = myServer._this(orb);
        o._release();
    }
}
