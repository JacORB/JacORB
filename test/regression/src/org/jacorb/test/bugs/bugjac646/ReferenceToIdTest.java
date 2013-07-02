package org.jacorb.test.bugs.bugjac646;

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

import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.WrongAdapter;

/**
 * <code>ReferenceToIdTest</code> tests that reference_to_id throws the correct
 * exceptions.
 *
 * @author Nick Cross
 * @version 1.0
 */
public class ReferenceToIdTest extends ORBTestCase
{
    public void testReferenceToId1 () throws Exception
    {
        org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[1];
        policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

        POA poa = rootPOA.create_POA("ChildOne", rootPOA.the_POAManager(),policies);
        BasicServerImpl impl = new BasicServerImpl();


        org.omg.CORBA.Object obj = rootPOA.servant_to_reference (impl);
        rootPOA.reference_to_id(obj);
        try
        {
            poa.reference_to_id(obj);

            fail ("Should have thrown wrong adapter");
        }
        catch (WrongAdapter e)
        {
            // expected
        }


        org.omg.CORBA.Object obj2 = poa.create_reference_with_id("id1".getBytes (), BasicServerHelper.id ());
        poa.reference_to_id(obj2);
        try
        {
            rootPOA.reference_to_id(obj2);

            fail ("Should have thrown wrong adapter");
        }
        catch (WrongAdapter e)
        {
            // expected
        }


        ((org.jacorb.orb.ORB)orb).addObjectKey("blabla", orb.object_to_string(obj2));

        String url = "corbaloc::localhost:1234/blabla";
        org.omg.CORBA.Object obj3 = orb.string_to_object(url);

        poa.reference_to_id(obj3);
        try
        {
            rootPOA.reference_to_id(obj3);

            fail ("Should have thrown wrong adapter");
        }
        catch (WrongAdapter e)
        {
            // expected
        }


        url = "corbaloc::localhost:1234/ablabl";
        org.omg.CORBA.Object obj4 = orb.string_to_object(url);

        try
        {
            rootPOA.reference_to_id(obj4);

            fail ("Should have thrown wrong adapter");
        }
        catch (WrongAdapter e)
        {
            // expected
        }


        ((org.jacorb.orb.ORB)orb).addObjectKey("foo",orb.object_to_string(obj));
        url = "corbaloc::localhost:1234/foo";
        org.omg.CORBA.Object obj5 = orb.string_to_object(url);
        rootPOA.reference_to_id(obj5);
        try
        {
            poa.reference_to_id(obj5);

            fail ("Should have thrown wrong adapter");
        }
        catch (WrongAdapter e)
        {
            // expected
        }

    }
}
