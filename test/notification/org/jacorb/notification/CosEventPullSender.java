package org.jacorb.notification;

import org.omg.CosEventComm.PullSupplierPOA;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventComm.Disconnected;
import org.omg.PortableServer.POA;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import EDU.oswego.cs.dl.util.concurrent.Latch;

/*
 *        JacORB - a free Java ORB
 */

/**
 * CosEventPullSender.java
 *
 *
 * Created: Tue Nov 26 11:26:11 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class CosEventPullSender extends PullSupplierPOA implements TestClientOperations, Runnable {
    
    Any event_;
    Any invalidAny_;
    ProxyPullConsumer myConsumer_;
    Latch latch_ = new Latch();
    private boolean connected_;
    private boolean error_;
    private boolean sent_;

    CosEventPullSender(Any event) {
	event_ = event;
    }
    
    public void run() {
	try {
	    latch_.acquire();
	} catch (InterruptedException ie) {}
    }

    public Any pull() throws Disconnected {
	BooleanHolder _b = new BooleanHolder();
	Any _event;
	while(true) {
	    _event = try_pull(_b);
	    if(_b.value) {
		return _event;
	    }
	    Thread.yield();
	}
    }
    
    public Any try_pull(BooleanHolder success) throws Disconnected {
	Any _event = invalidAny_;
	success.value = false;
	if (event_ != null) {
	    synchronized(this) {
		if (event_ != null) {
		    _event = event_;
		    event_ = null;
		    success.value = true;
		    sent_ = true;
		    latch_.release();
		}
	    }
	}
	return _event;
    }
    
    public void disconnect_pull_supplier() {
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

    public void shutdown() {
	myConsumer_.disconnect_pull_consumer();
    }

    public void connect(ORB orb, POA poa, org.omg.CosNotifyChannelAdmin.EventChannel channel) 
	throws AlreadyConnected, TypeError {
	invalidAny_ = orb.create_any();
	EventChannel _channel = EventChannelHelper.narrow(channel);
	SupplierAdmin _admin = _channel.for_suppliers();
	myConsumer_ = _admin.obtain_pull_consumer();
	myConsumer_.connect_pull_supplier(_this(orb));
	connected_ = true;
    }

}// CosEventPullSender
