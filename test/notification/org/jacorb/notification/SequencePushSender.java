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
import org.omg.CosNotifyComm.SequencePushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumer;
import org.omg.CosNotifyComm.SequencePushSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerHelper;
import org.omg.CosNotifyComm.SequencePushSupplierHelper;

class SequencePushSender 
    extends Thread 
    implements SequencePushSupplierOperations, TestClientOperations {
    Logger logger_ = Logger.getLogger("StructuredEventChannelTest.Sender");
    SequenceProxyPushConsumer pushConsumer_;
    StructuredEvent[] event_;
    int times_ = 1;
    boolean error_ = false;
    boolean connected_;
    boolean eventSent_;

    SequencePushSender(StructuredEvent[] event) {
	event_ = event;
    }

    SequencePushSender(StructuredEvent[] event, int times) {
	event_ = event;
	times_ = times;
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
		pushConsumer_.push_structured_events(event_);
	    } catch (Exception e) {
		logger_.info(e);
		error_ = true;
	    }
	}
	eventSent_ = true;
    }


    public void disconnect_sequence_push_supplier() {
	connected_ = false;
    }

    public void subscription_change(EventType[] eventType, EventType[] eventType2) throws InvalidEventType {
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected {
	SequencePushSupplierPOATie senderTie = new SequencePushSupplierPOATie(this);
	SupplierAdmin supplierAdmin = channel.default_supplier_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	
	pushConsumer_ = SequenceProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.SEQUENCE_EVENT, _proxyIdHolder));
	    
	pushConsumer_.connect_sequence_push_supplier(SequencePushSupplierHelper.narrow(senderTie._this(orb)));
	connected_ = true;
    }

    public void shutdown() {

    }
}
