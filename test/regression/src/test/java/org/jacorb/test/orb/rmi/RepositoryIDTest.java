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

package org.jacorb.test.orb.rmi;

import static org.junit.Assert.assertEquals;
import org.jacorb.ir.RepositoryID;
import org.jacorb.util.ValueHandler;
import org.junit.Test;

public class RepositoryIDTest
{
    /**
     * test for bug #528
     */
    @Test
    public void testClassName()
    {
        String id = ValueHandler.getRMIRepositoryID(Outer.StaticInner.class);
        String clazzName = RepositoryID.className(id, getClass().getClassLoader());
        assertEquals(Outer.StaticInner.class.getName(), clazzName);
    }
}
