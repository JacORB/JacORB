package org.jacorb.notification;

import org.omg.CosEventComm.PushSupplierPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;

/**
 * CosEventPushSender.java
 *
 *
 * Created: Fri Nov 22 18:46:42 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class CosEventPushSender extends PushSupplierPOA implements TestClientOperations, Runnable {

    boolean connected_;
    boolean sent_;
    boolean error_;
    ProxyPushConsumer myConsumer_;
    Any event_;

    CosEventPushSender(Any event) {
	event_ = event;
    }

    public void disconnect_push_supplier() {
	connected_ = false;
    }

    public boolean isConnected() {
	return connected_;
    }

    public boolean isEventHandled() {
	return sent_;
    }

    public boolean isError() {
	return error_;
    }

    public void connect(ORB orb, POA poa, org.omg.CosNotifyChannelAdmin.EventChannel channel) throws AlreadyConnected {
	EventChannel _channel = EventChannelHelper.narrow(channel);
	SupplierAdmin _admin = _channel.for_suppliers();
	myConsumer_ = _admin.obtain_push_consumer();
	myConsumer_.connect_push_supplier(_this(orb));
	connected_ = true;
    }
    public void shutdown() {
	myConsumer_.disconnect_push_consumer();
    }

    // Implementation of java.lang.Runnable

    /**
     * Describe <code>run</code> method here.
     *
     */
    public void run() {
	try {
	    myConsumer_.push(event_);
	    event_ = null;
	    sent_ = true;
	} catch (Disconnected d) {
	    error_ = true;
	}
    }

}// CosEventPushSender
