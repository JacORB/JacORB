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
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.BoundedPriorityEventQueue;
import org.jacorb.notification.queue.EventQueue;
import org.jacorb.notification.queue.EventQueueOverflowStrategy;
import org.jacorb.test.notification.MockMessage;

import org.omg.CORBA.Any;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;

import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.jacorb.notification.ConfigurableProperties;
import org.jacorb.notification.Constants;
import org.jacorb.util.Environment;

/**
 *  Unit Test for class PushToConsumer
 *
 *
 * Created: Mon Aug 18 16:11:37 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PushToConsumerTest extends TestCase
{

    TaskProcessor taskProcessor_;
    ORB orb = ORB.init();

    public void setUp() throws Exception {

        taskProcessor_ = new TaskProcessor();

    }

    public void tearDown() throws Exception {
        taskProcessor_.dispose();
    }

    public void testRepeatedDeliveryErrorsCauseTheConsumerToBeDisconnected() throws Exception {
        MockMessage msg = new MockMessage();

        Any any = orb.create_any();

        msg.setAny(any);

        PushToConsumerTask task = new PushToConsumerTask();

        task.setTaskFinishHandler(taskProcessor_.getTaskConfigurator().deliverTaskFinishHandler_);
        task.setTaskErrorHandler(taskProcessor_.getTaskConfigurator().deliverTaskErrorHandler_);
        task.setMessage(msg.getHandle());

        MockEventConsumer eventConsumer = new MockEventConsumer() {
                int counter = 0;
                boolean enabled = true;

                public boolean hasPendingEvents() {
                    return true;
                }

                public void deliverPendingEvents() {
                    throw new TRANSIENT();
                }

                public void deliverEvent(Message event) {
                    counter++;
                    if (enabled) {
                        throw new TRANSIENT();
                    }
                }

                public void enableDelivery() {
                    super.enableDelivery();

                    enabled = true;
                }

                public void disableDelivery() {
                    super.disableDelivery();

                    enabled = false;
                }

                public void check() {
                    super.check();
                    Assert.assertTrue(counter > 0);
                }
            };

        eventConsumer.expectedDisposeCalls = 1;

        task.setEventConsumer(eventConsumer);

        taskProcessor_.schedulePushToConsumerTask(task);

        long sleepTime =
            Environment.getIntPropertyWithDefault( ConfigurableProperties.BACKOUT_INTERVAL,
                                                   Constants.DEFAULT_BACKOUT_INTERVAL )
            * (Environment.getIntPropertyWithDefault(ConfigurableProperties.EVENTCONSUMER_ERROR_THRESHOLD,
                                                    Constants.DEFAULT_EVENTCONSUMER_ERROR_THRESHOLD)
            + 2);

        Thread.sleep(sleepTime);

        eventConsumer.check();
    }

    public void testPushFailRetry() throws Exception {

        MockMessage event1 =
            new MockMessage();

        Any any1 = orb.create_any();

        event1.setAny(any1);

        MockMessage event2 =
            new MockMessage();

        Any any2 = orb.create_any();

        event2.setAny(any2);

        PushToConsumerTask task =
            new PushToConsumerTask();

        task.setTaskFinishHandler(taskProcessor_.getTaskConfigurator().deliverTaskFinishHandler_);
        task.setTaskErrorHandler(taskProcessor_.getTaskConfigurator().deliverTaskErrorHandler_);
        task.setMessage(event1.getHandle());


        PushToConsumerTask task2 =
            new PushToConsumerTask();

        task2.setTaskFinishHandler(taskProcessor_.getTaskConfigurator().deliverTaskFinishHandler_);
        task2.setTaskErrorHandler(taskProcessor_.getTaskConfigurator().deliverTaskErrorHandler_);
        task2.setMessage(event2.getHandle());


        MockEventConsumer eventConsumer = new MockEventConsumer() {
                boolean once = false;
                public void deliverEvent(Message event) {
                    if (!once) {
                        once = true;
                        throw new TRANSIENT();
                    } else {
                        super.deliverEvent(event);
                    }
                }
            };

        task.setEventConsumer(eventConsumer);

        task2.setEventConsumer(eventConsumer);

        taskProcessor_.schedulePushToConsumerTask(task);

        Thread.sleep(100);

        taskProcessor_.schedulePushToConsumerTask(task2);

        Thread.sleep(4000);

        eventConsumer.addToExcepectedEvents(any1);
        eventConsumer.addToExcepectedEvents(any2);

        eventConsumer.check();
    }

    public void testPushFailDispose() throws Exception {

        PushToConsumerTask task =
            new PushToConsumerTask();

        MockMessage event =
            new MockMessage();

        Any any = orb.create_any();

        event.setAny(any);

        task.setTaskFinishHandler(taskProcessor_.getTaskConfigurator().deliverTaskFinishHandler_);
        task.setTaskErrorHandler(taskProcessor_.getTaskConfigurator().deliverTaskErrorHandler_);
        task.setMessage(event.getHandle());

        MockEventConsumer eventConsumer = new MockEventConsumer()  {
                public void deliverEvent(Message event) {
                    throw new OBJECT_NOT_EXIST();
                }
            };

        task.setEventConsumer(eventConsumer);

        taskProcessor_.schedulePushToConsumerTask(task);

        Thread.sleep(1000);

        eventConsumer.check();


        assertTrue(eventConsumer.disposeCalled == 1);
    }

    public void testPush() throws Exception {

        PushToConsumerTask task =
            new PushToConsumerTask();

        MockMessage event =
            new MockMessage("testEvent");

        Any any = orb.create_any();
        any.insert_string("test");

        event.setAny(any);

        task.setTaskFinishHandler(taskProcessor_.getTaskConfigurator().deliverTaskFinishHandler_);
        task.setTaskErrorHandler(taskProcessor_.getTaskConfigurator().deliverTaskErrorHandler_);
        task.setMessage(event.getHandle());

        MockEventConsumer eventConsumer = new MockEventConsumer();

        eventConsumer.addToExcepectedEvents(any);

        task.setEventConsumer(eventConsumer);

        taskProcessor_.schedulePushToConsumerTask(task);

        Thread.sleep(1000);

        eventConsumer.check();
    }

    /**
     * Creates a new <code>PushToConsumerTest</code> instance.
     *
     * @param name test name
     */
    public PushToConsumerTest (String name)
    {
        super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(PushToConsumerTest.class);

        suite = new TestSuite();
        suite.addTest(new PushToConsumerTest("testRepeatedDeliveryErrorsCauseTheConsumerToBeDisconnected"));

        return suite;
    }

    /**
     * Entry point
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

}
