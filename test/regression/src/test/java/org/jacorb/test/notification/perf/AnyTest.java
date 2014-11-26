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

package org.jacorb.test.notification.perf;

import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.notification.Address;
import org.jacorb.test.notification.AddressHelper;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;

public class AnyTest extends ORBTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeTrue(System.getProperty("jacorb.test.notificationperf", "false").equals("true"));
    }

    @Test
    public void testAny()
    {
        Any theAny = null;
        long startTime = 0;
        long endTime = 0;

        Address addr = new Address();
        addr.city = "Berlin";
        addr.number = 10;
        addr.street = "Street";

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++)
        {
            theAny = orb.create_any();

            AddressHelper.insert(theAny, addr);
        }

        endTime = System.currentTimeMillis();

        TestUtils.getLogger().debug("\"MyType To Any\" test: " + (endTime - startTime)

        + " ms for 100,000 trials");

        theAny = orb.create_any();

        AddressHelper.insert(theAny, addr);

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++)
        {
           AddressHelper.extract(theAny);
        }

        endTime = System.currentTimeMillis();

        TestUtils.getLogger().debug("\"MyType From Any\" test: " + (endTime - startTime)

        + " ms for 100,000 trials");
    }
}
