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
import junit.extensions.TestSetup;
import org.jacorb.idl.ParseException;


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
        suite.addTest (new MiscTest ("testMiscParseFail1"));
        suite.addTest (new MiscTest ("testMiscParseGood3"));
        suite.addTest (new MiscTest ("testMiscParseGood4"));
        suite.addTest (new MiscTest ("testMiscParseGood5"));
        suite.addTest (new MiscTest ("testMiscParseGood6"));
        suite.addTest (new MiscTest ("testMiscParseGood7"));
        suite.addTest (new MiscTest ("testMiscParseGood8"));
        suite.addTest (new MiscTest ("testMiscParseGood9"));
        suite.addTest (new MiscTest ("testMiscParseGood10"));
        suite.addTest (new MiscTest ("testMiscParseFail2"));
        suite.addTest (new MiscTest ("testMiscParseFail3"));
        suite.addTest (new MiscTest ("testMiscParseFail4"));
        suite.addTest (new MiscTest ("testMiscParseFail5"));
        suite.addTest (new MiscTest ("testMiscParseFail6"));
        suite.addTest (new MiscTest ("testMiscParseFail6"));
        suite.addTest (new MiscTest ("testMiscParseFail7"));
        suite.addTest (new MiscTest ("testMiscParseFail8"));
        suite.addTest (new MiscTest ("testMiscParseFail9"));
        suite.addTest (new MiscTest ("testMiscParseFail10"));
        suite.addTest (new MiscTest ("testMiscParseFail11"));
        suite.addTest (new MiscTest ("testMiscParseFail12"));
        suite.addTest (new MiscTest ("testMiscParseFail13"));
        suite.addTest (new MiscTest ("testMiscParseFail14"));
        suite.addTest (new MiscTest ("testMiscParseFail15"));
        suite.addTest (new MiscTest ("testMiscParseFail16"));
        suite.addTest (new MiscTest ("testMiscParseFail17"));
        suite.addTest (new MiscTest ("testMiscParseFail18"));
        suite.addTest (new MiscTest ("testMiscParseFail19"));
        suite.addTest (new MiscTest ("testMiscParseGood11"));
        suite.addTest (new MiscTest ("testMiscParseGood12"));
        suite.addTest (new MiscTest ("testMiscParseGood13"));
        suite.addTest (new MiscTest ("testMiscParseGood14"));
        suite.addTest (new MiscTest ("testMiscParseGood15"));
        suite.addTest (new MiscTest ("testMiscParseGood16"));
        suite.addTest (new MiscTest ("testMiscParseGood17"));
        suite.addTest (new MiscTest ("testMiscParseGood18"));
        suite.addTest (new MiscTest ("testMiscParseGood19"));
        suite.addTest (new MiscTest ("testMiscParseGood20"));
        suite.addTest (new MiscTest ("testMiscParseFail20"));

        return suite;
    }


    public void testMiscParseGood1 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/basetypes.idl");

        assertTrue ("Compiled basetypes.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood2 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/inherit.idl");

        assertTrue ("Compiled inherit.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseFail1 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/defined.idl");

        assertTrue("Compiled defined.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseGood3 ()
    {
        String file[] = new String[4];
        file[0] = "-DBLUB";
        file[1] = "-d";
        file[2] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[3] = ((String)System.getProperty ("testdir")).concat ("/idl/defined.idl");

        assertTrue ("Compiled defined.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood4 ()
    {
        String file[] = new String[4];
        file[0] = "-I" + ((String)System.getProperty ("testdir") ) + "/../../idl/omg";
        file[1] = "-d";
        file[2] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[3] = ((String)System.getProperty ("testdir")).concat ("/idl/Interoperability.idl");
        assertTrue ("Compiled Interoperability.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood5 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/reopen.idl");
        assertTrue ("Compiled reopen.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood6 ()
    {
        String file[] = new String[4];
        file[0] = "-all";
        file[1] = "-d";
        file[2] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[3] = ((String)System.getProperty ("testdir")).concat ("/idl/include.idl");
        assertTrue ("Compiled include.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood7 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/names.idl");
        assertTrue ("Compiled names.idl", org.jacorb.idl.parser.compile (file));
    }

    public void testMiscParseGood8 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/raises.idl");
        assertTrue ("Compiled raise.idl", org.jacorb.idl.parser.compile (file));
    }

    public void testMiscParseGood9 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scope.idl");
        assertTrue ("Compiled scope.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood10 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/ex1.idl");
        assertTrue ("Compiled ex1.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseFail2 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badenum1.idl");
        assertTrue("Compiled badenum1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail3 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badex1.idl");
        assertTrue("Compiled badex1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail4 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badex2.idl");
        assertTrue("Compiled badex2.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail5 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badintf1.idl");
        assertTrue("Compiled badintf1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail6 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop1.idl");
        assertTrue("Compiled badop1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail7 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop2.idl");
        assertTrue("Compiled badop2.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail8 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop3.idl");
        assertTrue("Compiled badop3.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
   }


    public void testMiscParseFail9 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop4.idl");
        assertTrue("Compiled badop4.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail10 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop5.idl");
        assertTrue("Compiled badop5.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail11 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop6.idl");
        assertTrue("Compiled badop6.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail12 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop7.idl");
        assertTrue("Compiled badop7.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail13 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badop8.idl");
        assertTrue("Compiled badop8.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail14 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badraises1.idl");
        assertTrue("Compiled badraises1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail15 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badstruct.idl");
        assertTrue("Compiled badstruct.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail16 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/badstruct2.idl");
        assertTrue("Compiled badstruct2.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail17 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/forward1.idl");
        assertTrue ("Compiled forward1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail18 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/inherit1.idl");
        assertTrue ("Compiled inherit1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseFail19 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/inherit2.idl");
        assertTrue ("Compiled inherit2.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }


    public void testMiscParseGood11 ()
    {
        String file[] = new String[4];
        file[0] = "-sloppy_forward";
        file[1] = "-d";
        file[2] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[3] = ((String)System.getProperty ("testdir")).concat ("/idl/forward1.idl");
        assertTrue ("Compiled forward1.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood12 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/resword_op.idl");
        assertTrue ("Compiled resword_op.idl", org.jacorb.idl.parser.compile (file));
    }


    public void testMiscParseGood13 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping1.idl");
        assertTrue ("Compiled scoping1.idl", org.jacorb.idl.parser.compile (file));
    }

    public void testMiscParseGood14 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping2.idl");
        assertTrue ("Compiled scoping2.idl", org.jacorb.idl.parser.compile (file));
    }
    public void testMiscParseGood15 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping3.idl");
        assertTrue ("Compiled scoping3.idl", org.jacorb.idl.parser.compile (file));
    }
    public void testMiscParseGood16 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping4.idl");
        assertTrue ("Compiled scoping4.idl", org.jacorb.idl.parser.compile (file));
    }
    public void testMiscParseGood17 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping8.idl");
        assertTrue ("Compiled scoping8.idl", org.jacorb.idl.parser.compile (file));
    }
    public void testMiscParseGood18 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/scoping10.idl");
        assertTrue ("Compiled scoping10.idl", org.jacorb.idl.parser.compile (file));
    }
    public void testMiscParseGood19 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/ReservedWord.idl");
        assertTrue ("Compiled ReservedWord.idl", org.jacorb.idl.parser.compile (file));
    }

    public void testMiscParseGood20 ()
    {
        String file[] = new String[4];
        file[0] = "-DDefB";
        file[1] = "-d";
        file[2] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[3] = ((String)System.getProperty ("testdir")).concat ("/idl/Ping1.idl");
        assertTrue ("Compiled Ping1.idl", org.jacorb.idl.parser.compile (file));
    }

    public void testMiscParseFail20 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/Ping1.idl");
        assertTrue ("Compiled Ping1.idl (for failure)", org.jacorb.idl.parser.compile (file)==false);
    }
}
