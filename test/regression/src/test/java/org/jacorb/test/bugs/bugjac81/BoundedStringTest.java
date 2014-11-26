package org.jacorb.test.bugs.bugjac81;

/*
 *        JacORB  - a free Java ORB
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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.omg.CORBA.TCKind;

public class BoundedStringTest
{
    @Test
    public void test1() throws Exception
    {
        assertEquals(80, org.jacorb.test.bugs.bugjac81.BoundErrorHelper.type().content_type().length());
    }

    @Test
    public void test2() throws Exception
    {
        assertEquals(2, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_count());
    }

    @Test
    public void test3() throws Exception
    {
        assertEquals(TCKind.tk_string, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(0).kind());
    }

    @Test
    public void test4() throws Exception
    {
        assertEquals(49, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(0).length());
    }

    @Test
    public void test5() throws Exception
    {
        assertEquals(0, org.jacorb.test.bugs.bugjac81.StructOneHelper.type().member_type(1).length());
    }
}
