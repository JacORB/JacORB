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

package org.jacorb.test.bugs.bug367;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.Properties;
import org.jacorb.test.ir.AbstractIRServerTestCase;
import org.jacorb.test.ir.IFRServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class BugJac367IRTest extends AbstractIRServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.ir.patch_pragma_prefix", "on");

        setup = new IFRServerSetup("irjac367.idl", new String[] {"-i2jpackage", "ir:org.jacorb.test.ir"}, props);
    }

    @Test
    public void testServerStart() throws Exception
    {
        assertFalse(repository._non_existent());
        assertNotNull(repository.lookup_id("IDL:org.jacorb.test/ir/StringAliasList:1.0"));
    }
}
