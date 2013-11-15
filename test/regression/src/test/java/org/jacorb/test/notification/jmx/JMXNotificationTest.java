/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.notification.jmx;

import static org.junit.Assert.fail;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.easymock.MockControl;
import org.jacorb.notification.jmx.BroadcastSupportMBeanDecorator;
import org.junit.Before;
import org.junit.Test;

public class JMXNotificationTest
{
    private MBeanServer mbeanServer_;

    @Before
    public void setUp() throws Exception
    {
        mbeanServer_ = MBeanServerFactory.createMBeanServer();
    }

    @Test
    public void testRegisteredJMXManageableDoesNotSupportNotification() throws Exception
    {
        TestService testService = new TestService();
        ObjectName name = new ObjectName("domain:type=service");
        mbeanServer_.registerMBean(testService, name);

        MockControl controlNotificationListener = MockControl.createControl(NotificationListener.class);
        NotificationListener mockNotificationListener = (NotificationListener) controlNotificationListener.getMock();

        MockControl controlNotificationFilter = MockControl.createControl(NotificationFilter.class);
        NotificationFilter mockNotificationFilter = (NotificationFilter) controlNotificationFilter.getMock();

        controlNotificationFilter.replay();
        controlNotificationListener.replay();

        try
        {
            mbeanServer_.addNotificationListener(name, mockNotificationListener,
                    mockNotificationFilter, name);
            fail();
        } catch (RuntimeOperationsException e)
        {
            // expected
        }

        controlNotificationFilter.verify();
        controlNotificationListener.verify();
    }

    @Test
    public void testRegisterWithDecorator() throws Exception
    {
        TestService testService = new TestService();
        ObjectName name = new ObjectName("domain:type=service");
        final BroadcastSupportMBeanDecorator mbean = new BroadcastSupportMBeanDecorator(testService);
        mbeanServer_.registerMBean(mbean, name);

        MockControl controlNotificationListener = MockControl.createControl(NotificationListener.class);
        NotificationListener mockNotificationListener = (NotificationListener) controlNotificationListener.getMock();

        MockControl controlNotificationFilter = MockControl.createControl(NotificationFilter.class);
        NotificationFilter mockNotificationFilter = (NotificationFilter) controlNotificationFilter.getMock();

        mockNotificationFilter.isNotificationEnabled(null);
        controlNotificationFilter.setMatcher(MockControl.ALWAYS_MATCHER);
        controlNotificationFilter.setReturnValue(true);

        mockNotificationListener.handleNotification(null, null);
        controlNotificationListener.setMatcher(MockControl.ALWAYS_MATCHER);

        controlNotificationFilter.replay();
        controlNotificationListener.replay();

        mbeanServer_.addNotificationListener(name, mockNotificationListener,
                mockNotificationFilter, name);

        testService.invokeMethod();

        controlNotificationFilter.verify();
        controlNotificationListener.verify();
    }
}
