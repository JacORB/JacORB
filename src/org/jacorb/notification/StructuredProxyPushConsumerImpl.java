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

import org.jacorb.notification.ProxyBase;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.NotifyPublishOperations;
import org.omg.CosNotifyComm.StructuredPushConsumerOperations;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.PortableServer.POA;
import org.apache.log.Logger;
import java.util.List;
import org.jacorb.notification.interfaces.EventConsumer;
import java.util.Collections;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushConsumerOperations;
import org.omg.CosNotifyComm.SequencePushSupplier;
import org.omg.PortableServer.Servant;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerPOATie;

/**
 * StructuredProxyPushConsumerImpl.java
 *
 *
 * Created: Mon Nov 04 01:52:01 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPushConsumerImpl 
    extends ProxyBase 
    implements StructuredProxyPushConsumerOperations {

    private StructuredPushSupplier myPushSupplier_;
    private List subsequentDestinations_;

    public StructuredProxyPushConsumerImpl(SupplierAdminTieImpl supplierAdminServant,
					   ApplicationContext appContext,
					   ChannelContext channelContext,
					   PropertyManager adminProperties,
					   PropertyManager qosProperties,
					   Integer key) {
	super(supplierAdminServant,
	      appContext, 
	      channelContext,
	      adminProperties,
	      qosProperties,
	      key);

	setProxyType(ProxyType.PUSH_STRUCTURED);

	subsequentDestinations_ = Collections.singletonList(myAdmin_);
    }

    public void push_structured_event(StructuredEvent structuredEvent) throws Disconnected {
	if (!connected_) {
	    throw new Disconnected();
	}

	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(structuredEvent, this);
	channelContext_.dispatchEvent(_notifyEvent);
    }

    public void disconnect_structured_push_consumer() {
	dispose();
    }
    
    protected void disconnectClient() {
	if (connected_) {
	    if (myPushSupplier_ != null) {
		connected_ = false;
		myPushSupplier_.disconnect_structured_push_supplier();
		myPushSupplier_ = null;
	    }
	}
    }

    public void connect_structured_push_supplier(StructuredPushSupplier structuredPushSupplier) 
	throws AlreadyConnected {

	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	myPushSupplier_ = structuredPushSupplier;
    }

    
    // Implementation of org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations


    /**
     * Describe <code>MyAdmin</code> method here.
     *
     * @return a <code>SupplierAdmin</code> value
     */
    public SupplierAdmin MyAdmin() {
	return (SupplierAdmin)myAdmin_.getThisRef();
    }

    public EventConsumer getEventConsumer() {
	return null;
    }

    public boolean hasEventConsumer() {
	return false;
    }
    
    public List getSubsequentFilterStages() {
	return subsequentDestinations_;
    }

    public void dispose() {
	super.dispose();
	disconnectClient();
    }

    public Servant getServant() {
	if (thisServant_ == null) {
	    synchronized(this) {
		if (thisServant_ == null) {
		    thisServant_ = new StructuredProxyPushConsumerPOATie(this);
		}
	    }
	}
	return thisServant_;
    }

}// StructuredProxyPushConsumerImpl
