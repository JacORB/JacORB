/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.notification.util.AbstractObjectPool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alphonse Bendt
 */

public class ObjectPoolTest extends TestCase
{
    public ObjectPoolTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(ObjectPoolTest.class);

        return suite;
    }

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
