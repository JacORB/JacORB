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

import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierOperations;
import org.apache.log.Logger;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.PullConsumer;
import org.omg.PortableServer.POA;
import org.omg.CORBA.BooleanHolder;
import org.jacorb.notification.interfaces.EventConsumer;
import org.omg.CosEventComm.Disconnected;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import org.omg.PortableServer.Servant;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierPOATie;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullSupplierImpl
    extends ProxyBase
    implements ProxyPullSupplierOperations,
	       org.omg.CosEventChannelAdmin.ProxyPullSupplierOperations,
	       EventConsumer {

    private PullConsumer pullConsumer_ = null;
    private boolean connected_ = false;
    private LinkedList pendingEvents_ = new LinkedList();
    private int maxListSize_ = 200;
    private static Any sUndefinedAny = null;

    ProxyPullSupplierImpl(ConsumerAdminTieImpl adminServant,
			  ApplicationContext appContext,
			  ChannelContext channelContext,
			  PropertyManager adminProperties,
			  PropertyManager qosProperties) {

	super(adminServant, 
	      appContext,
	      channelContext, 
	      adminProperties,
	      qosProperties);

	init(appContext);
    }

    ProxyPullSupplierImpl(ConsumerAdminTieImpl adminServant,
			  ApplicationContext appContext,
			  ChannelContext channelContext,
			  PropertyManager adminProperties,
			  PropertyManager qosProperties,
			  Integer key) {

	super(adminServant, 
	      appContext ,
	      channelContext, 
	      adminProperties,
	      qosProperties,
	      key);

	init(appContext);
    }

    private Any getUndefinedAny() {
	if (sUndefinedAny == null) {
	    synchronized(getClass()) {
		if (sUndefinedAny == null) {
		    sUndefinedAny = applicationContext_.getOrb().create_any();
		    //    sUndefinedAny.insert_octet((byte)0);
		}
	    }
	}
	return sUndefinedAny;
    }

    private void init(ApplicationContext appContext) {
	logger_.debug("init");

	setProxyType(ProxyType.PULL_ANY);
        connected_ = false;
    }

    public void disconnect_pull_supplier() {
	dispose();
    }

    private void disconnect() {
	if (pullConsumer_ != null) {
	    pullConsumer_.disconnect_pull_consumer();
	    pullConsumer_ = null;
	}
    }

    public Any pull()
        throws Disconnected {
        Any _event = null;

	if (!connected_) {
	    throw new Disconnected();
	}

	synchronized(pendingEvents_) {
	    while (pendingEvents_.isEmpty()) {
		try {
		    pendingEvents_.wait();
		    _event = (Any)pendingEvents_.getFirst();
		    pendingEvents_.remove(_event);
		} catch (InterruptedException e) {}
	    }
	}
	return _event;
    }

    public Any try_pull (BooleanHolder hasEvent)
        throws Disconnected {

	logger_.debug("try_pull");

        if (!connected_) { 
	    throw new Disconnected(); 
	}

        Any event = getUndefinedAny();
	hasEvent.value = false;

        synchronized(pendingEvents_) {
            if (!pendingEvents_.isEmpty()) {
                event = (Any)pendingEvents_.getFirst();
                pendingEvents_.remove(event);
                hasEvent.value = true;
            }
        }
	
	logger_.debug("try_pull returns: " + event);

	return event;
    }

    /**
     * Deliver Event to the underlying Consumer. As our Consumer is a
     * PullConsumer we simply put the Events in a Queue. The
     * PullConsumer will pull the Events out of the Queue at a later time.
     */
    public void deliverEvent(NotificationEvent event) {
	synchronized(pendingEvents_) {
	    if (pendingEvents_.size() > maxListSize_) {
		pendingEvents_.remove(pendingEvents_.getFirst());
	    }
	    pendingEvents_.add(event.toAny());
	    pendingEvents_.notifyAll();
	}
    }

    public void connect_any_pull_consumer(PullConsumer pullConsumer) throws AlreadyConnected {
	connect_pull_consumer(pullConsumer);
    }

    public void connect_pull_consumer(PullConsumer consumer) throws AlreadyConnected {
	logger_.info("connect_pull_consumer()");

	if (connected_) {
	    throw new AlreadyConnected();
	}

	connected_ = true;
	pullConsumer_ = consumer;
    }

    public ConsumerAdmin MyAdmin() {
	return (ConsumerAdmin)myAdmin_.getThisRef();
    }

    public List getSubsequentFilterStages() {
	return CollectionsWrapper.singletonList(this);
    }

    public EventConsumer getEventConsumer() {
	return this;
    }

    public boolean hasEventConsumer() {
	return true;
    }

    public void dispose() {
	super.dispose();
	disconnect();
	pendingEvents_.clear();
    }

    public void enableDelivery() {
	// as delivery to this PullSupplier causes no cost
	// we can ignore this
    }

    public void disableDelivery() {
	// as delivery to this PullSupplier causes no cost
	// we can ignore this
    }

    public void deliverPendingEvents() {
	// as we cannot actively deliver events we can ignore this
    }

    public Servant getServant() {
	if (thisServant_ == null) {
	    synchronized(this) {
		if (thisServant_ == null) {
		    thisServant_ = new ProxyPullSupplierPOATie(this);
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
