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
