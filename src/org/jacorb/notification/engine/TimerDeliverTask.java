package org.jacorb.notification.engine;

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

import org.jacorb.notification.interfaces.TimerEventConsumer;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.NotConnected;

/**
 *
 *
 * Created: Thu Jan 30 01:31:47 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TimerDeliverTask extends TaskBase {

    private TimerEventConsumer target_;
    
    public void setTimedDeliverTarget(TimerEventConsumer target) {
	target_ = target;
    }

    public void doWork() throws Disconnected, NotConnected {
	if (target_.hasPendingEvents()) {

	    target_.deliverPendingEvents();

	    if (target_.hasPendingEvents()) {
		setStatus(RESCHEDULE);
	    } else {
		setStatus(DONE);
	    }
	}
    }

    public void reset() {
	super.reset();
	target_ = null;
    }

}
