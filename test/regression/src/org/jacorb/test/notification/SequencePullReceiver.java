package org.jacorb.test.notification;

import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplier;
import org.omg.CosNotifyComm.SequencePullConsumerOperations;
import org.omg.CosNotifyComm.SequencePullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierHelper;
import org.omg.CosNotifyComm.SequencePullConsumerHelper;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;

class SequencePullReceiver extends Thread implements SequencePullConsumerOperations, TestClientOperations {

    StructuredEvent[] event_ = null;
    boolean connected_;
    SequenceProxyPullSupplier pullSupplier_;
    Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    boolean received_;
    long TIMEOUT = 1000;
    boolean error_;

    SequencePullReceiver() {
	super();
    }

    public boolean isConnected() {
	return connected_;
    }
    
    public void connect(NotificationTestCaseSetup setup,EventChannel channel,boolean useOrSemantic) throws AdminLimitExceeded, AlreadyConnected {
	SequencePullConsumerPOATie _receiverTie = new SequencePullConsumerPOATie(this);
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
	IntHolder _proxyId = new IntHolder();
	pullSupplier_ = SequenceProxyPullSupplierHelper.narrow(_consumerAdmin.obtain_notification_pull_supplier(ClientType.SEQUENCE_EVENT, _proxyId));
	pullSupplier_.connect_sequence_pull_consumer(SequencePullConsumerHelper.narrow(_receiverTie._this(setup.getClientOrb())));
	connected_ = true;
    }

    public boolean isEventHandled() {
	return event_ != null;
    }

    public boolean isError() {
	return false;
    }

    public void run() {
	BooleanHolder _success = new BooleanHolder();
	_success.value = false;
	long _startTime = System.currentTimeMillis();
	logger_.info("start receiver");

	try {
	    event_ = pullSupplier_.pull_structured_events(1);
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

    public void push_structured_events(StructuredEvent[] event) {
	event_ = event;
	synchronized(this) {
	    notifyAll();
	}
    }

    public void disconnect_sequence_pull_consumer() {
	connected_ = false;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }

    public void shutdown() {
	//mySupplier_.d
    }
}
