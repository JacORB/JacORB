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

class StructuredSender extends Thread implements StructuredPushSupplierOperations {
    Logger logger_ = Logger.getLogger("StructuredEventChannelTest.Sender");
    StructuredProxyPushConsumer pushConsumer_;
    StructuredEvent event_;
    int times_ = 1;
    boolean error_ = false;

    StructuredSender(StructuredEvent event) {
	event_ = event;
    }

    StructuredSender(StructuredEvent event, int times) {
	event_ = event;
	times_ = times;
    }

    public void run() {
	for (int x=0; x<times_; ++x) {
	    try {
		pushConsumer_.push_structured_event(event_);
	    } catch (Exception e) {
		logger_.info(e);
		error_ = true;
	    }
	}
    }

    public void disconnect_structured_push_supplier() {
    }

    public void subscription_change(EventType[] eventType, EventType[] eventType2) throws InvalidEventType {
    }

    void connect(POA poa, EventChannel channel) throws Exception {
	StructuredPushSupplierPOATie senderTie = new StructuredPushSupplierPOATie(this);
	SupplierAdmin supplierAdmin = channel.default_supplier_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	
	pushConsumer_ = StructuredProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT, _proxyIdHolder));
	    
	pushConsumer_.connect_structured_push_supplier(StructuredPushSupplierHelper.narrow(poa.servant_to_reference(senderTie)));
    }

}
