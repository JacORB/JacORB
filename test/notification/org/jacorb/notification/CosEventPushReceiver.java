package org.jacorb.notification;

import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushConsumerPOA;
import org.omg.CosEventComm.Disconnected;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.apache.log4j.Logger;

/**
 * CosEventPushReceiver.java
 *
 *
 * Created: Fri Nov 22 18:21:29 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class CosEventPushReceiver extends PushConsumerPOA implements Runnable, TestClientOperations {

    Any event_;
    long TIMEOUT = 2000;
    boolean received_ = false;
    boolean connected_;
    ProxyPushSupplier mySupplier_;
    Logger logger_ = Logger.getLogger("TEST.EventReceiver");
    
    public void push(Any event) throws Disconnected {
	logger_.debug("push()");
	synchronized(this) {
	    event_ = event;
	    notifyAll();
	}
    }

    public void disconnect_push_consumer() {
	connected_ = false;
    }

    public void run() {
	if (event_ == null) {
	    synchronized(this) {
		try {
		    wait(TIMEOUT);
		} catch (InterruptedException e) {}
	    }
	}

	if (event_ != null) {
	    received_ = true;
	}
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

    public void connect(ORB orb, POA poa, org.omg.CosNotifyChannelAdmin.EventChannel channel) throws AlreadyConnected, TypeError {
	EventChannel _channel = EventChannelHelper.narrow(channel);
	ConsumerAdmin _admin = _channel.for_consumers();
	mySupplier_ = _admin.obtain_push_supplier();
	mySupplier_.connect_push_consumer(_this(orb));
	connected_ = true;
    }

    public void shutdown() {
	mySupplier_.disconnect_push_supplier();
    }
}// CosEventPushReceiver
