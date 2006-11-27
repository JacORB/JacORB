package org.jacorb.test;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2005  Gerald Brose.
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

public class AllTest extends JacORBTestSuite
{
   public AllTest (String name)
   {
      super (name);
   }

   public static Test suite () throws Exception
   {
      TestSuite suite = new AllTest ("All jacorb");

      String services = System.getProperty("EXCLUDE_SERVICES", "");

      suite.addTest(org.jacorb.test.idl.AllTest.suite());
      suite.addTest(org.jacorb.test.orb.AllTest.suite());
      suite.addTest(org.jacorb.test.poa.AllTest.suite());
      if ( ! "true".equalsIgnoreCase(services))
      {
          suite.addTest(org.jacorb.test.naming.AllTest.suite());
          suite.addTest(org.jacorb.test.notification.AllTest.suite());
      }
      suite.addTest(org.jacorb.test.bugs.AllTest.suite());
      suite.addTest (org.jacorb.test.util.AllTest.suite());
      suite.addTest(org.jacorb.test.dii.AllTest.suite());

      suite.addTest(org.jacorb.test.transport.AllTest.suite());
      suite.addTest(org.jacorb.test.ir.AllTest.suite());

      return suite;
   }
}
