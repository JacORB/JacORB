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
package org.jacorb.notification.engine;

import org.jacorb.notification.TransmitEventCapable;
import org.jacorb.notification.NotificationEvent;

/*
 *        JacORB - a free Java ORB
 */

/**
 * DeliverTask.java
 *
 *
 * Created: Thu Nov 14 23:24:10 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class DeliverTask implements Task {

    TransmitEventCapable[] destinations_;
    NotificationEvent event_;
    int status_;

    void configureDestinations(TransmitEventCapable[] dest) {
	destinations_ = dest;
    }

    public int getStatus() {
	return status_;
    }

    public void run() {
	for (int x=0; x<destinations_.length; ++x) {
	    destinations_[x].transmit_event(event_);
	}
	status_ = DELIVERED;
    }

}// DeliverTask
