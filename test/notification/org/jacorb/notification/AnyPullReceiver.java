package org.jacorb.notification;

import org.omg.CosNotifyComm.PullConsumerPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventComm.Disconnected;
import org.apache.log4j.Logger;



/*
 *        JacORB - a free Java ORB
 */

/**
 * AnyPullReceiver.java
 *
 *
 * Created: Tue Nov 12 14:31:04 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class AnyPullReceiver extends PullConsumerPOA implements Runnable {

    Logger logger_ = Logger.getLogger("TEST.AnyPullReceiver");

    Any event_ = null;
    boolean received_ = false;
    boolean error_ = false;
    ORB orb_;
    POA poa_;
    ProxyPullSupplier mySupplier_;
    long TIMEOUT = 5000;
    boolean disconnectCalled_;

    public AnyPullReceiver(ORB orb, POA poa, ProxyPullSupplier mySupplier) throws Exception {
	orb_ = orb;
	poa_ = poa;
	mySupplier_ = mySupplier;
	mySupplier_.connect_any_pull_consumer(_this(orb_));
    }

    void reset() {
	error_ = false;
	received_ = false;
	event_ = null;
    }
    
    boolean disconnected() {
	return disconnectCalled_;
    }

    boolean received() {
	return received_;
    }

    boolean error() {
	return error_;
    }

    public void run() {
	BooleanHolder _success = new BooleanHolder();
	_success.value = false;
	long _startTime = System.currentTimeMillis();
	logger_.info("start receiver");

	try {
	    while (true) {
		event_ = mySupplier_.try_pull(_success);
	    
		if (_success.value) {
		    logger_.debug("Received Event");
		    received_ = true;
		    break;
		} 

		if (System.currentTimeMillis() < _startTime + TIMEOUT) {
		    Thread.yield();
		} else {
		    logger_.debug("Timeout");
		    received_ = false;
		    break;
		}
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }

    public void disconnect_pull_consumer() {
	disconnectCalled_ = true;
    }

}// AnyPullReceiver
