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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.omg.CORBA.ORB;
import org.jacorb.test.common.ClientServerTestCase;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

/**
 *  Unit Test for class NotificationTestCase.java
 *
 *
 * Created: Thu Mar 27 15:29:42 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationTestCase extends TestCase {

    NotificationTestCaseSetup setup_;

    public ORB getORB() {
	return setup_.getClientOrb();
    }

    public POA getPOA() {
	return setup_.poa_;
    }

    public TestUtils getTestUtils() {
	return setup_.getTestUtils();
    }

    public EventChannelFactory getEventChannelFactory() {
	return setup_.getServant().getEventChannelFactory();
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
