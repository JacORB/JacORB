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

package org.jacorb.test.bugs.bugjac753;

import org.junit.Assert;
import org.junit.Test;
import bugjac753.A1.a11Helper;
import bugjac753.A2.a21Helper;
import bugjac753.A2.a22Helper;
import bugjac753.A3.a31Helper;
import bugjac753.A3.a32Helper;
import bugjac753.A4.a1Helper;
import bugjac753.A4.A41.a2Helper;

/**
 * @author Alexander Birchenko
 */

public class Bugjac753Test
{
    //bugjac753_1.idl
    @Test
    public void testPragmaPrefixOverride()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A1/a11:1.0", a11Helper.id());
    }

    //bugjac753_2.idl
    @Test
    public void testTypePrefixPlaceIndependent()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A2/a21:1.0", a21Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A2/a22:1.0", a22Helper.id());
    }

    //bugjac753_3.idl
    @Test
    public void testMultipleTypePrefix()
    {
        Assert.assertEquals("IDL:typeprefix.test2/bugjac753/A3/a31:1.0", a31Helper.id());
        Assert.assertEquals("IDL:typeprefix.test2/bugjac753/A3/a32:1.0", a32Helper.id());
    }

    //bugjac753_4.idl
    @Test
    public void testTypePrefixInheritence()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A4/a1:1.0", a1Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A4/A41/a2:1.0", a2Helper.id());
    }

    //bugjac753_5.idl
    @Test
    public void testTypePrefixModuleScopeCheck()
    {
        Assert.assertEquals("IDL:bugjac753/A5/a1:1.0", bugjac753.A5.a1Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A5/A51/a2:1.0", bugjac753.A5.A51.a2Helper.id());
        Assert.assertEquals("IDL:bugjac753/A5/a3:1.0", bugjac753.A5.a3Helper.id());
    }

    //bugjac753_6.idl
    @Test
    public void testTypePrefixReopenedModuleScope1()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A6/a1:1.0", bugjac753.A6.a1Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A6/a2:1.0", bugjac753.A6.a2Helper.id());
    }

    //bugjac753_7.idl
    @Test
    public void testTypePrefixReopenedModuleScope2()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A7/a1:1.0", bugjac753.A7.a1Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A7/a2:1.0", bugjac753.A7.a2Helper.id());
    }

    //bugjac753_8.idl
    @Test
    public void testTypePrefixDuplicatedModulesName()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A8/A81/A8/a1:1.0", bugjac753.A8.A81.A8.a1Helper.id());
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A8/A81/a2:1.0", bugjac753.A8.A81.a2Helper.id());
        Assert.assertEquals("IDL:bugjac753/A8/a3:1.0",  bugjac753.A8.a3Helper.id());
    }

    //bugjac753_9.idl
    @Test
    public void testTypePrefixFullModuleName()
    {
        Assert.assertEquals("IDL:typeprefix.test/bugjac753/A9/a1:1.0", bugjac753.A9.a1Helper.id());
    }
}
