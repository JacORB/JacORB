/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.notification.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jacorb.notification.util.WeakHashSet;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class WeakHashSetTest
{
    private Set objectUnderTest_;

    /*
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = new WeakHashSet();
    }

    @Test
    public void testSize()
    {
        assertEquals(0, objectUnderTest_.size());
        Object o1 = new Object();
        objectUnderTest_.add(o1);
        assertEquals(1, objectUnderTest_.size());
    }

    @Test
    public void testIsEmpty()
    {
        assertTrue(objectUnderTest_.isEmpty());
        Object o1 = new Object();
        objectUnderTest_.add(o1);
        assertFalse(objectUnderTest_.isEmpty());
    }

    @Test
    public void testContains()
    {
        Object o1 = new Object();
        assertFalse(objectUnderTest_.contains(o1));
        objectUnderTest_.add(o1);
        assertTrue(objectUnderTest_.contains(o1));
    }

    @Test
    public void testIterator()
    {
        Iterator i = objectUnderTest_.iterator();
        assertFalse(i.hasNext());

        Object o1 = new Object();
        Object o2 = new Object();
        objectUnderTest_.add(o1);
        objectUnderTest_.add(o2);

        i = objectUnderTest_.iterator();
        assertTrue(i.hasNext());
        assertNotNull(i.next());
        assertNotNull(i.next());
        assertFalse(i.hasNext());
    }


    @Test
    public void testToArray()
    {
        Object[] array = objectUnderTest_.toArray();

        assertEquals(0, array.length);

        Object o1 = new Object();
        objectUnderTest_.add(o1);

        array = objectUnderTest_.toArray();

        assertEquals(1, array.length);
        assertEquals(o1, array[0]);
    }


    @Test
    public void testToArrayObjectArray()
    {
        String s1 = "text";

        objectUnderTest_.add(s1);

        String[] array = (String[]) objectUnderTest_.toArray(new String[0]);
        assertEquals(1, array.length);
        assertEquals("text", array[0]);
    }

    @Test
    public void testAdd()
    {
        Object o1 = new Object();

        assertTrue(objectUnderTest_.add(o1));
        assertFalse(objectUnderTest_.add(o1));
    }

    @Test
    public void testRemove()
    {
        Object o1 = new Object();

        objectUnderTest_.add(o1);
        assertTrue(objectUnderTest_.contains(o1));
        assertTrue(objectUnderTest_.remove(o1));
        assertFalse(objectUnderTest_.remove(o1));
        assertFalse(objectUnderTest_.contains(o1));
    }

    @Test
    public void testContainsAll()
    {
        Object o1 = new Object();
        Object o2 = new Object();

        List all = Arrays.asList(new Object[] { o1, o2 });

        assertFalse(objectUnderTest_.containsAll(all));

        objectUnderTest_.add(o1);

        assertFalse(objectUnderTest_.containsAll(all));

        objectUnderTest_.add(o2);

        assertTrue(objectUnderTest_.containsAll(all));
    }

    @Test
    public void testAddAll()
    {
        Object o1 = new Object();
        Object o2 = new Object();

        List all = Arrays.asList(new Object[] { o1, o2 });

        assertFalse(objectUnderTest_.contains(o1));
        assertFalse(objectUnderTest_.contains(o2));

        assertTrue(objectUnderTest_.addAll(all));
        assertFalse(objectUnderTest_.addAll(all));

        assertTrue(objectUnderTest_.contains(o1));
        assertTrue(objectUnderTest_.contains(o2));
    }

    @Test
    public void testRetainAll()
    {
        Object o1 = new Object();
        Object o2 = new Object();

        List all = Arrays.asList(new Object[] { o1 });

        objectUnderTest_.add(o1);
        objectUnderTest_.add(o2);

        assertTrue(objectUnderTest_.contains(o1));
        assertTrue(objectUnderTest_.contains(o2));

        assertTrue(objectUnderTest_.retainAll(all));
        assertFalse(objectUnderTest_.retainAll(all));

        assertTrue(objectUnderTest_.contains(o1));
        assertFalse(objectUnderTest_.contains(o2));
    }

    @Test
    public void testRemoveAll()
    {
        Object o1 = new Object();
        Object o2 = new Object();

        List all = Arrays.asList(new Object[] { o1 });

        objectUnderTest_.add(o1);
        objectUnderTest_.add(o2);

        assertTrue(objectUnderTest_.contains(o1));
        assertTrue(objectUnderTest_.contains(o2));

        assertTrue(objectUnderTest_.removeAll(all));
        assertFalse(objectUnderTest_.removeAll(all));

        assertFalse(objectUnderTest_.contains(o1));
        assertTrue(objectUnderTest_.contains(o2));
    }

    @Test
    public void testClear()
    {
        Object o1 = new Object();

        assertTrue(objectUnderTest_.isEmpty());

        objectUnderTest_.add(o1);

        assertFalse(objectUnderTest_.isEmpty());

        objectUnderTest_.clear();

        assertTrue(objectUnderTest_.isEmpty());
    }

    @Test
    public void testIsReallyWeak()
    {
        long timeout = System.currentTimeMillis() + 10000;

        Object o1 = new Object();
        ReferenceQueue queue = new ReferenceQueue();

        new WeakReference(o1, queue);

        objectUnderTest_.add(o1);

        o1 = null;

        while (queue.poll() == null && System.currentTimeMillis() < timeout)
        {
            System.gc();
        }

        assertTrue(objectUnderTest_.isEmpty());
    }
}
