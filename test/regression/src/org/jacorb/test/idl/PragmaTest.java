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
 * Pragma.java
 *
 * IDL parse #pragma tests.
 *
 */

public class PragmaTest extends TestCase
{
   public PragmaTest (String name)
   {
      super (name);
   }


   public static Test suite ()
   {
      TestSuite suite = new TestSuite ("Pragma Tests");
      suite.addTest (new PragmaTest ("testPragmaParseGood"));
      suite.addTest (new PragmaTest ("testPragmaParseFail1"));
      suite.addTest (new PragmaTest ("testPragmaParseFail2"));

      return suite;
   }


   /**
    */
   public void testPragmaParseGood ()
   {
      StringBuffer command = new StringBuffer ();
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
      file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/Pragma.idl");

      assertTrue ("Compiled Pragma.idl", org.jacorb.idl.parser.compile (file));
   }


   public void testPragmaParseFail1 ()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
      file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/Pragma_Fail1.idl");

      assertTrue("Compiled Pragma_Fail1.idl", org.jacorb.idl.parser.compile (file)==false);
   }


   public void testPragmaParseFail2 ()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
      file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/Pragma_Fail2.idl");

      assertTrue("Compiled Pragma_Fail2.idl", org.jacorb.idl.parser.compile (file)==false);
   }
}
