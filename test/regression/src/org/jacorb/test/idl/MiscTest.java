package org.jacorb.test.idl;

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

import junit.framework.*;
import org.jacorb.test.common.*;

/**
 * MiscTest.java
 *
 * Various IDL moved from test directory.
 *
 */

public class MiscTest extends TestCase
{
    public MiscTest (String name)
    {
        super (name);
    }


    public static Test suite ()
    {
        TestSuite suite = new TestSuite ("Misc Tests");

        suite.addTest (new MiscTest ("testMiscParseGood1"));
        suite.addTest (new MiscTest ("testMiscParseGood2"));
        suite.addTest (new MiscTest ("testMiscParseGood3"));
        suite.addTest (new MiscTest ("testMiscParseGood4"));
        suite.addTest (new MiscTest ("testMiscParseGood5"));
        suite.addTest (new MiscTest ("testMiscParseGood6"));
        suite.addTest (new MiscTest ("testMiscParseGood7"));
        suite.addTest (new MiscTest ("testMiscParseGood8"));
        suite.addTest (new MiscTest ("testMiscParseGood9"));
        suite.addTest (new MiscTest ("testMiscParseGood10"));
        suite.addTest (new MiscTest ("testMiscParseGood12"));
        suite.addTest (new MiscTest ("testMiscParseGood13"));
        suite.addTest (new MiscTest ("testMiscParseGood14"));
        suite.addTest (new MiscTest ("testMiscParseGood15"));
        suite.addTest (new MiscTest ("testMiscParseGood16"));
        suite.addTest (new MiscTest ("testMiscParseGood17"));
        suite.addTest (new MiscTest ("testMiscParseGood18"));
        suite.addTest (new MiscTest ("testMiscParseGood19"));
        suite.addTest (new MiscTest ("testMiscParseGood20"));

        return suite;
    }


    public void testMiscParseGood1 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/basetypes.idl";

        assertTrue ("Compiled basetypes.idl", org.jacorb.idl.parser.compileAndHandle  (file));
    }


    public void testMiscParseGood2 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/inherit.idl";

        assertTrue ("Compiled inherit.idl", org.jacorb.idl.parser.compileAndHandle (file));
    }



    public void testMiscParseGood3 ()
    {
        String file[] = new String[4];
        file[0] = "-DBLUB";
        file[1] = "-d";
        file[2] = TestUtils.testHome() + "/src/generated";
        file[3] = TestUtils.testHome() + "/idl/compiler/succeed/defined.idl";

        assertTrue ("Compiled defined.idl", org.jacorb.idl.parser.compileAndHandle (file));
    }


    public void testMiscParseGood4 ()
    {
        String file[] = new String[4];
        file[0] = "-I" + TestUtils.testHome() + "/../../idl/omg";
        file[1] = "-d";
        file[2] = TestUtils.testHome() + "/src/generated";
        file[3] = TestUtils.testHome() + "/idl/compiler/succeed/Interoperability.idl";
        assertTrue ("Compiled Interoperability.idl", org.jacorb.idl.parser.compileAndHandle (file));
    }


    public void testMiscParseGood5 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/reopen.idl";
        assertTrue ("Compiled reopen.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


    public void testMiscParseGood6 ()
    {
        String file[] = new String[4];
        file[0] = "-all";
        file[1] = "-d";
        file[2] = TestUtils.testHome() + "/src/generated";
        file[3] = TestUtils.testHome() + "/idl/compiler/succeed/include.idl";
        assertTrue ("Compiled include.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


    public void testMiscParseGood7 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/names.idl";
        assertTrue ("Compiled names.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }

    public void testMiscParseGood8 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/raises.idl";
        assertTrue ("Compiled raise.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }

    public void testMiscParseGood9 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scope.idl";
        assertTrue ("Compiled scope.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


    public void testMiscParseGood10 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/typedefdStruct.idl";
        assertTrue ("Compiled ex1.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


    public void testMiscParseGood12 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/reservedJavaWordRename.idl";
        assertTrue ("Compiled reservedJavaWordRename.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


    public void testMiscParseGood13 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping1.idl";
        assertTrue ("Compiled scoping1.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }

    public void testMiscParseGood14 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping2.idl";
        assertTrue ("Compiled scoping2.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }
    public void testMiscParseGood15 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping3.idl";
        assertTrue ("Compiled scoping3.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }
    public void testMiscParseGood16 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping4.idl";
        assertTrue ("Compiled scoping4.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }
    public void testMiscParseGood17 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping8.idl";
        assertTrue ("Compiled scoping8.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }
    public void testMiscParseGood18 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/scoping10.idl";
        assertTrue ("Compiled scoping10.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }

    public void testMiscParseGood19 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = TestUtils.testHome() + "/idl/compiler/succeed/ReservedWord.idl";
        assertTrue ("Compiled ReservedWord.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }

    public void testMiscParseGood20 ()
    {
        String file[] = new String[4];
        file[0] = "-DDefB";
        file[1] = "-d";
        file[2] = TestUtils.testHome() + "/src/generated";
        file[3] = TestUtils.testHome() + "/idl/compiler/succeed/Ping1.idl";
        assertTrue ("Compiled Ping1.idl", org.jacorb.idl.parser.compileAndHandle(file));
    }


}
