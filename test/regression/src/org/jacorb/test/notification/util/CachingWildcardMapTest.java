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

import org.jacorb.notification.util.CachingWildcardMap;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CachingWildcardMapTest.java
 *
 *
 * Created: Sat Apr 12 14:08:13 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class CachingWildcardMapTest extends WildcardMapTest {

    public void setUp() throws Exception {
        map_ = new CachingWildcardMap(4);
    }

    public CachingWildcardMapTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CachingWildcardMapTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

} // CachingWildcardMapTest
