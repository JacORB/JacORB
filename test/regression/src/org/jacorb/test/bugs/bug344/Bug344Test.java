package org.jacorb.test.bugs.bug344;

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

import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;

/**
 * <code>TestCase</code> tests rapid activation and deactivation of
 * objects in order to ensure the threading is correct.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version 1.0
 */
public class Bug344Test extends ORBTestCase
{
    /**
     * <code>testActivateDeactivate1</code> tests activating an object without
     * ID (i.e. using a new one each time) and using servant_to_id to obtain
     * the ID to deactivate_the_object.
     */
	public void testActivateDeactivate1 () throws Exception
	{
		BasicServerImpl soi = new BasicServerImpl();

		for (int count=0;count<100;count++)
		{
			rootPOA.activate_object( soi);
			rootPOA.deactivate_object(rootPOA.servant_to_id(soi));
		}
	}


    /**
     * <code>testActivateDeactivate2</code> tests activating and deactivating
     * the object using the same ID.
     */
	public void testActivateDeactivate2 () throws Exception
	{
		BasicServerImpl soi = new BasicServerImpl();

		// This will activate it so do deactivate first
		byte []id = rootPOA.servant_to_id( soi );

		for (int count=0;count<100;count++)
		{
			rootPOA.deactivate_object(id);
			rootPOA.activate_object_with_id(id, soi);
		}
	}


    /**
     * <code>testActivateDeactivate3</code> tests activating an object using a POA policy
     * of MULTIPLE_ID.
     */
	public void testActivateDeactivate3 () throws Exception
	{
		// create POA
		Policy policies[] = new Policy[3];
		policies[0] = rootPOA.create_id_assignment_policy(
				org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID);
		policies[1] = rootPOA.create_id_uniqueness_policy(
				org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID);
		policies[2] = rootPOA.create_servant_retention_policy(
				org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);

		POA poa = rootPOA.create_POA("system_id", rootPOA.the_POAManager(), policies);

		BasicServerImpl soi = new BasicServerImpl();

		byte [] id = poa.activate_object(soi);

		for (int count=0;count<100;count++)
		{
			poa.deactivate_object(id);
			poa.activate_object_with_id( id, soi);
		}
	}
}
