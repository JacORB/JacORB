package org.jacorb.notification;

import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushConsumerPOA;
import org.omg.CosEventComm.Disconnected;
import org.omg.CORBA.Any;



/*
 *        JacORB - a free Java ORB
 */

/**
 * CosEventPushReceiver.java
 *
 *
 * Created: Fri Nov 22 18:21:29 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class CosEventPushReceiver extends PushConsumerPOA implements Runnable {
    Any event_;
    long TIMEOUT = 1000;
    boolean received_ = false;

    public CosEventPushReceiver() {
    }
    
    public void push(Any event) throws Disconnected {
	synchronized(this) {
	    event_ = event;
	    notifyAll();
	}
    }

    public void disconnect_push_consumer() {
    }

    public void run() {
	if (event_ == null) {
	    synchronized(this) {
		try {
		    wait(TIMEOUT);
		} catch (InterruptedException e) {}

		if (event_ != null) {
		    received_ = true;
		}
	    }
	}
    }

    public boolean received() {
	return received_;
    }
}// CosEventPushReceiver
