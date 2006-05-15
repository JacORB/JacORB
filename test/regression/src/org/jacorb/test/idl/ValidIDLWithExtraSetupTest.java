/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
import junit.framework.TestSuite;

import org.jacorb.test.common.TestUtils;

/**
 * these tests do not fit in the standard scheme
 * of valid/invalid idl tests.
 * to be able to parse the idl files here
 * JacIDL needs more setup. in this
 * case an additional param is needed.
 * therefor the method createJacIDLArgs has
 * been overwritten.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ValidIDLWithExtraSetupTest extends AbstractIDLTestcase
{
    private final String additionalArgument;

    public ValidIDLWithExtraSetupTest(String argument, String file)
    {
        super("testMiscParseGood", new File(file));
        additionalArgument = argument;
    }

    protected String[] createJacIDLArgs()
    {
        String file[] = new String[4];
        file[0] = additionalArgument;
        file[1] = "-d";
        file[2] = dirGeneration.getAbsolutePath();
        file[3] = idlFile.getAbsolutePath();

        return file;
    }

    public void testMiscParseGood() throws Exception
    {
        runJacIDL(false);
        compileGeneratedSources(false);
        deleteRecursively(dirGeneration);
        deleteRecursively(dirCompilation);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        final String testHome = TestUtils.testHome();
        suite.addTest(new ValidIDLWithExtraSetupTest("-DBLUB", testHome + "/idl/compiler/misc/defined.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + testHome + "/../../idl/omg", testHome + "/idl/compiler/misc/Interoperability.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + testHome + "/../../idl/omg", testHome + "/idl/compiler/misc/rt1051.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-DDefB", testHome + "/idl/compiler/misc/Ping1.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-ami_callback", testHome + "/idl/compiler/misc/ami.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-ami_callback", testHome + "/idl/compiler/misc/rt1180.idl"));

        return suite;
    }
}
