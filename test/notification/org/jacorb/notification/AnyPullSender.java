package org.jacorb.notification;

import org.omg.CosNotifyComm.PullSupplierPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumer;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumer;
import org.apache.log4j.Logger;



/*
 *        JacORB - a free Java ORB
 */

/**
 * AnyPullSender.java
 *
 *
 * Created: Tue Nov 12 16:03:52 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class AnyPullSender extends PullSupplierPOA {
    Logger logger_ = Logger.getLogger("TEST.AnyPullSender");

    Any event_;
    Any invalidAny_;
    ProxyPullConsumer myConsumer_;
    boolean disconnectCalled_ = false;

    public AnyPullSender(ORB orb, POA poa, ProxyPullConsumer myConsumer) throws Exception {
	myConsumer.connect_any_pull_supplier(_this(orb));
	myConsumer_ = myConsumer;
	invalidAny_ = orb.create_any();
    }
    
    void reset() {
	event_ = null;
    }

    boolean disconnected() {
	logger_.info("disc: "+ disconnectCalled_);
	return disconnectCalled_;
    }

    void shutdown() {
	myConsumer_.disconnect_pull_consumer();
    }

    void send(Any event) {
	synchronized(this) {
	    event_ = event;
	    logger_.info("send(Any)");
	}
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {}
    }

    boolean sent() {
	return sent_;
    }

    boolean sent_ = false;

    public void subscription_change(EventType[] e1, EventType[] e2) {
    }

    public Any pull() throws Disconnected {
	logger_.info("pull()");

	BooleanHolder _b = new BooleanHolder();
	Any _event;
	while(true) {
	    _event = try_pull(_b);
	    if(_b.value) {
		return _event;
	    }
	    Thread.yield();
	}
    }

    public Any try_pull(BooleanHolder success) throws Disconnected {
	Any _event = invalidAny_;
	success.value = false;
	if (event_ != null) {
	    synchronized(this) {
		if (event_ != null) {
		    _event = event_;
		    event_ = null;
		    success.value = true;
		    logger_.debug("try_pull will be successful");
		    sent_ = true;
		}
	    }
	}
	return _event;
    }

    public void disconnect_pull_supplier() {
	logger_.info("disconnect_pull_supplier");
	disconnectCalled_ = true;
	logger_.info("status: "+ disconnectCalled_);
    }

}// AnyPullSender
