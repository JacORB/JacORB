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
    private final String arg0;

    public ValidIDLWithExtraSetupTest(String arg0, String file)
    {
        super("testMiscParseGood", new File(file));
        this.arg0 = arg0;
    }

    protected String[] createJacIDLArgs()
    {
        String file[] = new String[4];
        file[0] = arg0;
        file[1] = "-d";
        file[2] = TestUtils.testHome() + "/src/generated";
        file[3] = filePath;
        
        return file;
    }
    
    public void testMiscParseGood()
    {
        runJacIDL(false);
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        
        final String testHome = TestUtils.testHome();
        suite.addTest(new ValidIDLWithExtraSetupTest("-DBLUB", testHome + "/idl/compiler/succeed/defined.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + testHome + "/../../idl/omg", testHome + "/idl/compiler/succeed/Interoperability.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-DDefB", testHome + "/idl/compiler/succeed/Ping1.idl"));
        
        return suite;
    }
}
