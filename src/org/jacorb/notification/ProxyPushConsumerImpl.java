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
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.Disconnected;

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

    private org.omg.CosEventComm.PushSupplier myPushSupplier;
    private boolean connected;
    

    Logger timeLogger_ = Logger.getLogger("TIME.ProxyPushConsumer");

        ProxyPushConsumerImpl(ApplicationContext appContext,
			  ChannelContext channelContext,
                          SupplierAdminTieImpl myAdminServant,
			  SupplierAdmin myAdmin) {

	super(myAdminServant,
	      appContext,
	      channelContext,
	      Logger.getLogger("Proxy.ProxyPushConsumer"));

	setProxyType(ProxyType.PUSH_ANY);
        connected = false;
    }

    ProxyPushConsumerImpl(ApplicationContext appContext,
			  ChannelContext channelContext,
                          SupplierAdminTieImpl myAdminServant,
			  SupplierAdmin myAdmin,
			  Integer key) {

	super(myAdminServant,
	      appContext,
	      channelContext,
	      key,
	      Logger.getLogger("Proxy.ProxyPushConsumer"));

	setProxyType(ProxyType.PUSH_ANY);
        connected = false;
    }

    public void disconnect_push_consumer() {
	dispose();
    }

    private void disconnectClient() {
	if (myPushSupplier != null) {
	    logger_.info("disconnect()");
	    myPushSupplier.disconnect_push_supplier();
	    myPushSupplier = null;
	}
    }

    /**
     * Supplier sends data to the consumer (this object) using this call.
     */
    public void push(Any event) throws Disconnected {
	logger_.debug("push(Any ...)");
	long _time = System.currentTimeMillis();

        if (!connected)  {
            throw new Disconnected();
        }

	NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(event, this);

	channelContext_.getEventChannelServant().dispatchEvent(_notifyEvent);
	timeLogger_.info("push(): " + (System.currentTimeMillis() - _time));
    }

    public void connect_push_supplier(org.omg.CosEventComm.PushSupplier pushSupplier) throws AlreadyConnected {
	connect_any_push_supplier(pushSupplier);
    }

    public void connect_any_push_supplier(org.omg.CosEventComm.PushSupplier pushSupplier) throws AlreadyConnected {
        logger_.debug("connect pushsupplier");

        if (connected) {
            throw new AlreadyConnected();
        }

        myPushSupplier = pushSupplier;
        connected = true;
    }

    public SupplierAdmin MyAdmin() {
	return (SupplierAdmin)myAdmin_.getThisRef();
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

    public void dispose() {
	super.dispose();
	disconnectClient();
    }
}
