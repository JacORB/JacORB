package org.jacorb.notification;

import org.omg.CosEventComm.PullSupplierPOA;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventComm.Disconnected;



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

public class CosEventPullSender extends PullSupplierPOA {
    
    Any event_;
    Any invalidAny_;
    ProxyPullConsumer myConsumer_;

    public CosEventPullSender(ORB orb, ProxyPullConsumer myConsumer) throws Exception {
	myConsumer.connect_pull_supplier(_this(orb));
	myConsumer_ = myConsumer;
	invalidAny_ = orb.create_any();
    }
    
    void send(Any event) {
	synchronized(this) {
	    event_ = event;
	}
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
		}
	    }
	}
	return _event;
    }
    
    public void disconnect_pull_supplier() {
    }

}// CosEventPullSender
