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

import org.jacorb.notification.EventChannelFactoryImpl;
import org.jacorb.notification.EventChannelImpl;
import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.notification.MockMessage;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.jacorb.test.notification.StructuredPullReceiver;
import org.jacorb.test.notification.StructuredPushReceiver;

import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyFilter.MappingFilter;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.Debug;


/**
 *  Unit Test for class ReleaseTasks
 *
 *
 * Created: Sun Aug 17 11:48:32 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ReleaseTasksTest extends NotificationTestCase
{

    Logger logger_ = Debug.getNamedLogger(getClass().getName());

    EventChannelFactoryImpl factory_;
    EventChannelImpl eventChannelServant_;
    EventChannel eventChannel_;

    public void setUp() throws Exception {
        super.setUp();

        factory_ = EventChannelFactoryImpl.newFactory();

        Property[] p1 = new Property[0];
        Property[] p2 = new Property[0];

        eventChannelServant_ = factory_.create_channel_servant(0, p1, p2);
        eventChannel_ = eventChannelServant_.getEventChannel();
    }

    public void tearDown() {
        super.tearDown();

        factory_.dispose();
    }

    public void testAllTasksAreReleased() throws Exception {
        StructuredPushReceiver pushReceiver = new StructuredPushReceiver(this);
        pushReceiver.connect(getSetup(), eventChannel_, false);

        StructuredPullReceiver pullReceiver = new StructuredPullReceiver(this);
        pullReceiver.connect(getSetup(), eventChannel_, false);

        final FilterStage supplierAdminMock = new MockFilterStage() {
                public List getSubsequentFilterStages() {
                    return eventChannelServant_.getAllConsumerAdmins();
                }
            };

        final FilterStage proxyConsumerMock = new MockFilterStage() {
                public List getSubsequentFilterStages() {
                    return Collections.singletonList(supplierAdminMock);
                }
            };

        MockMessage eventMock = new MockMessage();
        eventMock.setFilterStage(proxyConsumerMock);
        eventMock.setStructuredEvent(getTestUtils().getStructuredEvent());
        eventMock.setMaxRef(2);

        Message event = eventMock.getHandle();

        eventChannelServant_.getChannelContext().dispatchEvent(event);

        pullReceiver.run();

        Thread.sleep(1000);

        assertTrue(pushReceiver.isEventHandled());
        assertTrue(pullReceiver.isEventHandled());

        eventMock.validateRefCounter();
    }

    /**
     * Creates a new <code>ReleaseTasksTest</code> instance.
     *
     * @param name test name
     */
    public ReleaseTasksTest (String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite() throws Exception
    {
        TestSuite _suite;

        _suite = new TestSuite("");

        NotificationTestCaseSetup _setup =
            new NotificationTestCaseSetup(_suite);

        String[] methodNames = TestUtils.getTestMethods( ReleaseTasksTest.class);

        for (int x=0; x<methodNames.length; ++x) {
            _suite.addTest(new ReleaseTasksTest(methodNames[x], _setup));
        }

        return _setup;
    }

    /**
     * Entry point
     */
    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(suite());
    }
}

class MockFilterStage implements FilterStage {

    public boolean isDisposed() {
        return false;
    }

    public List getSubsequentFilterStages() {
        return null;
    }

    public List getFilters() {
        return Collections.EMPTY_LIST;
    }

    public boolean hasEventConsumer() {
        return false;
    }

    public boolean hasOrSemantic() {
        return false;
    }

    public EventConsumer getEventConsumer() {
        return null;
    }

    public boolean hasLifetimeFilter() {
        return false;
    }

    public boolean hasPriorityFilter() {
        return false;
    }

    public MappingFilter getLifetimeFilter() {
        return null;
    }

    public MappingFilter getPriorityFilter() {
        return null;
    }

}
