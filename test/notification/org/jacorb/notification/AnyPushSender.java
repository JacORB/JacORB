package org.jacorb.notification;

import org.omg.CosNotifyComm.PushSupplierPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;

class AnyPushSender extends PushSupplierPOA {
    ORB orb_;
    POA poa_;
    ProxyPushConsumer consumer_;
    boolean disconnectedCalled_;

    AnyPushSender(ORB orb, POA poa, ProxyPushConsumer consumer) throws AlreadyConnected {
	orb_ = orb;
	poa_ = poa;
	consumer_ = consumer;
	consumer.connect_any_push_supplier(this._this(orb_));
    }

    public void disconnect_push_supplier() {
	disconnectedCalled_ = true;
    }
    
    boolean disconnected() {
	return disconnectedCalled_;
    }

    public void subscription_change(EventType[] e1, EventType[] e2) {}

    public void send(Any any) throws Disconnected {
	consumer_.push(any);
    }
}
