package org.jacorb.test.bugs;

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

import org.jacorb.test.bugs.bugjac10.TypeCodeTestCase;
import org.jacorb.test.bugs.bugjac45.BugJac45Test;
import org.jacorb.test.bugs.bugjac69.InvalidIORTest;
import org.jacorb.test.bugs.bugjac81.BoundedStringTest;
import org.jacorb.test.common.*;

/**
 * Test suite for all bug tests.
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */

public class AllTest extends JacORBTestSuite
{
   public AllTest(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      TestSuite suite = new AllTest("All bugs");

      suite.addTest(org.jacorb.test.bugs.bug228.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug272.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug344.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug351.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug384.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug387.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug401.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug532.TestCase.suite());
      suite.addTest(org.jacorb.test.bugs.bug619.TestCase.suite());
      suite.addTest(TypeCodeTestCase.suite());
      suite.addTestSuite(InvalidIORTest.class);
      suite.addTestSuite(BoundedStringTest.class);
      suite.addTestSuite(BugJac45Test.class);

      return suite;
   }
}

