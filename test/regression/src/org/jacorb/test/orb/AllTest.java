package org.jacorb.test.orb;

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
import org.jacorb.test.orb.giop.ClientConnectionTest;
import org.jacorb.test.orb.giop.CodeSetTest;

public class AllTest extends JacORBTestSuite
{
   public AllTest (String name)
   {
      super (name);
   }

   public static Test suite()
    {
        TestSuite suite = new AllTest ("All ORB Tests");

        suite.addTestSuite(org.jacorb.test.orb.ORBInitTest.class);
        suite.addTest (org.jacorb.test.orb.CDRIOTest.suite());
        suite.addTest (org.jacorb.test.orb.BasicTest.suite());
        suite.addTest (org.jacorb.test.orb.CharTest.suite());
        suite.addTest (org.jacorb.test.orb.ArrayTest.suite());
        suite.addTest (org.jacorb.test.orb.ExceptionTest.suite());
        suite.addTest (org.jacorb.test.orb.CorbalocTest.suite());
        suite.addTest (org.jacorb.test.orb.iiop.ClientIIOPConnectionTest.suite() );
        suite.addTest (org.jacorb.test.orb.CallbackTest.suite());
        suite.addTest (org.jacorb.test.orb.value.ValueTest.suite ());
        suite.addTest (org.jacorb.test.orb.rmi.AllTest.suite ());
        suite.addTest (org.jacorb.test.orb.dynany.AllTest.suite ());
        suite.addTest (org.jacorb.test.orb.policies.AllTest.suite());
        suite.addTest (org.jacorb.test.orb.connection.AllTest.suite ());
        suite.addTest (org.jacorb.test.orb.etf.AllTest.suite());
        // long running test
        suite.addTest (org.jacorb.test.orb.AlternateIIOPAddressTest.suite());
        // long running test
        suite.addTest (org.jacorb.test.orb.AlternateIIOPAddress2Test.suite());
        // long running test
        suite.addTest (org.jacorb.test.orb.AlternateProfileTest.suite());
        suite.addTest (org.jacorb.test.orb.LongLongSeq.suite());
        suite.addTest (org.jacorb.test.orb.RecursiveParam.suite());
        suite.addTest( CDRInputStreamTest.suite() );
        suite.addTest( CDROutputStreamTest.suite() );
        suite.addTest( ClientConnectionTest.suite() );
        suite.addTest( CodeSetTest.suite() );
        suite.addTestSuite(CreateTypeCodesTest.class);
        suite.addTestSuite(DIOPIORTest.class);
        suite.addTestSuite(TypeCodeTest.class);
        suite.addTest(AnyTest.suite());
        suite.addTestSuite(InvalidIORTest.class);
        suite.addTest(org.jacorb.test.orb.factory.AllTest.suite());
        suite.addTest (org.jacorb.test.orb.CodesetTest.suite());
        suite.addTestSuite(OAAddressIPv6Test.class);
        suite.addTest (org.jacorb.test.orb.ORBSingletonTest.suite());

        return suite;
    }
}
