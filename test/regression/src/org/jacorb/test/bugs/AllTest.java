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

import org.jacorb.test.bugs.bug380.BugJac380Test;
import org.jacorb.test.bugs.bug400.Bug400Test;
import org.jacorb.test.bugs.bug735.Bug735Test;
import org.jacorb.test.bugs.bugjac10.BugJac10Test;
import org.jacorb.test.bugs.bugjac149.ObjectReplacementTest;
import org.jacorb.test.bugs.bugjac174.BugJac174Test;
import org.jacorb.test.bugs.bugjac178.POAThreadingTest;
import org.jacorb.test.bugs.bugjac181.BugJac181Test;
import org.jacorb.test.bugs.bugjac182.BugJac182Test;
import org.jacorb.test.bugs.bugjac189.BugJac189Test;
import org.jacorb.test.bugs.bugjac192.BugJac192Test;
import org.jacorb.test.bugs.bugjac195.BugJac195Test;
import org.jacorb.test.bugs.bugjac200.AcceptorExceptionListenerTest;
import org.jacorb.test.bugs.bugjac220.BugJac220Test;
import org.jacorb.test.bugs.bugjac235.BugJac235Suite;
import org.jacorb.test.bugs.bugjac251.BugJac251Test;
import org.jacorb.test.bugs.bugjac257.BugJac257Test;
import org.jacorb.test.bugs.bugjac262.BugJac262Test;
import org.jacorb.test.bugs.bugjac294.BugJac294Test;
import org.jacorb.test.bugs.bugjac303.BugJac303Test;
import org.jacorb.test.bugs.bugjac305.BugJac305Test;
import org.jacorb.test.bugs.bugjac319.BugJac319AbstractTest;
import org.jacorb.test.bugs.bugjac359.BugJac359Test;
import org.jacorb.test.bugs.bugjac384.BugJac_384Test;
import org.jacorb.test.bugs.bugjac352.BugJac352Test;
import org.jacorb.test.bugs.bugjac415.BugJac415Test;
import org.jacorb.test.bugs.bugjac443.BugJac443Test;
import org.jacorb.test.bugs.bugjac45.BugJac45Test;
import org.jacorb.test.bugs.bugjac69.InvalidIORTest;
import org.jacorb.test.bugs.bugjac722.BugJac722Test;
import org.jacorb.test.bugs.bugjac81.BoundedStringTest;
import org.jacorb.test.bugs.bug367.BugJac367IRTest;
import org.jacorb.test.bugs.bugjac516.BugJac516Test;
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

      suite.addTestSuite(org.jacorb.test.bugs.bug228.Bug228Test.class);
      suite.addTestSuite(org.jacorb.test.bugs.bug272.Bug272Test.class);
      suite.addTestSuite(org.jacorb.test.bugs.bug344.Bug344Test.class);
      suite.addTest(org.jacorb.test.bugs.bug351.Bug351Test.suite());
      suite.addTest(org.jacorb.test.bugs.bug384.Bug384Test.suite());
      suite.addTest(org.jacorb.test.bugs.bug387.Bug387Test.suite());
      suite.addTest(org.jacorb.test.bugs.bug401.Bug401Test.suite());
      suite.addTestSuite(org.jacorb.test.bugs.bug459.Bug459Test.class);
      suite.addTestSuite(org.jacorb.test.bugs.bug532.Bug532Test.class);
      suite.addTest(org.jacorb.test.bugs.bug619.Bug619Test.suite());
      suite.addTest(BugJac10Test.suite());
      suite.addTestSuite(InvalidIORTest.class);
      suite.addTestSuite(BoundedStringTest.class);
      suite.addTestSuite(BugJac45Test.class);
      suite.addTestSuite(BugJac303Test.class);
      suite.addTest(ObjectReplacementTest.suite());
      suite.addTest(BugJac182Test.suite());
      suite.addTest(POAThreadingTest.suite());
      suite.addTest(BugJac235Suite.suite());
      suite.addTest(BugJac192Test.suite());
      suite.addTestSuite(BugJac174Test.class);
      suite.addTest(BugJac220Test.suite());
      suite.addTestSuite(BugJac305Test.class);
      suite.addTest(BugJac181Test.suite());
      suite.addTest(BugJac189Test.suite());
      suite.addTest(BugJac195Test.suite());
      suite.addTest(AcceptorExceptionListenerTest.suite());
      suite.addTest(BugJac251Test.suite());
      suite.addTest(BugJac257Test.suite());
      suite.addTest(BugJac262Test.suite());
      suite.addTestSuite(BugJac294Test.class);
      suite.addTest(BugJac319AbstractTest.suite());
      suite.addTest(org.jacorb.test.bugs.bugjac330.BugJac330Suite.suite());
      suite.addTestSuite(Bug400Test.class);
      suite.addTest(BugJac722Test.suite());
      suite.addTest(org.jacorb.test.bugs.bugcos370.BugCos370Test.suite());
      suite.addTestSuite(Bug735Test.class);
      suite.addTestSuite(BugJac443Test.class);
      suite.addTest(BugJac367IRTest.suite());
      suite.addTestSuite(BugJac516Test.class);
      suite.addTest(BugJac352Test.suite());
      suite.addTest(BugJac359Test.suite());
      suite.addTestSuite(BugJac_384Test.class);
      suite.addTestSuite(BugJac380Test.class);
      suite.addTestSuite(BugJac415Test.class);
      suite.addTestSuite(org.jacorb.test.bugs.JBPAPP9891.MarshallingTest.class);

      return suite;
   }
}
