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
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplier;
import org.omg.CosNotifyComm.SequencePushConsumerOperations;
import org.omg.CosNotifyComm.SequencePushConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierHelper;
import org.omg.CosNotifyComm.SequencePushConsumerHelper;

class SequencePushReceiver extends Thread implements SequencePushConsumerOperations, TestClientOperations {
    Logger logger_ = Logger.getLogger("SequenceEventChannelTest.Receiver");
    SequenceProxyPushSupplier pushSupplier_;
    boolean received_ = false;
    boolean connected_ = false;

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

    public void push_structured_events(StructuredEvent[] event) throws Disconnected {
	received_ = true;
	synchronized(this) {
	    notifyAll();
	}
    }

    public void disconnect_sequence_push_consumer() {
	logger_.info("Disconnected!");
	connected_ = false;
    }

    public void offer_change(EventType[] type1, EventType[] type2) throws InvalidEventType {
    }

    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected, TypeError {
	SequencePushConsumerPOATie receiverTie = new SequencePushConsumerPOATie(this);
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
	IntHolder _proxyIdHolder = new IntHolder();
	pushSupplier_ = SequenceProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.SEQUENCE_EVENT, _proxyIdHolder));
	pushSupplier_.connect_sequence_push_consumer(SequencePushConsumerHelper.narrow(receiverTie._this(orb)));
	connected_ = true;
    }

    public boolean isEventHandled() {
	return received_;
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isError() {
	return false;
    }

    public void shutdown() {
	pushSupplier_.disconnect_sequence_push_supplier();
    }
}
