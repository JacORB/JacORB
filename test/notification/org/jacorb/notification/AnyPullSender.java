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
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;

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

public class AnyPullSender extends PullSupplierPOA implements TestClientOperations {
    Logger logger_ = Logger.getLogger("TEST.AnyPullSender");

    Any event_;
    Any invalidAny_;
    ProxyPullConsumer myConsumer_;
    boolean connected_ = false;
    boolean available_ = false;

    public AnyPullSender(Any event) {
	event_ = event;
    }
    
    void reset() {
	event_ = null;
    }

    public boolean isConnected() {
	return connected_;
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected, TypeError {
	IntHolder _proxyId = new IntHolder();

	SupplierAdmin _supplierAdmin = channel.default_supplier_admin();

	myConsumer_ = 
	    ProxyPullConsumerHelper.narrow(_supplierAdmin.obtain_notification_pull_consumer(ClientType.ANY_EVENT, _proxyId));
	myConsumer_.connect_any_pull_supplier(_this(orb));
	connected_ = true;
    }

    public void shutdown() {
	myConsumer_.disconnect_pull_consumer();
    }

    public void run() {
	available_ = true;
    }

    public boolean isEventHandled() {
	return sent_;
    }

    boolean sent_ = false;

    public boolean isError() {
	return false;
    }

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
	if (available_) {
	    synchronized(this) {
		if (available_) {
		    _event = event_;
		    event_ = null;
		    success.value = true;
		    logger_.debug("try_pull will be successful");
		    sent_ = true;
		    available_ = false;
		}
	    }
	}
	return _event;
    }

    public void disconnect_pull_supplier() {
	connected_ = false;
    }

}// AnyPullSender
