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

package org.jacorb.test.notification.jmx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Constructor;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import org.jacorb.notification.jmx.COSNotificationService;
import org.jacorb.notification.jmx.JMXManageableMBeanProvider;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

public class COSNotificationServiceTest extends ORBTestCase
{
    private MBeanServer mBeanServer_;

    private ObjectName objectName_;

    @Before
    public void setUp() throws Exception
    {
        mBeanServer_ = MBeanServerFactory.createMBeanServer();

        try
        {
            Class<?> mcns = TestUtils.classForName ("org.jacorb.notification.jmx.mx4j.MX4JCOSNotificationService");

            Constructor<?> constructor = mcns.getConstructors()[0];
            COSNotificationService notifyMBean = (COSNotificationService) constructor.newInstance(new Object[] { orb, mBeanServer_, new JMXManageableMBeanProvider("TestDomain"),
                    new String[0] } );

            objectName_ = ObjectName.getInstance("test:type=EventChannelFactory");
            mBeanServer_.registerMBean(notifyMBean, objectName_);
        }
        catch (ClassNotFoundException e)
        {
            Assume.assumeFalse("Caught class not found " + e, true);
        }
    }

    @Test
    public void testStart() throws Exception
    {
        mBeanServer_.invoke(objectName_, "start", new Object[0], new String[0]);

        Object ior = mBeanServer_.getAttribute(objectName_, "IOR");

        org.omg.CORBA.Object object = orb.string_to_object((String) ior);

        EventChannelFactory factory = EventChannelFactoryHelper.narrow(object);

        assertFalse(factory._non_existent());
    }

    @Test
    public void testStop() throws Exception
    {
        mBeanServer_.invoke(objectName_, "start", new Object[0], new String[0]);

        Object ior = mBeanServer_.getAttribute(objectName_, "IOR");

        org.omg.CORBA.Object object = orb.string_to_object((String) ior);

        EventChannelFactory factory = EventChannelFactoryHelper.narrow(object);

        mBeanServer_.invoke(objectName_, "stop", new Object[0], new String[0]);

        try
        {
            assertTrue(factory._non_existent());
        } catch (OBJECT_NOT_EXIST e)
        {
            // expecteds
        }
    }
}
