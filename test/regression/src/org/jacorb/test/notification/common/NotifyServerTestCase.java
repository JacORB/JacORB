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

package org.jacorb.test.notification.common;

import java.lang.reflect.Constructor;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

public abstract class NotifyServerTestCase extends ClientServerTestCase
{
    public NotifyServerTestCase(String name, NotifyServerTestSetup setup)
    {
        super(name, setup);
    }

    public final EventChannelFactory getEventChannelFactory()
    {
        EventChannelFactory channelFactory = EventChannelFactoryHelper.narrow(setup.getServerObject());

        return channelFactory;
    }

    public EventChannel getDefaultChannel() throws UnsupportedAdmin, UnsupportedQoS
    {
        return getEventChannelFactory().create_channel(new Property[0], new Property[0], new IntHolder());
    }
    
    public final void setUp() throws Exception
    {
        super.setUp();
        
        setUpTest();
    }
    
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
        TestSuite _suite = new TestSuite(suiteName);

        NotifyServerTestSetup _setup = new NotifyServerTestSetup(_suite);

        String[] _methodNames = TestUtils.getTestMethods(clazz, testMethodPrefix);

        addToSuite(_suite, _setup, clazz, _methodNames);

        return _setup;
    }
    
    private static void addToSuite(TestSuite suite, NotifyServerTestSetup setup, Class clazz,
            String[] testMethods) throws Exception
    {
        Constructor _ctor = clazz.getConstructor(new Class[] { String.class,
                NotifyServerTestSetup.class });

        for (int x = 0; x < testMethods.length; ++x)
        {
            suite.addTest((Test) _ctor.newInstance(new Object[] { testMethods[x], setup }));
        }
    }
}
