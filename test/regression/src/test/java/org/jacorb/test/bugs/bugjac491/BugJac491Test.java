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

package org.jacorb.test.bugs.bugjac491;

import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.AttrDescriptionSeqHelper;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.OctetSeqHelper;

public class BugJac491Test extends ORBTestCase
{
    @Test (expected=BAD_OPERATION.class)
    public void testBugJac491() throws Exception
    {
        org.omg.CORBA.Any testAny = orb.create_any();
        OctetSeqHelper.insert (testAny, new byte[] { (byte)1 });
        byte []result1 = OctetSeqHelper.extract (testAny);
        assertTrue (result1.length == 1);

        testAny = orb.create_any();
        AttrDescriptionSeqHelper.extract (testAny);
    }
}
