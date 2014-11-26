/*
 *        JacORB - a free Java ORB
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

package org.jacorb.test.notification.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.jacorb.notification.util.AbstractObjectPool;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */

public class ObjectPoolTest
{

    @Test
    public void testGetReturnsUniqueObject() throws Exception
    {
        final AtomicInteger counter = new AtomicInteger(0);

        int testSize = 10;

        AbstractObjectPool pool = new AbstractObjectPool("Test", testSize, 1, 2, 20, 0)
        {
            public Object newInstance()
            {
                return new Integer(counter.incrementAndGet());
            }
        };

        pool.configure(null);
        
        HashSet unique = new HashSet();

        for (int x = 0; x < testSize * 2; ++x)
        {
            Integer i = (Integer) pool.lendObject();
            assertFalse(unique.contains(i));
            unique.add(i);
        }
    }

    @Test
    public void testMaximumElements() throws Exception
    {
        final AtomicInteger counter = new AtomicInteger(0);

        AbstractObjectPool pool = new AbstractObjectPool("Test", 0, 0, 0, 0, 1)
        {
            public Object newInstance()
            {
                return new Integer(counter.incrementAndGet());
            }
        };
        
        pool.configure(null);

        Object lended = pool.lendObject();

        try
        {
            pool.lendObject();
            fail();
        } catch (RuntimeException e)
        {
            // expected
        }

        try
        {
            pool.returnObject(new Integer(10));
            fail();
        } catch (RuntimeException e)
        {
            // expected
        }

        try
        {
            pool.lendObject();
            fail();
        } catch (RuntimeException e)
        {
            // expected
        }

        pool.returnObject(lended);
        
        pool.lendObject();
    }
}
