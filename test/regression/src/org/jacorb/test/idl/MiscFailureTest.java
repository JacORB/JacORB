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

public class MiscFailureTest extends TestCase
{
    private static  String outdir = 
    ((String)System.getProperty ("testdir")).concat("/src/generated");

    private static  String idldir = 
    ((String)System.getProperty ("testdir")).concat("/idl/compiler/fail/");

    public MiscFailureTest (String name)
    {
        super (name);
    }


    public static Test suite ()
    {
        TestSuite suite = new TestSuite ("Misc Failure Tests");

        suite.addTest (new MiscFailureTest ("testMiscParseFail1"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail2"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail3"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail4"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail5"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail6"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail6"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail7"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail8"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail9"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail10"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail11"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail12"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail13"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail14"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail15"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail16"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail17"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail18"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail19"));
        suite.addTest (new MiscFailureTest ("testMiscParseFail20"));

        return suite;
    }


    public void testMiscParseFail1 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = idldir.concat("defined.idl");

        assertFalse( "Compiled defined.idl (for failure)", 
                     org.jacorb.idl.parser.compileAndHandle(file) );

    }


    public void testMiscParseFail2 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badenum1.idl");
        assertTrue("Compiled badenum1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail3 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badex1.idl");
        assertTrue("Compiled badex1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail4 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badex2.idl");
        assertTrue("Compiled badex2.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail5 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badintf1.idl");
        assertTrue("Compiled badintf1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail6 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop1.idl");
        assertTrue("Compiled badop1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail7 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop2.idl");
        assertTrue("Compiled badop2.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail8 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop3.idl");
        assertTrue("Compiled badop3.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
   }


    public void testMiscParseFail9 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop4.idl");
        assertTrue("Compiled badop4.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail10 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop5.idl");
        assertTrue("Compiled badop5.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail11 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop6.idl");
        assertTrue("Compiled badop6.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail12 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop7.idl");
        assertTrue("Compiled badop7.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail13 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badop8.idl");
        assertTrue("Compiled badop8.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail14 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badraises1.idl");
        assertTrue("Compiled badraises1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail15 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badstruct.idl");
        assertTrue("Compiled badstruct.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail16 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/badstruct2.idl");
        assertTrue("Compiled badstruct2.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail17 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/forwardUndefined.idl");
        assertTrue ("Compiled forward1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail18 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/interfaceInheritsFromStruct.idl");
        assertTrue ("Compiled inherit1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }


    public void testMiscParseFail19 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/fail/interfaceInheritsFromUndefined.idl");
        assertTrue ("Compiled inherit2.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }

    public void testMiscParseFail20 ()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = outdir;
        file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/compiler/succeed/Ping1.idl");
        assertTrue ("Compiled Ping1.idl (for failure)", org.jacorb.idl.parser.compileAndHandle(file)==false);
    }
}
