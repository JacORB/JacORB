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

import org.jacorb.test.common.ClientServerSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.jacorb.notification.EventChannelFactoryImpl;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import junit.extensions.TestSetup;

/**
 * NotificationTestCaseSetup.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class NotificationTestCaseSetup extends TestSetup {

    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    ORB orb_;
    POA poa_;
    TestUtils testUtils_;
    private EventChannelFactoryImpl eventChannelServant_;

    public TestUtils getTestUtils() {
	return(testUtils_);
    }

    public NotificationTestCaseSetup(Test suite) throws Exception {
	super(suite);
    }

    public void setUp() throws Exception {
	logger_.debug("setUp");

	super.setUp();
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));
	testUtils_ = new TestUtils(orb_);
	eventChannelServant_ = new EventChannelFactoryImpl();
    }

    public void tearDown() throws Exception {
	logger_.debug("tearDown");
	eventChannelServant_.dispose();
	orb_.shutdown(true);
	super.tearDown();
	logger_.debug("tearDown - done");
    }
    
    public EventChannelFactoryImpl getServant() {
	logger_.debug("getServant: " + eventChannelServant_);
	return eventChannelServant_;
    }

    public ORB getClientOrb() {
	return orb_;
    }

    public POA getClientRootPOA() {
	return poa_;
    }
}
