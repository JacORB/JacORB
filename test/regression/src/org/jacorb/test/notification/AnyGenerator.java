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
import org.omg.CORBA.Any;
import org.jacorb.test.notification.TimingTest;
import org.jacorb.test.notification.TimingTestHelper;

/**
 * AnyGenerator.java
 *
 *
 * Created: Wed Feb 12 13:28:29 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

class AnyGenerator implements TestEventGenerator {

    ORB orb_;
    int counter_ = 0;

    AnyGenerator(ORB orb) {
	orb_ = orb;
    }

    public Any getNextEvent() {
	TimingTest _t = new TimingTest();
	_t.id = counter_++;
	_t.currentTime = (int)System.currentTimeMillis();

	Any _event = orb_.create_any();
	TimingTestHelper.insert(_event, _t);

	return _event;
    }
}
