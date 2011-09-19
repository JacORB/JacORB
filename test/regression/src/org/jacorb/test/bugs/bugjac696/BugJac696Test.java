package org.jacorb.test.bugs.bugjac696;

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

import java.lang.reflect.Field;
import org.jacorb.poa.AOM;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;

/**
 * <code>BugJac696Test</code> tests that reference_to_id throws the correct
 * exceptions.
 *
 * @author Nick Cross
 * @version 1.0
 */
public class BugJac696Test extends ORBTestCase
{
    public void testBugJac696 () throws Exception
    {
        org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[1];
        policies[0] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

        POA poa = rootPOA.create_POA("ChildOne", rootPOA.the_POAManager(),policies);

        BasicServerImpl impl = new BasicServerImpl();
        org.omg.CORBA.Object obj = rootPOA.servant_to_reference (impl);

        poa.destroy(true, true);

        Thread root = getAOMThread ((org.jacorb.poa.POA)rootPOA);
        Thread child = getAOMThread ((org.jacorb.poa.POA)poa);

        assertTrue ("Root should still have AOM Thread", root.isAlive());
        assertTrue ("Child should not have AOM Thread", ( ! child.isAlive()));

        obj._release ();
    }


    /**
     * <code>getAOMThread</code> uses reflection to override access protection to
     * retrieve the AOMThread variable held in the AOM (which is held in POA).
     *
     * @param poa an <code>org.jacorb.poa.POA</code> value
     * @return a <code>Thread</code> value
     * @exception Exception if an error occurs
     */
    private Thread getAOMThread(org.jacorb.poa.POA poa) throws Exception
    {
        Field fields[] = org.jacorb.poa.POA.class.getDeclaredFields();
        AOM aom = null;
        Thread aomRemovalThread = null;

        for (int i = 0; i < fields.length; ++i)
        {
            if ("aom".equals(fields[i].getName()))
            {
                Field f = fields[i];
                f.setAccessible(true);
                aom = (org.jacorb.poa.AOM)f.get (poa);
                break;
            }
        }
        fields = AOM.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i)
        {
            if ("aomRemoval".equals(fields[i].getName()))
            {
                Field f = fields[i];
                f.setAccessible(true);
                aomRemovalThread = (Thread)f.get (aom);
                break;
            }
        }

        return aomRemovalThread;
    }
}
