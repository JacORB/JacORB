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

package org.jacorb.test.orb.rmi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.ir.RepositoryID;
import org.jacorb.util.ValueHandler;

public class RepositoryIDTest extends TestCase
{
    /**
     * test for bug #528
     */ 
    public void testClassName()
    {
        String id = ValueHandler.getRMIRepositoryID(Outer.StaticInner.class);
        String clazzName = RepositoryID.className(id, getClass().getClassLoader());
        assertEquals(Outer.StaticInner.class.getName(), clazzName);
    }
    
    public static Test suite()
    {
        return new TestSuite(RepositoryIDTest.class);
    }
}
