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

package org.jacorb.test.orb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.ORBTestCase;

public class CDROutputStreamTest extends ORBTestCase
{
    private CDROutputStream objectUnderTest;

    protected void doSetUp() throws Exception
    {
        objectUnderTest = new CDROutputStream(orb);
    }

    protected void doTearDown() throws Exception
    {
        objectUnderTest.close();
        objectUnderTest = null;
    }

    public void testIncreaseSize()
    {
        byte[] buffer = new byte[4];
        objectUnderTest.setBuffer(buffer);

        objectUnderTest.increaseSize(8);

        assertTrue(objectUnderTest.size() >= 8);
    }

    public static Test suite()
    {
        return new TestSuite(CDROutputStreamTest.class);
    }
}
