package org.jacorb.notification;

import org.omg.CosNotifyComm.PushConsumerPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplier;
import org.apache.log4j.Logger;

public class AnyPushReceiver extends PushConsumerPOA implements Runnable {
    Any event_ = null;
    ORB orb_;
    POA poa_;
    Logger logger_ = Logger.getLogger("TEST.AnyPushReceiver");
    boolean disconnectCalled_;

    /**
     * returns true if a event was received
     */
    boolean received() {
	logger_.info("received()");
	logger_.debug("event = " + event_);

	return (event_ != null);
    }

    AnyPushReceiver(ORB orb, POA poa, ProxyPushSupplier supplier) throws Exception {
	this(orb, poa);
	supplier.connect_any_push_consumer(_this(orb));
    }
    
    public AnyPushReceiver(ORB orb, POA poa) throws Exception {
	super();
	orb_ = orb;
	poa_ = poa;
    }

    public void run() {
	if (event_ == null) {
	    synchronized(this) {
		try {
		    wait(2000);
		} catch (InterruptedException e) {}
	    }
	}
    }

    public void push(Any any) throws Disconnected {
	logger_.info("push()");
	event_ = any;
	synchronized(this) {
	    notifyAll();
	}
    }

    public boolean disconnected() {
	return disconnectCalled_;
    }

    public void disconnect_push_consumer() {
	disconnectCalled_ = true;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {}
}
