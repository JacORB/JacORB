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

import org.jacorb.orb.*;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;

import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotification.Property;

import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;

import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyPullConsumerOperations;
import org.apache.log4j.Logger;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import java.util.List;
import java.util.Collections;
import org.omg.CosEventComm.PullSupplier;
import org.omg.PortableServer.POA;

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPullConsumer.
 * This defines connect_pull_supplier() and disconnect_pull_consumer().
 *
 * 2002/23/08 JFC OMG EventService Specification 1.1 page 2-7 states:
 *      "Registration is a two step process.  An event-generating application
 *      first obtains a proxy consumer from a channel, then 'connects' to the
 *      proxy consumer by providing it with a supplier.  ...  The reason for
 *      the two step registration process..."
 *    Modifications to support the above have been made as well as to support
 *    section 2.1.5 "Disconnection Behavior" on page 2-4.
 *
 * @authors Jeff Carlson, Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 * $Id$
 */

public class ProxyPullConsumerImpl
    extends ProxyBase
    implements Runnable, 
	       ProxyPullConsumerOperations,
	       org.omg.CosEventChannelAdmin.ProxyPullConsumerOperations {

    private org.omg.CosEventComm.PullSupplier myPullSupplier_;
    private boolean connected = false;
    private SupplierAdminTieImpl myAdminServant_;
    private SupplierAdmin myAdmin_;
    private ProxyType myType_ = ProxyType.PULL_ANY;

    private long pollInterval_ = 1000;

    /**
     */
    ProxyPullConsumerImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   SupplierAdminTieImpl adminServant,
			   SupplierAdmin myAdmin) {

	super(appContext, channelContext, Logger.getLogger("Proxy.ProxyPullConsumer"));
	myAdmin_ = myAdmin;
	myAdminServant_ = adminServant;
        connected = false;
        //_this_object( orb );
    }

    /**
     * See EventService v 1.1 specification section 2.1.4.
     *   'disconnect_pull_consumer terminates the event communication; it releases
     *   resources used at the consumer to support event communication.  Calling
     *   this causes the implementation to call disconnect_pull_supplier operation
     *   on the corresponding PullSupplier interface (if that iterface is known).'
     * See EventService v 1.1 specification section 2.1.5.  This method should
     *   adhere to the spec as it a) causes a call to the corresponding disconnect
     *   on the connected supplier, b) 'If a consumer or supplier has received a
     *   disconnect call and subsequently receives another disconnect call, it
     *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
     */

    public void disconnect_pull_consumer() {
        if (connected) {
	    disconnect();
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    private void disconnect() {
	if (myPullSupplier_ != null) {
	    myPullSupplier_.disconnect_pull_supplier();
	    myPullSupplier_ = null;
	    connected = false;
	}
    }

    /**
     * Start being a good PullConsumer and ask for loads of events.
     */

    public void run() {
        org.omg.CORBA.BooleanHolder hasEvent = new org.omg.CORBA.BooleanHolder();
        org.omg.CORBA.Any event = null;

	synchronized(this) {
	    while(connected) {
                try {
                    event = myPullSupplier_.try_pull( hasEvent );
                } catch( org.omg.CORBA.UserException userEx ) {
                    connected = false;
                    // userEx.printStackTrace();
                    return;
                } catch( org.omg.CORBA.SystemException sysEx ) {
                    connected = false;
                    // sysEx.printStackTrace();
                    return;
                }

                if (hasEvent.value) {
		    logger_.debug("pulled event");
		    NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(event, this);
		    channelContext_.getEventChannelServant().process_event(_notifyEvent);
                }
                // Let other threads get some time on the CPU in case we're
                // in a cooperative environment.
		try {
		    Thread.sleep(pollInterval_);
		} catch (InterruptedException ie) {}
            }
        }
    }

    public void connect_any_pull_supplier(PullSupplier pullSupplier) 
	throws AlreadyConnected {

	if (connected) {
	    throw new AlreadyConnected();
	} else {
	    connected = true;
	    myPullSupplier_ = pullSupplier;
	    new Thread(this).start();
	}
    }

    public void connect_pull_supplier(PullSupplier pullSupplier) throws AlreadyConnected {
	connect_any_pull_supplier(pullSupplier);
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
	logger_.info("dispose");
	disconnect();
    }
}
