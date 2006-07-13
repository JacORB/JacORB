/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BasicTest extends TestCase
{
    public void test_Is_JacORB_POA_a_POA() throws Exception
    {
        ORB orb = ORB.init(new String[0], null);
        try
        {
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            assertTrue(poa._is_a(POAHelper.id()));
        }
        finally
        {
            orb.shutdown(true);
        }
    }

    public static Test suite() throws Exception
    {
        return new TestSuite(BasicTest.class);
    }
}
