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

package org.jacorb.test.notification.common;

import junit.framework.Test;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyFilter.Filter;

/**
 * base class for notification service integration tests.
 * this setup class will start a notification service in another
 * process.
 * 
 * @author Alphonse Bendt
 */
public abstract class NotifyServerTestCase extends ClientServerTestCase
{
    public NotifyServerTestCase(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }

    /**
     * access the EventChannelFactory 
     */
    public final EventChannelFactory getEventChannelFactory()
    {
        EventChannelFactory channelFactory = EventChannelFactoryHelper.narrow(setup
                .getServerObject());

        return channelFactory;
    }

    /**
     * creates an EventChannel with default settings
     */
    public EventChannel getDefaultChannel() throws UnsupportedAdmin, UnsupportedQoS
    {
        return getEventChannelFactory().create_channel(new Property[0], new Property[0],
                new IntHolder());
    }

    public final void setUp() throws Exception
    {
        super.setUp();

        setUpTest();
    }

    /**
     * do local test setup
     */
    protected void setUpTest() throws Exception
    {
        // no op
    }

    public static Test suite(Class clazz) throws Exception
    {
        return suite("TestSuite defined in Class " + clazz.getName(), clazz);
    }

    public static Test suite(String suiteName, Class clazz) throws Exception
    {
        return suite(suiteName, clazz, "test");
    }

    public static Test suite(String suiteName, Class clazz, String testMethodPrefix)
            throws Exception
    {
        return TestUtils.suite(clazz, NotifyServerTestSetup.class, suiteName, testMethodPrefix);
    }

    /**
     * access an ORB instance
     */
    public ORB getClientORB()
    {
        return setup.getClientOrb();
    }
    
    public Filter createFilter() throws Exception
    {
        return getDefaultChannel().default_filter_factory().create_filter("EXTENDED_TCL");
    }
}
