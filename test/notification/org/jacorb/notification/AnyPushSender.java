package org.jacorb.notification;

import org.omg.CosNotifyComm.PushSupplierPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.apache.log4j.Logger;

class AnyPushSender extends PushSupplierPOA implements TestClientOperations {

    ORB orb_;
    POA poa_;
    ProxyPushConsumer myConsumer_;
    boolean connected_;
    Any event_;
    boolean error_;
    Logger timeLogger_ = Logger.getLogger("TIME.AnyPushSender");
    long sendTime_;
    TestEventGenerator generator_;
    PerformanceListener perfListener_;
    int runs_;
    long interval_;
    Logger logger_ = Logger.getLogger("TEST.AnyPushSender");

    AnyPushSender(Any event) throws AlreadyConnected {
	event_ = event;
    }

    AnyPushSender(PerformanceListener perfListener, TestEventGenerator generator, int runs, long interval) {
	generator_ = generator;
	perfListener_ = perfListener;
	runs_ = runs;
	interval_ = interval;
    }

    public void disconnect_push_supplier() {
	connected_ = false;
    }
    
    public boolean isConnected() {
	return connected_;
    }

    public boolean isError() {
	return false;
    }

    public boolean isEventHandled() {
	return true;
    }

    public void subscription_change(EventType[] e1, EventType[] e2) {}

    public void run() {
	if (event_ != null) {
	    singleSend();
	} else {
	    multSend();
	}
    }
    
    public void multSend() {
	for (int x=0; x<runs_; ++x) {
	    Any _event = generator_.getNextEvent();
	    try {
		long _start = System.currentTimeMillis();
		myConsumer_.push(_event);
		long _stop = System.currentTimeMillis();
		perfListener_.eventSent(_event, System.currentTimeMillis(), _stop - _start);
		try {
		    Thread.sleep(interval_);
		} catch (InterruptedException ie) {}
	    } catch (Exception e) {
		if (perfListener_!=null) {
		    perfListener_.eventFailed(_event, e);
		}
	    }
	}
    }

    public void singleSend() {
	long _time = System.currentTimeMillis();
	try {
	    myConsumer_.push(event_);
	    sendTime_ = System.currentTimeMillis();

	} catch (Disconnected d) {
	    error_ = true;
	}
	timeLogger_.info("push(): " + (System.currentTimeMillis() - _time));
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected {
	IntHolder _proxyId = new IntHolder();
	SupplierAdmin _supplierAdmin = channel.default_supplier_admin();
	myConsumer_= ProxyPushConsumerHelper.narrow(_supplierAdmin.obtain_notification_push_consumer(ClientType.ANY_EVENT, _proxyId));
	myConsumer_.connect_any_push_supplier(_this(orb));
	connected_ = true;
    }

    public void shutdown() {
	myConsumer_.disconnect_push_consumer();
    }

}
