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
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosEventChannelAdmin.AlreadyConnected;

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

public class AnyPullReceiver extends PullConsumerPOA implements Runnable, TestClientOperations {

    Logger logger_ = Logger.getLogger("TEST.AnyPullReceiver");

    Any event_ = null;
    boolean received_ = false;
    boolean error_ = false;
    ORB orb_;
    POA poa_;
    ProxyPullSupplier mySupplier_;
    long TIMEOUT = 5000;
    boolean connected_;

    public void connect(ORB orb, POA poa, EventChannel channel) throws AlreadyConnected, AdminLimitExceeded {
	orb_ = orb;
	poa_ = poa;
	IntHolder _proxyId = new IntHolder();
	mySupplier_ = ProxyPullSupplierHelper.narrow(channel.default_consumer_admin().obtain_notification_pull_supplier(ClientType.ANY_EVENT, _proxyId));
	mySupplier_.connect_any_pull_consumer(_this(orb));
	connected_ = true;
    }

    public void shutdown() {
	mySupplier_.disconnect_pull_supplier();
	mySupplier_ = null;
    }

    void reset() {
	error_ = false;
	received_ = false;
	event_ = null;
    }
    
    public boolean isConnected() {
	return connected_;
    }

    public boolean isEventHandled() {
	return received_;
    }

    public boolean isError() {
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
	connected_ = false;
    }

}// AnyPullReceiver
