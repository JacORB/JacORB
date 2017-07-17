package org.jacorb.test.poa;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2017 Gerald Brose / The JacORB Team.
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

import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.poa.RequestQueueListener;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;

import java.util.Properties;

public class RequestQueueListenerTest extends ORBTestCase implements RequestQueueListener
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("jacorb.poa.queue_listeners", RequestQueueListenerTest.class.getName());
    }

    @Test
    public void testCreateListener () throws Exception
    {
        // Create a child POA
        POA poa = rootPOA.create_POA
        (
                "TestServerPOA",
                rootPOA.the_POAManager(),
                new Policy[]
                {
                        rootPOA.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID)
                }
        );

        poa.the_POAManager().activate();
    }

    @Override public void requestAddedToQueue(ServerRequest request, int queue_size)
    {

    }

    @Override public void requestRemovedFromQueue(ServerRequest request, int queue_size)
    {

    }
}
