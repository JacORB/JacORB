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
package org.jacorb.notification;

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
import org.apache.log4j.Logger;
import java.util.List;



/*
 *        JacORB - a free Java ORB
 */

/**
 * StructuredProxyPushConsumerImpl.java
 *
 *
 * Created: Mon Nov 04 01:52:01 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPushConsumerImpl extends ProxyBase implements StructuredProxyPushConsumerOperations {

    ProxyType myType_ = ProxyType.PUSH_STRUCTURED;
    SupplierAdmin myAdmin_;
    SupplierAdminTieImpl myAdminServant_;
    StructuredPushSupplier myPushSupplier_;

    public StructuredProxyPushConsumerImpl(ApplicationContext appContext,
					   ChannelContext channelContext,
					   SupplierAdminTieImpl supplierAdminServant, 
					   SupplierAdmin supplierAdmin) {
	super(appContext, 
	      channelContext,
	      Logger.getLogger("Proxy.StructuredPushConsumer"));

	myAdmin_ = supplierAdmin;
	myAdminServant_ = supplierAdminServant;
    }
    
    // Implementation of org.omg.CosNotifyComm.StructuredPushConsumerOperations

    /**
     * Describe <code>push_structured_event</code> method here.
     *
     * @param structuredEvent a <code>StructuredEvent</code> value
     * @exception Disconnected if an error occurs
     */
    public void push_structured_event(StructuredEvent structuredEvent) throws Disconnected {
	if (!connected_) {
	    throw new Disconnected();
	}

	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(structuredEvent, this);
	channelContext_.getEventChannelServant().process_event(_notifyEvent);
    }

    /**
     * Describe <code>disconnect_structured_push_consumer</code> method
     * here.
     *
     */
    public void disconnect_structured_push_consumer() {
	
    }
    
    // Implementation of org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerOperations

    /**
     * Describe <code>connect_structured_push_supplier</code> method here.
     *
     * @param structuredPushSupplier a <code>StructuredPushSupplier</code>
     * value
     * @exception AlreadyConnected if an error occurs
     */
    public void connect_structured_push_supplier(StructuredPushSupplier structuredPushSupplier) throws AlreadyConnected {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	myPushSupplier_ = structuredPushSupplier;
    }
    
    // Implementation of org.omg.CosNotifyChannelAdmin.ProxyConsumerOperations

    /**
     * Describe <code>MyType</code> method here.
     *
     * @return a <code>ProxyType</code> value
     */
    public ProxyType MyType() {
	return myType_;
    }

    /**
     * Describe <code>MyAdmin</code> method here.
     *
     * @return a <code>SupplierAdmin</code> value
     */
    public SupplierAdmin MyAdmin() {
	return null;
    }

    public TransmitEventCapable getEventSink() {
	return null;
    }
    
    public List getSubsequentDestinations() {
	return null;
    }

    public void dispose() {
    }

}// StructuredProxyPushConsumerImpl
