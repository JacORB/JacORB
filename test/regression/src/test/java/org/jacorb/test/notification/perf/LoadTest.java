/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.notification.StructuredPushReceiver;
import org.jacorb.test.notification.StructuredPushSender;
import org.jacorb.test.notification.common.NotifyServerTestCase;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.jacorb.test.harness.TestUtils;

/**
 * @author Alphonse Bendt
 */
public class LoadTest extends NotifyServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeTrue(System.getProperty("jacorb.test.notificationperf", "false").equals("true"));
    }

    int count = 0;

    EventChannelFactory factory;

    EventChannel channel;

    IntHolder intHolder;

    boolean active = true;

    @Before
    public void setUp() throws Exception
    {
        factory = getEventChannelFactory();
        intHolder = new IntHolder();

        channel = factory.create_channel(new Property[0], new Property[0], intHolder);
    }

    @After
    public void tearDown()
    {
        channel.destroy();
    }

    @Test
    public void testLoad() throws Exception
    {
        StructuredPushSender sender = new StructuredPushSender(setup.getClientOrb());

        StructuredPushReceiver receiver = new StructuredPushReceiver(setup.getClientOrb())
        {
            @Override
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

                TestUtils.getLogger().debug("Received ...");

                super.push_structured_event(event);
            }
        };

        TestUtils.getLogger().debug("connect sender");
        sender.connect(channel, true);
        TestUtils.getLogger().debug("connect receiver");
        receiver.connect(channel, true);

        boolean _active = active;

        int batchSize = 1000;

        while (_active)
        {
            for (int x = 0; x < batchSize; ++x)
            {
                Any any = setup.getClientOrb().create_any();
                any.insert_long(x);

                StructuredEvent event = new StructuredEvent();
                event.filterable_data = new Property[] { new Property("number", any) };
                event.header = new EventHeader();
                event.header.fixed_header = new FixedEventHeader();
                event.header.variable_header = new Property[0];
                event.header.fixed_header.event_name = "event_name";
                event.header.fixed_header.event_type = new EventType("domain_name", "type_name");
                event.remainder_of_body = setup.getClientOrb().create_any();

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
}
