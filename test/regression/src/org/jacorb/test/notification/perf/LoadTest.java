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

package org.jacorb.test.notification.perf;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.jacorb.test.notification.StructuredPushReceiver;
import org.jacorb.test.notification.StructuredPushSender;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class LoadTest extends NotificationTestCase
{
    int count = 0;

    EventChannelFactory factory;

    EventChannel channel;

    IntHolder intHolder;

    boolean active = true;

    public LoadTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        ORB orb = getORB();

        factory = EventChannelFactoryHelper.narrow(orb
                .resolve_initial_references("NotificationService"));

        intHolder = new IntHolder();

        channel = factory.create_channel(new Property[0], new Property[0], intHolder);
    }

    public void tearDownTest()
    {
        channel.destroy();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(LoadTest.class);
    }

    public void testLoad() throws Exception
    {
        final List received = new ArrayList();

        StructuredPushSender sender = new StructuredPushSender(getClientORB());

        StructuredPushReceiver receiver = new StructuredPushReceiver(getClientORB())
        {
            public void push_structured_event(StructuredEvent event)
                    throws org.omg.CosEventComm.Disconnected
            {
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                
                System.out.println("Received ...");
                
                super.push_structured_event(event);
            }
        };

        System.out.println("connect sender");
        sender.connect(channel, true);
        System.out.println("connect receiver");
        receiver.connect(channel, true);

        boolean _active = active;

        int batchSize = 1000;

        while (_active)
        {
            for (int x = 0; x < batchSize; ++x)
            {
                Any any = getORB().create_any();
                any.insert_long(x);

                StructuredEvent event = new StructuredEvent();
                event.filterable_data = new Property[] { new Property("number", any) };
                event.header = new EventHeader();
                event.header.fixed_header = new FixedEventHeader();
                event.header.variable_header = new Property[0];
                event.header.fixed_header.event_name = "event_name";
                event.header.fixed_header.event_type = new EventType("domain_name", "type_name");
                event.remainder_of_body = getClientORB().create_any();

                event.remainder_of_body.insert_longlong(System.currentTimeMillis());
                sender.pushConsumer_.push_structured_event(event);

                // Thread.sleep(10);
            }

            synchronized (this)
            {
                _active = active;
            }
            Thread.sleep(4000);

            // assertEquals(batchSize, received.size());

            // _active = false;
        }

        Thread.sleep(60000);
    }

    public static void main(String[] args) throws Exception
    {
        TestRunner.run(suite());
    }
}