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

import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyPushConsumerOperations;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Collections;
import org.omg.CORBA.ORB;

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPushConsumer.
 * This defines connect_push_supplier(), disconnect_push_consumer() and the all
 * important push() method that the Supplier can call to actuall deliver a
 * message.
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
public class ProxyPushConsumerImpl
    extends ProxyBase
    implements ProxyPushConsumerOperations, org.omg.CosEventChannelAdmin.ProxyPushConsumerOperations {

    private SupplierAdminTieImpl myAdminServant_;
    private SupplierAdmin myAdmin_;
    private org.omg.CosEventComm.PushSupplier myPushSupplier;
    private boolean connected;
    private ProxyType myType_ = ProxyType.PUSH_ANY;
    
    /**
     * Konstruktor - wird von EventChannel aufgerufen
     */
    ProxyPushConsumerImpl(ApplicationContext appContext,
			  ChannelContext channelContext,
                          SupplierAdminTieImpl myAdminServant,
			  SupplierAdmin myAdmin) {

	super(appContext,
	      channelContext,
	      Logger.getLogger("Proxy.ProxyPushConsumer"));

	myAdmin_ = myAdmin;
	myAdminServant_ = myAdminServant;
        connected = false;
        //_this_object(orb);
    }

    /**
     * fuers PushConsumer Interface:
     * See EventService v 1.1 specification section 2.1.1.
     *   'disconnect_push_consumer terminates the event communication; it releases
     *   resources used at the consumer to support event communication.  Calling
     *   this causes the implementation to call disconnect_push_supplier operation
     *   on the corresponding PushSupplier interface (if that iterface is known).'
     * See EventService v 1.1 specification section 2.1.5.  This method should
     *   adhere to the spec as it a) causes a call to the corresponding disconnect
     *   on the connected supplier, b) 'If a consumer or supplier has received a
     *   disconnect call and subsequently receives another disconnect call, it
     *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
     */
    public void disconnect_push_consumer() {
        if (connected) {
	    disconnect();
	    connected = false;
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    private boolean disconnect() {
	if (myPushSupplier != null) {
	    logger_.info("disconnect()");
	    myPushSupplier.disconnect_push_supplier();
	    myPushSupplier = null;
	    return true;
	}
	return false;
    }

    /**
     * Supplier sends data to the consumer (this object) using this call.
     */
    public void push (org.omg.CORBA.Any event )
	throws org.omg.CosEventComm.Disconnected {
	debug("push(Any)");
	
        if ( !connected )  {
            throw new org.omg.CosEventComm.Disconnected();
        }

	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(event, this);
	channelContext_.getEventChannelServant().process_event(_notifyEvent);
    }

    public void connect_push_supplier(org.omg.CosEventComm.PushSupplier pushSupplier) throws AlreadyConnected {
	connect_any_push_supplier(pushSupplier);
    }

    public void connect_any_push_supplier(org.omg.CosEventComm.PushSupplier pushSupplier) throws AlreadyConnected {
        debug("connect pushsupplier");
        if (connected) {
            throw new AlreadyConnected();
        }
        myPushSupplier = pushSupplier;
        connected = true;
    }

    public ProxyType MyType() {
        return myType_;
    }

    public SupplierAdmin MyAdmin() {
        return myAdmin_;
    }

    public List getSubsequentDestinations() {
	return Collections.singletonList(myAdminServant_);
    }

    public TransmitEventCapable getEventSink() {
	return null;
    }

    public void dispose() {
	logger_.info("dispose()");
	disconnect();
    }
}
