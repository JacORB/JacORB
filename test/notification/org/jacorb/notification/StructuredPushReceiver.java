package org.jacorb.notification;

import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.apache.log4j.Logger;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplier;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyComm.StructuredPushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyComm.StructuredPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.Filter;

class StructuredPushReceiver extends Thread 
    implements StructuredPushConsumerOperations, 
	       TestClientOperations {

    StructuredProxyPushSupplier pushSupplier_;

    int received_ = 0;
    int expected_ = 0;
    int filterId_;

    long timeout_ = 1000;

    CyclicBarrier barrier_;

    boolean connected_ = false;
    PerformanceLogger perfListener_;

    StructuredPushReceiver() {
    }

    StructuredPushReceiver(PerformanceLogger perfListener, int expected) {
	perfListener_ = perfListener;
	expected_ = expected;
    }

    public void setBarrier(CyclicBarrier barrier) {
	barrier_ = barrier;
    }
    
    public void setFilter(Filter filter) {
	filterId_ = pushSupplier_.add_filter(filter);
    }

    public void setTimeOut(long timeout) {
	timeout_ = timeout;
    }

    public void run() {
	if (!isEventHandled()) {
	    synchronized(this) {
		try {
		    wait(timeout_);
		} catch (InterruptedException e) {
		}
	    }
	}

	if (barrier_ != null) {
	    try {
		barrier_.barrier();
	    } catch (InterruptedException ie) {}
	}
    }

    public void push_structured_event(StructuredEvent event) throws Disconnected {
	received_++;

	if (perfListener_ != null) {
	    perfListener_.eventReceived(event, System.currentTimeMillis());
	}

	if (received_ == expected_) {
	    synchronized(this) {
		notifyAll();
	    }
	}
    }

    public void disconnect_structured_push_consumer() {
	connected_ = false;
    }

    public void offer_change(EventType[] type1, EventType[] type2) throws InvalidEventType {
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected, TypeError {
	StructuredPushConsumerPOATie receiverTie = new StructuredPushConsumerPOATie(this);
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	pushSupplier_ = StructuredProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, _proxyIdHolder));
	pushSupplier_.connect_structured_push_consumer(StructuredPushConsumerHelper.narrow(receiverTie._this(orb)));
	connected_ = true;
    }

    public boolean isEventHandled() {
	if (expected_ > 0) {
	    return received_ == expected_;
	} else {
	    return received_ > 0;
	}
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isError() {
	return false;
    }

    public void shutdown() throws FilterNotFound {
	if (filterId_ != Integer.MIN_VALUE) {
	    pushSupplier_.remove_filter(filterId_);
	}
	pushSupplier_.disconnect_structured_push_supplier();
	
    }
}
