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



class StructuredReceiver extends Thread implements StructuredPushConsumerOperations {
    Logger logger_ = Logger.getLogger("StructuredEventChannelTest.Receiver");
    StructuredProxyPushSupplier pushSupplier_;

    boolean received_ = false;

    public void run() {
	if (!received_) {
	    synchronized(this) {
		try {
		    wait(1000);
		} catch (InterruptedException e) {
		}
	    }
	}
    }

    public void push_structured_event(StructuredEvent event) throws Disconnected {
	received_ = true;
	synchronized(this) {
	    notifyAll();
	}
    }

    public void disconnect_structured_push_consumer() {
	logger_.info("Disconnected!");
    }

    public void offer_change(EventType[] type1, EventType[] type2) throws InvalidEventType {
    }

    void connect(POA poa, EventChannel channel) throws Exception {
	StructuredPushConsumerPOATie receiverTie = new StructuredPushConsumerPOATie(this);
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	pushSupplier_ = StructuredProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.STRUCTURED_EVENT, _proxyIdHolder));
	pushSupplier_.connect_structured_push_consumer(StructuredPushConsumerHelper.narrow(poa.servant_to_reference(receiverTie)));
    }

    boolean received() {
	return received_;
    }
}
