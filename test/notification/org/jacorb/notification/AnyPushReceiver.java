package org.jacorb.notification;

import org.omg.CosNotifyComm.PushConsumerPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplier;
import org.apache.log4j.Logger;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import EDU.oswego.cs.dl.util.concurrent.Barrier;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

public class AnyPushReceiver extends PushConsumerPOA implements Runnable, TestClientOperations {
    Any event_ = null;
    ORB orb_;
    POA poa_;
    long receiveTime_;
    CyclicBarrier barrier_;
    Logger logger_ = Logger.getLogger("TEST.AnyPushReceiver");
    Logger timeLogger_ = Logger.getLogger("TIME.AnyPushReceiver");
    ProxyPushSupplier mySupplier_;
    PerformanceListener perfListener_;

    boolean connected_;
    int expected_;
    int received_ = 0;
    long TIMEOUT = 3000L;
    long TIMEOUT_OFF = 0;
    int filterId_ = Integer.MIN_VALUE;

    AnyPushReceiver() {
    }

    AnyPushReceiver(PerformanceListener listener, int runs) {
	perfListener_=listener;
	expected_ = runs;
    }

    public void setFilter(Filter filter) {
	filterId_ = mySupplier_.add_filter(filter);
    }

    public boolean isEventHandled() {
	if (expected_ > 0) {
	    return received_ == expected_;
	} else {
	    return received_ > 0;
	}
    }

    public void setTimeOut(long timeout) {
	TIMEOUT = timeout;
    }

    public void setBarrier(CyclicBarrier barrier) {
	barrier_ = barrier;
    }

    public void shutdown() throws FilterNotFound {
	if (filterId_ != Integer.MIN_VALUE) {
	    mySupplier_.remove_filter(filterId_);
	}
	mySupplier_.disconnect_push_supplier();
    }

    public void connect(ORB orb, POA poa, EventChannel channel) 
	throws AdminLimitExceeded, 
	       TypeError, 
	       AlreadyConnected {

	logger_.debug("connect");
	IntHolder _proxyId = new IntHolder();
	
	logger_.debug("get consumer admin");
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();

	logger_.debug("get proxy push supplier");
	mySupplier_ = 
	    ProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.ANY_EVENT, _proxyId));
	logger_.debug("Call connect");
	mySupplier_.connect_any_push_consumer(_this(orb));

	connected_ = true;
    }

    public void run() {
	if (!isEventHandled()) {
	    try {
		synchronized(this) {
		    wait(TIMEOUT);
		}
	    } catch (InterruptedException e) {}
	}

	if (barrier_ != null) {
	    try {
		barrier_.barrier();
	    } catch (InterruptedException ie) {}
	}
    }

    public void push(Any any) throws Disconnected {
	received_++;
	if (perfListener_!= null) {
	    perfListener_.eventReceived(any, System.currentTimeMillis());
	}

	if (received_ == expected_) {
	    synchronized(this) {
		notifyAll();
	    }
	}
    }

    public long calcTotalTime(long start) {
	return (receiveTime_ - start);
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isError() {
	return false;
    }

    public void disconnect_push_consumer() {
	logger_.debug("disconnect");

	connected_ = false;;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {}
}
