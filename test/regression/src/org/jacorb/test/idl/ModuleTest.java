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
 * Module.java
 *
 * IDL parse tests.
 *
 */

public class ModuleTest extends TestCase
{
   public ModuleTest (String name)
   {
      super (name);
   }


   public static Test suite ()
   {
      TestSuite suite = new TestSuite ("Module Tests");
      suite.addTest (new ModuleTest ("testModuleParseGood"));

      return suite;
   }


   /**
    */
   public void testModuleParseGood ()
   {
      String file[] = new String[3];
      file[0] = "-d";
      file[1] = ((String)System.getProperty ("testdir")).concat ("/src/generated");
      file[2] = ((String)System.getProperty ("testdir")).concat ("/idl/Module.idl");

      assertTrue ("Compiled Module.idl", org.jacorb.idl.parser.compileAndHandle (file));
   }
}
