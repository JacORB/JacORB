/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

/**
 * this test will try to process all idl files included
 * in the directory &lt;TEST_HOME&gt;/idl/compiler/fail.
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
            @Override
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

    public ParseInvalidIDLTest(File file) throws IOException
    {
        super(file);
    }

    @Test
    public void testParseInvalidIDLFails() throws Exception
    {
        runJacIDL(true);
    }
}
