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

package org.jacorb.test.idl;

import java.io.File;
import junit.framework.Test;
import org.jacorb.test.common.TestUtils;

/**
 * this test will try to process all idl files included
 * in the directory <TEST_HOME>/idl/compiler/fail.
 * this test assumes the idl files to be invalid and
 * will fail if JacIDL does not cause an error during processing.
 *
 * @author Alphonse Bendt
 */
public class ParseInvalidIDLTest extends AbstractIDLTestcase
{
    public ParseInvalidIDLTest(File file)
    {
        super("testParseInvalidIDLFails", file);
    }

    public void testParseInvalidIDLFails() throws Exception
    {
        runJacIDL(true, false);
        // if a test fails the directory
        // will not be deleted. this way
        // the contents can be inspected.
        TestUtils.deleteRecursively(dirGeneration);
    }

    public static Test suite()
    {
        final String dir = TestUtils.testHome() + "/idl/compiler/fail";
        return suite(dir, ParseInvalidIDLTest.class);
    }
}
