package org.jacorb.notification;

import org.omg.CosEventComm.PullConsumerPOA;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventComm.Disconnected;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;

/*
 *        JacORB - a free Java ORB
 */

/**
 * CosEventPullReceiver.java
 *
 *
 * Created: Tue Nov 26 11:03:03 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class CosEventPullReceiver extends PullConsumerPOA implements Runnable {

    ProxyPullSupplier mySupplier_;
    Any event_ = null;
    long TIMEOUT = 1000;
    boolean error_ = false;

    public CosEventPullReceiver(ORB orb, ProxyPullSupplier supplier) throws AlreadyConnected {
	mySupplier_ = supplier;
	supplier.connect_pull_consumer(_this(orb));
    }
    
    public void disconnect_pull_consumer() {
    }

    public boolean received() {
	return (event_ != null);
    }

    public void run() {
	long _startTime = System.currentTimeMillis();
	long _stopTime = _startTime + TIMEOUT;
	BooleanHolder _success = new BooleanHolder();
	try {
	    while (System.currentTimeMillis() < _stopTime) {
		event_ = mySupplier_.try_pull(_success);
		if (_success.value) {
		    break;
		}
		Thread.yield();
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

}// CosEventPullReceiver
