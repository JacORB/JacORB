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

import org.apache.log4j.Logger;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierOperations;
import org.omg.CosNotifyComm.StructuredPullConsumer;
import org.omg.PortableServer.POA;
import java.util.LinkedList;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventHeader;
import java.util.Collections;
import java.util.List;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierOperations;
import org.omg.CosNotifyComm.SequencePullConsumer;

/**
 * StructuredProxyPullSupplierImpl.java
 *
 *
 * Created: Tue Nov 05 14:25:49 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPullSupplierImpl 
    extends ProxyBase 
    implements StructuredProxyPullSupplierOperations,
	       EventDispatcher {

    StructuredPullConsumer structuredPullConsumer_;
    LinkedList pendingEvents_ = new LinkedList();
    int maxListSize_ = 200;
    static StructuredEvent undefinedStructuredEvent_;

    public StructuredProxyPullSupplierImpl(ApplicationContext appContext, 
					   ChannelContext channelContext,
					   ConsumerAdminTieImpl myAdminServant, 
					   ConsumerAdmin myAdmin,
					   Integer key) {

	super(myAdminServant, appContext, channelContext, key, Logger.getLogger("Proxy.StructuredProxyPullSupplier"));
	
	if (undefinedStructuredEvent_ == null) {
	    synchronized(getClass()) {
		if (undefinedStructuredEvent_ == null) {
		    undefinedStructuredEvent_ = new StructuredEvent();
		    EventType _type = new EventType();
		    FixedEventHeader _fixed = new FixedEventHeader(_type, "");
		    Property[] _variable = new Property[0];
		    undefinedStructuredEvent_.header = new EventHeader(_fixed, _variable);
		    undefinedStructuredEvent_.filterable_data = new Property[0];
		    undefinedStructuredEvent_.remainder_of_body = orb_.create_any();
		}
	    }
	}
    }
    
    public void connect_structured_pull_consumer(StructuredPullConsumer consumer) throws AlreadyConnected {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	structuredPullConsumer_ = consumer;
    }


    public ConsumerAdmin MyAdmin() {
	return (ConsumerAdmin)myAdmin_.getThisRef();
    }

    public StructuredEvent pull_structured_event() throws Disconnected {
	StructuredEvent _event = null;
	BooleanHolder _hasEvent = new BooleanHolder();
	while(true) {
	    _event = try_pull_structured_event(_hasEvent);
	    if(_hasEvent.value) {
		return _event;
	    }
	    Thread.yield();
	}
    }

    public StructuredEvent try_pull_structured_event(BooleanHolder hasEvent) throws Disconnected {
	if(!connected_) {
	    throw new Disconnected();
	}

	StructuredEvent _event = null;
	synchronized(pendingEvents_) {
	    int _listSize = pendingEvents_.size();
	    if(_listSize > 0) {
		_event = (StructuredEvent)pendingEvents_.getFirst();
		pendingEvents_.remove(_event);
		hasEvent.value = true;
		return _event;
	    } else {
		hasEvent.value = false;
		return undefinedStructuredEvent_;
	    }
	}
    }

    
    public void disconnect_structured_pull_supplier() {
	dispose();
    }

    public void disconnect_sequence_pull_supplier() {
	dispose();
    }

    private void disconnectClient() {
	if (connected_) {
	    if (structuredPullConsumer_ != null) {
		structuredPullConsumer_.disconnect_structured_pull_consumer();
		connected_ = false;
		structuredPullConsumer_ = null;
	    }
	}
    }

    public void dispatchEvent(NotificationEvent event) {
	logger_.debug("dispatchEvent");

	synchronized(pendingEvents_) {
	    if(pendingEvents_.size() > maxListSize_) {
		pendingEvents_.remove(pendingEvents_.getFirst());
	    }
	    pendingEvents_.add(event.toStructuredEvent());
	}
    }

    public List getSubsequentDestinations() {
	return Collections.singletonList(this);
    }
    
    public EventDispatcher getEventDispatcher() {
	return this;
    }

    public boolean hasEventDispatcher() {
	return true;
    }

    public void dispose() {
	super.dispose();
	disconnectClient();
    }

    public void markError() {
	connected_ = false;
    }
}// StructuredProxyPullSupplierImpl
