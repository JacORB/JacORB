package org.jacorb.test.bugs.bugjac305;

/*
 *        JacORB  - a free Java ORB
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

import org.jacorb.test.bugs.bug401.BHelper;

/**
 * Tests that an derived valuetype has the base type set.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class BugJac305Test extends junit.framework.TestCase
{
    /**
     * <code>test_typecode</code> tests the basetype is set.
     */
    public void test_typecode() throws Exception
    {
        assertNotNull(BHelper.type().concrete_base_type());
    }
}
