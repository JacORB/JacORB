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

import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.PortableServer.POA;

import org.jacorb.notification.EventChannelFactoryImpl;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 *  Unit Test for class NotificationTestCase.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationTestCase extends TestCase {

    NotificationTestCaseSetup setup_;

    private EventChannelFactoryImpl factoryServant_;

    public void tearDown() {
        if (factoryServant_ != null) {
            factoryServant_.dispose();
        }
    }

    public ORB getORB() {

        return setup_.getClientOrb();

    }

    public POA getPOA() {

        return setup_.poa_;

    }

    public NotificationTestUtils getTestUtils() {
        return setup_.getTestUtils();
    }

    public EventChannelFactory getEventChannelFactory() {
        return setup_.getServant().getEventChannelFactory();
    }

    public EventChannelFactory getLocalEventChannelFactory() throws Exception {
        factoryServant_ = EventChannelFactoryImpl.newFactory();

        return EventChannelFactoryHelper.narrow(factoryServant_._this(getORB()));
    }

    public NotificationTestCaseSetup getSetup() {
        return setup_;
    }

    /**
     * Creates a new <code>NotificationTestCase</code> instance.
     *
     * @param name test name
     */
    public NotificationTestCase(String name, NotificationTestCaseSetup setup) {
        super(name);
        setup_ = setup;
    }
}
