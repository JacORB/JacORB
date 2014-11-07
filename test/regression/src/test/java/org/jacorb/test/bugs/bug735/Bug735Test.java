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

package org.jacorb.test.bugs.bug735;

import static org.junit.Assert.fail;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.TRANSIENT;

/**
 * @author Alphonse Bendt
 */
public class Bug735Test extends ORBTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
    }

    @Test
    public void testIsAFailsWithTaoIOR()
    {
        final String ior = "IOR:010000000100000000000000010000000000000060000000000102600000000f656c6f6e736"
            + "1706f647270353035000010d600000000003a14010f004e55500000001a0000000000000001526f"
            + "6f74504f4100747365727665725252000000000000000001547261646553727641646d696e00000"
            + "0000000";

        org.omg.CORBA.Object object = orb.string_to_object(ior);

        try
        {
            object._is_a("something");
            fail();
        }
        catch(TRANSIENT e)
        {
            // expected
        }
    }
}
