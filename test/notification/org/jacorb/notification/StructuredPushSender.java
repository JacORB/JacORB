package org.jacorb.notification;

import org.omg.CosNotifyComm.StructuredPushSupplierOperations;
import org.apache.log4j.Logger;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerHelper;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;

class StructuredPushSender 
    extends Thread 
    implements StructuredPushSupplierOperations, TestClientOperations {

    Logger logger_ = Logger.getLogger("StructuredEventChannelTest.Sender");
    PerformanceLogger perfListener_;
    StructuredGenerator generator_;
    StructuredProxyPushConsumer pushConsumer_;
    StructuredEvent event_;
    int times_ = 1;
    boolean error_ = false;
    boolean connected_;
    boolean eventSent_;
    long interval_ = 0;

    StructuredPushSender(StructuredEvent event) {
	event_ = event;
    }

    StructuredPushSender(StructuredEvent event, int times) {
	event_ = event;
	times_ = times;
    }

    StructuredPushSender(PerformanceLogger logger, StructuredGenerator generator, int times, long interval) {
	perfListener_ = logger;
	generator_ = generator;
	times_ = times;
	interval_ = interval;
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isEventHandled() {
	return eventSent_;
    }

    public boolean isError() {
	return error_;
    }

    public void run() {
	for (int x=0; x<times_; ++x) {
	    try {
		if (generator_ != null) {
		    pushConsumer_.push_structured_event(generator_.getNextEvent());
		} else {
		    pushConsumer_.push_structured_event(event_);
		}
	    } catch (Exception e) {
		logger_.info(e);
		error_ = true;
	    }
	    try {
		Thread.sleep(interval_);
	    } catch (InterruptedException ie) {}
	}
	eventSent_ = true;
    }


    public void disconnect_structured_push_supplier() {
	connected_ = false;
    }

    public void subscription_change(EventType[] eventType, EventType[] eventType2) throws InvalidEventType {
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected {
	StructuredPushSupplierPOATie senderTie = new StructuredPushSupplierPOATie(this);
	SupplierAdmin supplierAdmin = channel.default_supplier_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	
	pushConsumer_ = 
	    StructuredProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT, _proxyIdHolder));
	    
	pushConsumer_.connect_structured_push_supplier(StructuredPushSupplierHelper.narrow(senderTie._this(orb)));
	connected_ = true;
    }

    public void shutdown() {
	pushConsumer_.disconnect_structured_push_consumer();
    }
}
