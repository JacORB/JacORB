package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.CollectionsWrapper;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Method;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CollectionsWrapperTest extends TestCase
{

    public void testTime() throws Exception {
        long now = System.currentTimeMillis();

        List[] list = new List[1000];

        for (int x=0; x<list.length; ++x) {
            list[x] = Collections.singletonList("testling");
        }

        //      System.out.println(System.currentTimeMillis() - now);

        Method method = Collections.class.getMethod("singletonList", new Class[] {Object.class});

        now = System.currentTimeMillis();

        for (int x=0; x<list.length; ++x) {
            list[x] = (List)method.invoke(null, new Object[]{"testling"});
        }


    }

    public void testCollectionsWrapper() throws Exception {
        String o = "testling";

        List list = CollectionsWrapper.singletonList(o);

        assertTrue(list.size() == 1);

        assertEquals(o, list.get(0));

        Iterator i = list.iterator();

        while (i.hasNext()) {
            assertEquals(o, i.next());
        }
    }

    /**
     *
     * @param name test name
     */
    public CollectionsWrapperTest (String name)
    {
        super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(CollectionsWrapperTest.class);

        return suite;
    }

    /**
     * Entry point
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());

    }
}
