package org.jacorb.test.notification.engine;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import junit.framework.Test;

import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.engine.PushToConsumerTask;
import org.jacorb.test.notification.MockMessage;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.Any;


/**
 * @author Alphonse Bendt
 */

public class PushToConsumerTest extends NotificationTestCase
{
    DefaultTaskProcessor taskProcessor_;

    public void setUp() throws Exception
    {
        taskProcessor_ = new DefaultTaskProcessor();

        taskProcessor_.configure( getConfiguration() );
    }


    public void tearDown() throws Exception
    {
        taskProcessor_.dispose();
    }


    public void testPush() throws Exception
    {
        PushToConsumerTask task =
            new PushToConsumerTask(taskProcessor_);

        task.configure(getConfiguration());

        MockMessage event =
            new MockMessage("testEvent");

        event.configure(getConfiguration());

        Any any = getORB().create_any();

        any.insert_string("test");

        event.setAny(any);

        task.setMessage(event.getHandle());

        MockEventConsumer eventConsumer = new MockEventConsumer();

        eventConsumer.addToExcepectedEvents(any);

        task.setMessageConsumer(eventConsumer);

        task.schedule();

        Thread.sleep(1000);

        eventConsumer.check();
    }


    public PushToConsumerTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }


    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(PushToConsumerTest.class);
    }
}
