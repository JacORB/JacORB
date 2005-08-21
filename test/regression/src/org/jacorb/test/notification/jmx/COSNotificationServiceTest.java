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

package org.jacorb.test.notification.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jacorb.notification.jmx.COSNotificationService;
import org.jacorb.notification.jmx.JMXManageableMBeanProvider;
import org.jacorb.notification.jmx.mx4j.MX4JCOSNotificationService;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

public class COSNotificationServiceTest extends TestCase
{
    private MBeanServer mBeanServer_;

    private ORB orb_;

    private ObjectName objectName_;

    public COSNotificationServiceTest(String arg)
    {
        super(arg);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mBeanServer_ = MBeanServerFactory.createMBeanServer();
        orb_ = ORB.init(new String[0], null);

        COSNotificationService notifyMBean = new MX4JCOSNotificationService(orb_, mBeanServer_, new JMXManageableMBeanProvider("TestDomain"),
                new String[0]);

        objectName_ = ObjectName.getInstance("test:type=EventChannelFactory");
        mBeanServer_.registerMBean(notifyMBean, objectName_);
    }

    public void testStart() throws Exception
    {
        mBeanServer_.invoke(objectName_, "start", new Object[0], new String[0]);

        Object ior = mBeanServer_.getAttribute(objectName_, "IOR");

        org.omg.CORBA.Object object = orb_.string_to_object((String) ior);

        EventChannelFactory factory = EventChannelFactoryHelper.narrow(object);

        assertFalse(factory._non_existent());
    }

    public void testStop() throws Exception
    {
        mBeanServer_.invoke(objectName_, "start", new Object[0], new String[0]);

        Object ior = mBeanServer_.getAttribute(objectName_, "IOR");

        org.omg.CORBA.Object object = orb_.string_to_object((String) ior);

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
