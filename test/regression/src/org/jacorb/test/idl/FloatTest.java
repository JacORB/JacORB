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
 * FloatTest.java
 *
 * IDL parse tests.
 *
 */

public class FloatTest
    extends TestCase
{
    private static  String outdir =
    ((String)System.getProperty ("testdir")).concat("/src/generated");

    private static  String testdir =
    ((String)System.getProperty ("testdir")).concat("/idl/compiler/");


    public FloatTest (String name)
    {
        super( name );
    }


   public static Test suite ()
   {
      TestSuite suite = new TestSuite("Float Tests");
      suite.addTest( new FloatTest("testFloatParseGood"));
      suite.addTest( new FloatTest("testFloatParseFail1"));
      suite.addTest( new FloatTest("testFloatParseFail2"));
      return suite;
   }


   /**
    */
   public void testFloatParseGood ()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = outdir;
      file[2] = testdir.concat( "succeed/Floats.idl");

      assertTrue( "Compiled succeed/Floats.idl",
                  org.jacorb.idl.parser.compileAndHandle( file ) );
   }


   /**
    */
   public void testFloatParseFail1()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = outdir;
      file[2] = testdir.concat("fail/Floats.idl");

      assertFalse( "Compiled fail/Floats.idl",
                   org.jacorb.idl.parser.compileAndHandle(file) );
   }


   /**
    */
   public void testFloatParseFail2()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = outdir;
      file[2] = testdir.concat ("fail/Floats.idl");

      assertFalse( "Compiled fail/Floats.idl",
                   org.jacorb.idl.parser.compileAndHandle(file) );
   }
}
