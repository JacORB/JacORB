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

package org.jacorb.test.orb;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.util.List;
import org.jacorb.orb.BufferManager;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Verify that a CDROutputStream with a custom size buffer does not get returned
 * to the buffermanager.
 */
public class BufferTest extends ORBTestCase
{
    private CDROutputStream objectUnderTest;

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest = new CDROutputStream(orb);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBufferReturn()
    {
        byte[] buffer = new byte[1100];
        objectUnderTest.setBuffer(buffer);

        Field buffmgrf;
        Field buffpoolf;
        try
        {
            buffmgrf = CDROutputStream.class.getDeclaredField("bufMgr");
            buffmgrf.setAccessible(true);

            BufferManager buffermgr = (BufferManager) buffmgrf.get (objectUnderTest);

            buffpoolf = BufferManager.class.getDeclaredField("bufferPool");
            buffpoolf.setAccessible(true);
            List []bufferPool = (List[]) buffpoolf.get (buffermgr);
            List bp = bufferPool [ 1 ];

            int currentSize = bp.size();

            objectUnderTest.close();

            assertTrue ("Buffer should not have been returned to pool", bp.size() == currentSize);
        }
        catch (SecurityException e)
        {
            fail(e.toString());
        }
        catch (NoSuchFieldException e)
        {
            fail(e.toString());
        }
        catch (IllegalArgumentException e)
        {
            fail(e.toString());
        }
        catch (IllegalAccessException e)
        {
            fail(e.toString());
        }
    }
}
