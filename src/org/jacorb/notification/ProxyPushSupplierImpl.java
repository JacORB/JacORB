package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import org.apache.log.Logger;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierOperations;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierPOA;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierPOA;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.PushConsumer;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import java.util.List;
import java.util.Collections;
import org.omg.CORBA.BAD_PARAM;
import org.jacorb.notification.interfaces.EventConsumer;
import java.util.LinkedList;
import java.util.Iterator;
import org.omg.CORBA.Any;
import org.omg.PortableServer.Servant;
import org.omg.CosNotifyChannelAdmin.ProxyPushSupplierPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPushSupplierImpl 
    extends ProxyBase 
    implements ProxyPushSupplierOperations,
	       org.omg.CosEventChannelAdmin.ProxyPushSupplierOperations,
	       EventConsumer {

    private org.omg.CosEventComm.PushConsumer myPushConsumer_;
    private boolean connected_;
    private boolean enabled_;
    private boolean active_;
    private ConsumerAdminTieImpl myAdminServant_;
    private List pendingEvents_;

    ProxyPushSupplierImpl(ConsumerAdminTieImpl myAdminServant,
			  ApplicationContext appContext,
			  ChannelContext channelContext,
			  PropertyManager adminProperties,
			  PropertyManager qosProperties,
			  Integer key) {
	
	super(myAdminServant, 
	      appContext, 
	      channelContext, 
	      adminProperties,
	      qosProperties,
	      key);

	init(myAdminServant);
    }

    ProxyPushSupplierImpl(ConsumerAdminTieImpl myAdminServant,
			  ApplicationContext appContext,
			  ChannelContext channelContext,
			  PropertyManager adminProperties,
			  PropertyManager qosProperties) {
	
	super(myAdminServant, 
	      appContext, 
	      channelContext, 
	      adminProperties,
	      qosProperties);

	init(myAdminServant);
    }

    void init(ConsumerAdminTieImpl myAdminServant) {
	myAdminServant_ = myAdminServant;
	connected_ = false;
	enabled_ = true;
	pendingEvents_ = new LinkedList();
    }

    public String toString() {
	return "<ProxyPushSupplier connected: " + connected_ + ">";
    }

    public void disconnect_push_supplier() {
	dispose();
    }
    
    private void disconnectClient() {
	if (myPushConsumer_ != null) {
	    logger_.debug("disconnect");
	    myPushConsumer_.disconnect_push_consumer();
	    myPushConsumer_ = null;
	    connected_ = false;
	}
    }

    public void deliverEvent(NotificationEvent event){
	if (connected_) {
	    try {
		if (active_ && enabled_) {
		    logger_.debug("pre push");
		    myPushConsumer_.push(event.toAny());
		    logger_.debug("pushed any to consumer");
		} else {
		    pendingEvents_.add(event.toAny());
		    logger_.debug("added to pendingEventS");
		}
	    } catch(Disconnected e) {
		connected_ = false;
		logger_.debug("push failed: Not connected");
	    }
	} else {
	    logger_.debug("Not connected");
	}
    }

    public void connect_push_consumer(org.omg.CosEventComm.PushConsumer pushConsumer) throws AlreadyConnected {
	connect_any_push_consumer(pushConsumer);
    }

    public void connect_any_push_consumer(org.omg.CosEventComm.PushConsumer pushConsumer)
	throws AlreadyConnected {
	
	if (connected_) { 
	    throw new AlreadyConnected(); 
	}

	if (pushConsumer == null) { 
	    throw new BAD_PARAM(); 
	}

	myPushConsumer_ = pushConsumer;
	connected_ = true;
	active_ = true;
    }

    public ConsumerAdmin MyAdmin() {
	return (ConsumerAdmin)myAdmin_.getThisRef();
    }

    public List getSubsequentFilterStages() {
	return Collections.singletonList(this);
    }

    public EventConsumer getEventConsumer() {
	return this;
    }

    public boolean hasEventConsumer() {
	return true;
    }

    synchronized public void suspend_connection() throws NotConnected, ConnectionAlreadyInactive {
	if (!connected_) {
	    throw new NotConnected();
	}

	if (!active_) {
	    throw new ConnectionAlreadyInactive();
	}
	active_ = false;
    }

    public void deliverPendingEvents() throws NotConnected {
	if (!pendingEvents_.isEmpty()) {
	    Iterator _i = pendingEvents_.iterator();
	    while (_i.hasNext()) {
		try {
		    myPushConsumer_.push((Any)_i.next());
		} catch (Disconnected e) {
		    connected_ = false;
		    throw new NotConnected();
		}
	    }
	}
    }

    synchronized public void resume_connection() throws NotConnected, ConnectionAlreadyActive {
	if (!connected_) {
	    throw new NotConnected();
	}

	if (active_) {
	    throw new ConnectionAlreadyActive();
	}

	deliverPendingEvents();
	active_ = true;
    }

    synchronized public void dispose() {
	super.dispose();
	disconnectClient();
    }

    synchronized public void enableDelivery() {
	enabled_ = true;
    }

    synchronized public void disableDelivery() {
	enabled_ = false;
    }

    public Servant getServant() {
	if (thisServant_ == null) {
	    synchronized(this) {
		if (thisServant_ == null) {
		    thisServant_ = new ProxyPushSupplierPOATie(this);
		}
	    }
	}
	return thisServant_;
    }

    public void setServant(Servant servant) {
	thisServant_ = servant;
    }

    public boolean hasPendingEvents() {
	return !pendingEvents_.isEmpty();
    }

}
