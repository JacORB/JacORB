package org.jacorb.notification;

import org.omg.CosNotifyComm.StructuredPullSupplierOperations;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.NotifySubscribeOperations;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotifyComm.StructuredPullSupplierOperations;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumer;
import org.omg.CosNotifyComm.StructuredPullSupplierPOATie;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyComm.StructuredPullSupplierHelper;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerHelper;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;

/*
 *        JacORB - a free Java ORB
 */

/**
 * StructuredPullSender.java
 *
 *
 * Created: Sun Dec 08 15:13:41 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredPullSender extends Thread  implements StructuredPullSupplierOperations, TestClientOperations {

    StructuredEvent event_;
    StructuredProxyPullConsumer pullConsumer_;
    private boolean error_;
    ORB orb_;
    boolean connected_;
    boolean eventHandled_;

    public boolean isError() {
	return error_;
    }

    public boolean isEventHandled() {
	return eventHandled_;
    }

    public StructuredPullSender(StructuredEvent event) {
	event_ = event;
    }

    // Implementation of org.omg.CosNotifyComm.NotifySubscribeOperations

    /**
     * Describe <code>subscription_change</code> method here.
     *
     * @param eventType an <code>EventType[]</code> value
     * @param eventType an <code>EventType[]</code> value
     * @exception InvalidEventType if an error occurs
     */
    public void subscription_change(EventType[] eventType1, EventType[] eventType2) throws InvalidEventType {
	
    }
    
    // Implementation of org.omg.CosNotifyComm.StructuredPullSupplierOperations

    /**
     * Describe <code>pull_structured_event</code> method here.
     *
     * @return a <code>StructuredEvent</code> value
     * @exception Disconnected if an error occurs
     */
    public StructuredEvent pull_structured_event() throws Disconnected {
	BooleanHolder _success = new BooleanHolder();
	StructuredEvent _event;
	while(true) {
	    _event = try_pull_structured_event(_success);
	    if(_success.value) {
		return _event;
	    } 
	    Thread.yield();
	}
    }

    /**
     * Describe <code>try_pull_structured_event</code> method here.
     *
     * @param booleanHolder a <code>BooleanHolder</code> value
     * @return a <code>StructuredEvent</code> value
     * @exception Disconnected if an error occurs
     */
    public StructuredEvent try_pull_structured_event(BooleanHolder booleanHolder) throws Disconnected {
	booleanHolder.value = false;
	StructuredEvent _result = TestUtils.getInvalidStructuredEvent(orb_);
	if (event_ != null) {
	    synchronized(this) {
		if (event_ != null) {
		    _result = event_;
		    event_ = null;
		    booleanHolder.value = true;
		    eventHandled_ = true;
		}
	    }
	}
	return _result;
    }

    /**
     * Describe <code>disconnect_structured_pull_supplier</code> method
     * here.
     *
     */
    public void disconnect_structured_pull_supplier() {	
	connected_ = false;
    }
    
    public void connect(ORB orb, POA poa, EventChannel channel) throws AdminLimitExceeded, AlreadyConnected, TypeError {
	orb_ = orb;
	StructuredPullSupplierPOATie _senderTie = new StructuredPullSupplierPOATie(this);
	SupplierAdmin _supplierAdmin = channel.default_supplier_admin();
	IntHolder _proxyId = new IntHolder();
	pullConsumer_ = StructuredProxyPullConsumerHelper.narrow(_supplierAdmin.obtain_notification_pull_consumer(ClientType.STRUCTURED_EVENT, _proxyId));
	pullConsumer_.connect_structured_pull_supplier(StructuredPullSupplierHelper.narrow(_senderTie._this(orb)));
	connected_ = true;
    }

    public void shutdown() {
	pullConsumer_.disconnect_structured_pull_consumer();
    }

    public boolean isConnected() {
	return connected_;
    }

}// StructuredPullSender
