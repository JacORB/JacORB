package org.jacorb.notification;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.StructuredPullConsumerPOA;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyComm.StructuredPullConsumerOperations;
import org.omg.CosNotifyComm.StructuredPullConsumerPOATie;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyComm.StructuredPullSupplier;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplier;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierHelper;
import org.omg.CosNotifyComm.StructuredPullConsumerHelper;
import org.omg.CORBA.BooleanHolder;
import org.apache.log4j.Logger;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.AlreadyConnected;

class StructuredPullReceiver extends Thread implements StructuredPullConsumerOperations, TestClientOperations {

    StructuredEvent event_ = null;
    ORB orb_;
    POA poa_;
    boolean connected_;
    StructuredProxyPullSupplier pullSupplier_;
    Logger logger_;
    boolean received_;
    long TIMEOUT = 1000;
    boolean error_;

    StructuredPullReceiver() {
	super();
	logger_ = Logger.getLogger("TEST.StructuredPullReceiver");
	//	supplier.
    }

    public boolean isConnected() {
	return connected_;
    }
    
    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected {
	StructuredPullConsumerPOATie _receiverTie = new StructuredPullConsumerPOATie(this);
	ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
	IntHolder _proxyId = new IntHolder();
	pullSupplier_ = StructuredProxyPullSupplierHelper.narrow(_consumerAdmin.obtain_notification_pull_supplier(ClientType.STRUCTURED_EVENT, _proxyId));
	pullSupplier_.connect_structured_pull_consumer(StructuredPullConsumerHelper.narrow(_receiverTie._this(orb)));
	connected_ = true;
    }

    public void shutdown() {
	pullSupplier_.disconnect_structured_pull_supplier();
	pullSupplier_ = null;
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
	    while (true) {
		event_ = pullSupplier_.try_pull_structured_event(_success);
	    
		if (_success.value) {
		    logger_.debug("Received Event");
		    received_ = true;
		    break;
		} 

		if (System.currentTimeMillis() < _startTime + TIMEOUT) {
		    Thread.yield();
		} else {
		    logger_.debug("Timeout");
		    received_ = false;
		    break;
		}
	    }
	} catch (Disconnected d) {
	    d.printStackTrace();
	    error_ = true;
	}
    }

    public void push_structured_event(StructuredEvent event) {
	event_ = event;
	synchronized(this) {
	    notifyAll();
	}
    }

    public void disconnect_structured_pull_consumer() {
	connected_ = false;
    }

    public void offer_change(EventType[] e1, EventType[] e2) {
    }

}
