package org.jacorb.test.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import java.util.Properties;

import junit.framework.Test;

import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.conf.Attributes;
import org.omg.CORBA.ORB;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedNotificationChannelTest extends NotificationTestCase {

    public TypedNotificationChannelTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }

    public void testCreateTypedChannelFactory() throws Exception {
        ORB _orb = getORB();

        Properties _props = new Properties();

        _props.put(Attributes.ENABLE_TYPED_CHANNEL, "on");

        AbstractChannelFactory _servant = AbstractChannelFactory.newFactory(_orb, false, _props);

        org.omg.CORBA.Object _obj = _servant.activate();

        TypedEventChannelFactory _factory = TypedEventChannelFactoryHelper.narrow(_obj);
    }

    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedNotificationChannelTest.class);
    }
}
