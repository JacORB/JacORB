/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.test.bugs.bug459;

import java.util.Properties;

import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPullConsumerPOA;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class TestCase extends junit.framework.TestCase
{
    class MyPullConsumer extends StructuredPullConsumerPOA
    {
        public void disconnect_structured_pull_consumer()
        {
            // TODO Auto-generated method stub
        }

        public void offer_change(EventType[] added, EventType[] removed) throws InvalidEventType
        {
            // TODO Auto-generated method stub
        }
    }

    public void testIt() throws Exception
    {
        MyPullConsumer myPullConsumer = new MyPullConsumer();

        for (int run = 0; run < 5; run++)
        {
            System.out.println("Run " + run);
            // initialize ORB
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], props);
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            poa.the_POAManager().activate();

            myPullConsumer._this(orb);

            orb.shutdown(true);
        }
    }
}
