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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

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
    final static String IDL = TestUtils.testHome() + "/src/test/idl/compiler/fail";

    @Parameters(name="{index} : {0}")
    public static Collection<Object[]> data()
    {
        List<Object[]> params = new ArrayList<Object[]>();
        File fileNames[] = new File(IDL).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".idl");
            }
        });

        for (File file : fileNames) {
            params.add(new Object[] { file });
        }
        return params;
    }

    public ParseInvalidIDLTest(File file)
    {
        super(file);
    }

    @Test
    public void testParseInvalidIDLFails() throws Exception
    {
        runJacIDL(true);
        // if a test fails the directory
        // will not be deleted. this way
        // the contents can be inspected.
        TestUtils.deleteRecursively(dirGeneration);
    }
}
