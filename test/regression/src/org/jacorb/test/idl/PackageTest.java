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

public class PackageTest extends TestCase
{
   public PackageTest (String name)
   {
      super (name);
   }

   public static Test suite ()
   {
      TestSuite suite = new TestSuite ("Package idl");

      suite.addTest (LongTest.suite ());
      suite.addTest (FloatTest.suite ());
      suite.addTest (PragmaTest.suite ());
      suite.addTest (ModuleTest.suite ());
      suite.addTest (CharTest.suite ());
      suite.addTest (TypeDefTest.suite ());
      suite.addTest (InterfaceTest.suite ());
      suite.addTest (UnionTest.suite ());
      suite.addTest (ValueTest.suite ());
      suite.addTest (MiscTest.suite ());
      suite.addTest (MiscFailureTest.suite ());

      return suite;
   }
}
