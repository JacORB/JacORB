package org.jacorb.test.notification;

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

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.PortableServer.POA;

import org.jacorb.notification.EventChannelFactoryImpl;

import junit.framework.TestCase;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationTestCase extends TestCase {

    private NotificationTestCaseSetup setup_;

    private EventChannel defaultChannel_;

    ////////////////////////////////////////

    public NotificationTestCase(String name, NotificationTestCaseSetup setup) {
        super(name);

        setup_ = setup;
    }

    ////////////////////////////////////////

    public void tearDown() throws Exception {
        super.tearDown();

        if (defaultChannel_ != null) {
            defaultChannel_.destroy();
        }
    }


    public EventChannel getDefaultChannel() throws Exception {
        if (defaultChannel_ == null) {
            defaultChannel_ = getFactory().create_channel(new Property[0],
                                                          new Property[0],
                                                          new IntHolder() );
        }

        return defaultChannel_;
    }


    public ORB getORB() {
        return setup_.getORB();
    }


    public POA getPOA() {
        return setup_.getPOA();
    }


    public NotificationTestUtils getTestUtils() {
        return setup_.getTestUtils();
    }


    public EventChannelFactory getFactory() {
        try {
            return setup_.getFactoryServant().getEventChannelFactory();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    private NotificationTestCaseSetup getSetup() {
        return setup_;
    }
}
