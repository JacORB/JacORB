package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.apache.log4j.Logger;
import org.jacorb.orb.*;
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

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPushSupplier.
 * This defines connect_push_consumer() and disconnect_push_supplier().  Helper
 * method will push a method to the registered consumer.
 *
 * 2002/23/08 JFC OMG EventService Specification 1.1 page 2-7 states:
 *      "Registration is a two step process.  An event-generating application
 *      first obtains a proxy consumer from a channel, then 'connects' to the
 *      proxy consumer by providing it with a supplier.  ...  The reason for
 *      the two step registration process..."
 *    Modifications to support the above have been made as well as to support
 *    section 2.1.5 "Disconnection Behavior" on page 2-4.
 *
 * @author Jeff Carlson, Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose
 * @version $Id$
 */
public class ProxyPushSupplierImpl 
    extends ProxyBase 
    implements ProxyPushSupplierOperations,
	       org.omg.CosEventChannelAdmin.ProxyPushSupplierOperations,
	       TransmitEventCapable {

    private org.omg.CosEventComm.PushConsumer myPushConsumer_;
    private boolean connected_;
    private ConsumerAdminTieImpl myAdminServant_;
    private ConsumerAdmin myAdmin_;
    private ProxyType myType_ = ProxyType.PUSH_ANY;

    ProxyPushSupplierImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   ConsumerAdminTieImpl myAdminServant,
			   ConsumerAdmin myAdmin) {
	
	super(appContext, channelContext, Logger.getLogger("Proxy.ProxyPushSupplier"));
	myAdmin_ = myAdmin;
	myAdminServant_ = myAdminServant;
	connected_ = false;
	//_this_object(orb);
  }

  /**
   * fuers PushSupplier Interface
   * See EventService v 1.1 specification section 2.1.2.
   *   'disconnect_push_supplier terminates the event communication; it releases
   *   resources used at the supplier to support event communication.  Calling
   *   this causes the implementation to call disconnect_push_consumer operation
   *   on the corresponding PushSupplier interface (if that iterface is known).'
   * See EventService v 1.1 specification section 2.1.5.  This method should
   *   adhere to the spec as it a) causes a call to the corresponding disconnect
   *   on the connected supplier, b) 'If a consumer or supplier has received a
   *   disconnect call and subsequently receives another disconnect call, it
   *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
   */
    public void disconnect_push_supplier() {
	if (connected_) {
	    disconnect();
	    connected_ = false;
	} else {
	    throw new OBJECT_NOT_EXIST();
	}
    }
    
    private boolean disconnect() {
	if (myPushConsumer_ != null) {
	    logger_.debug("disconnect");
	    myPushConsumer_.disconnect_push_consumer();
	    myPushConsumer_ = null;
	    return true;
	}
	return false;
    }

  /**
   * Methoden, die von unserem EventChannel aufgerufen werden
   */
    public void transmit_event(NotificationEvent event){
	logger_.info("transmit_event()");
	if (connected_) {
	    try {
		myPushConsumer_.push(event.toAny());
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
	
	if ( connected_ ) { 
	    throw new org.omg.CosEventChannelAdmin.AlreadyConnected(); 
	}
	if ( pushConsumer == null ) { 
	    throw new org.omg.CORBA.BAD_PARAM(); 
	}
	myPushConsumer_ = pushConsumer;
	connected_ = true;
    }

    public ProxyType MyType() {
	return myType_;
    }

    public ConsumerAdmin MyAdmin() {
	return myAdmin_;
    }

    public List getSubsequentDestinations() {
	return Collections.singletonList(this);
    }

    public TransmitEventCapable getEventSink() {
	return this;
    }

    public void dispose() {
	logger_.info("dispose");
	disconnect();
    }
}
