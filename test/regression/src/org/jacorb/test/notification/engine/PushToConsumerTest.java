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

import org.jacorb.notification.engine.PushToConsumerTask;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.test.notification.MockMessage;
import org.jacorb.util.Debug;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PushToConsumerTest extends TestCase
{
    Logger test_logger = Debug.getNamedLogger(getClass().getName());

    TaskProcessor taskProcessor_;

    ORB orb = ORB.init();

    public void setUp() throws Exception
    {
        taskProcessor_ = new TaskProcessor();
    }


    public void tearDown() throws Exception
    {
        taskProcessor_.dispose();
    }


    public void testPush() throws Exception
    {
        PushToConsumerTask task =
            new PushToConsumerTask(taskProcessor_);

        MockMessage event =
            new MockMessage("testEvent");

        Any any = orb.create_any();
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


    public PushToConsumerTest (String name)
    {
        super(name);
    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite(PushToConsumerTest.class);

        return suite;
    }


    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
