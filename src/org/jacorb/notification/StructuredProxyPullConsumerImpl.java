package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.SequencePullSupplier;
import org.omg.CosNotifyComm.StructuredPullConsumerOperations;
import org.omg.CosNotifyComm.StructuredPullSupplier;

/**
 * StructuredProxyPullConsumerImpl.java
 *
 *
 * Created: Mon Nov 04 01:27:01 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPullConsumerImpl extends ProxyBase 
    implements StructuredProxyPullConsumerOperations,
	       Runnable {

    private StructuredPullSupplier mySupplier_;
    protected long pollInterval_ = 1000L;
    protected boolean active_ = true;
    protected Thread thisThread_;

    public StructuredProxyPullConsumerImpl(ApplicationContext appContext,
					   ChannelContext channelContext, 
					   SupplierAdminTieImpl supplierAdminServant, 
					   SupplierAdmin supplierAdmin,
					   Integer key) {
	super(supplierAdminServant,
	      appContext, 
	      channelContext,
	      key,
	      Logger.getLogger("Proxy.StructuredPullConsumer"));
    }
    
    // Implementation of
    // org.omg.CosNotifyComm.StructuredPullConsumerOperations

    public void disconnect_structured_pull_consumer() {
	dispose();
    }

    
    // Implementation of org.omg.CosNotifyChannelAdmin.StructuredProxyPullConsumerOperations

    public void connect_structured_pull_supplier(StructuredPullSupplier structuredPullSupplier) 
	throws AlreadyConnected {

	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	active_ = true;

	mySupplier_ = structuredPullSupplier;
	thisThread_ = new Thread(this);
	thisThread_.start();
    }


    synchronized public void suspend_connection() throws NotConnected, ConnectionAlreadyInactive {
	if (!connected_) {
	    throw new NotConnected();
	}
	if (!active_) {
	    throw new ConnectionAlreadyInactive();
	}
	active_ = false;
	thisThread_.interrupt();
	try {
	    thisThread_.join();
	} catch (InterruptedException e) {}
	thisThread_ = null;
    }

    synchronized public void resume_connection() throws ConnectionAlreadyActive, NotConnected {
	if (!connected_) {
	    throw new NotConnected();
	}
	if (active_) {
	    throw new ConnectionAlreadyActive();
	}
	active_ = true;
	thisThread_ = new Thread(this);
	thisThread_.start();
    }

    public SupplierAdmin MyAdmin() {
	return (SupplierAdmin)myAdmin_.getThisRef();
    }

    public EventType[] obtain_subscription_types(ObtainInfoMode obtainInfoMode) {
	return null;
    }

    public void validate_event_qos(Property[] property1, 
				   NamedPropertyRangeSeqHolder namedPropertyRangeSeqHolder) throws UnsupportedQoS {
	
    }

    public void run() {
	runStructured();
    }


    public void runStructured() {
	BooleanHolder _hasEvent = new BooleanHolder();
	StructuredEvent _event = null;
	synchronized(this) {
	    while(connected_ && active_) {
		try {
		    _hasEvent.value = false;
		    _event = mySupplier_.try_pull_structured_event(_hasEvent);
		} catch (UserException ex) {
		    connected_ = false;
		    return;
		} catch (SystemException sysEx) {
		    connected_ = false;
		    return;
		}

		if (_hasEvent.value) {
		    logger_.debug("pulled Event");
		    NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(_event, this);
		    channelContext_.getEventChannelServant().dispatchEvent(_notifyEvent);
		}
		
		try {
		    Thread.sleep(pollInterval_);
		} catch (InterruptedException ie) {}
	    }
	}
    }

    public List getSubsequentDestinations() {
	return Collections.singletonList(myAdmin_);
    }
    
    public EventDispatcher getEventDispatcher() {
	return null;
    }
    
    public boolean hasEventDispatcher() {
	return false;
    }

    protected void disconnectClient() {
	if (connected_) {
	    if (myAdmin_ != null) {
		mySupplier_.disconnect_structured_pull_supplier();
		mySupplier_ = null;
	    }
	}
	connected_ = false;
    }

    public void dispose() {
	super.dispose();
	disconnectClient();
    }
}// StructuredProxyPullConsumerImpl
