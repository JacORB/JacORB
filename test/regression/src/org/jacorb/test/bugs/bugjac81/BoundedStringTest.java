package org.jacorb.test.bugs.bugjac81;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import junit.framework.TestCase;

public class BoundedStringTest extends TestCase
{
    public void test1() throws Exception
    {
        assertEquals(80, org.jacorb.test.bugs.bugjac81.BoundErrorHelper.type().content_type().length());
    }

    public void test2() throws Exception
    {
        assertEquals(2, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_count());
    }

    public void test3() throws Exception
    {
        assertEquals("string", org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(0).toString());
    }

    public void test4() throws Exception
    {
        assertEquals(49, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(0).length());
    }

    public void test5() throws Exception
    {
        assertEquals(0, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(1).length());
    }
}
