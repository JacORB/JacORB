package org.jacorb.test.notification.util;

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
import junit.framework.TestSuite;

import org.jacorb.notification.util.CachingWildcardMap;
import org.jacorb.notification.util.WildcardMap;

/**
 * @author Alphonse Bendt
 */

public class CachingWildcardMapTest extends AbstractWildcardMapTestCase
{
    public WildcardMap newWildcardMap()
    {
        return new CachingWildcardMap();
    }

    public static Test suite()
    {
        return new TestSuite(CachingWildcardMapTest.class);
    }
    
    public void testCache()
    {
        objectUnderTest_.put("key1", "value1");
        assertEquals("value1", objectUnderTest_.getWithExpansion("key1")[0]);
        assertEquals("value1", objectUnderTest_.getWithExpansion("key1")[0]);        
    }
}