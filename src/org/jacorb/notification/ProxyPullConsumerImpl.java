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
import org.jacorb.notification.framework.EventDispatcher;

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

    private boolean connected_ = false;
    private boolean active_ = false;
    private long pollInterval_ = 1000L;
    private Thread thisThread_;

    ProxyPullConsumerImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   SupplierAdminTieImpl adminServant,
			   SupplierAdmin myAdmin,
			   Integer key) {

	super(adminServant,appContext, channelContext, key, Logger.getLogger("Proxy.ProxyPullConsumer"));
        connected_ = false;
    }

    ProxyPullConsumerImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   SupplierAdminTieImpl adminServant,
			   SupplierAdmin myAdmin) {

	super(adminServant,appContext, channelContext, Logger.getLogger("Proxy.ProxyPullConsumer"));
        connected_ = false;
    }

    public void disconnect_pull_consumer() {
	dispose();
    }

    private void disconnectClient() {
	if (myPullSupplier_ != null) {
	    myPullSupplier_.disconnect_pull_supplier();
	    myPullSupplier_ = null;
	    connected_ = false;
	    active_ = false;
	}
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
	thisThread_ = new Thread(this);
	thisThread_.start();
    }

    public void run() {
        org.omg.CORBA.BooleanHolder hasEvent = new org.omg.CORBA.BooleanHolder();
        org.omg.CORBA.Any event = null;

	synchronized(this) {
	    while(connected_) {
                try {
                    event = myPullSupplier_.try_pull( hasEvent );
                } catch( org.omg.CORBA.UserException userEx ) {
                    connected_ = false;
                    // userEx.printStackTrace();
                    return;
                } catch( org.omg.CORBA.SystemException sysEx ) {
                    connected_ = false;
                    // sysEx.printStackTrace();
                    return;
                }

                if (hasEvent.value) {
		    logger_.debug("pulled event");
		    NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(event, this);
		    channelContext_.getEventChannelServant().dispatchEvent(_notifyEvent);
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

	if (connected_) {
	    throw new AlreadyConnected();
	} else {
	    connected_ = true;
	    active_ = true;
	    myPullSupplier_ = pullSupplier;
	    thisThread_ = new Thread(this);
	    thisThread_.start();
	}
    }

    public void connect_pull_supplier(PullSupplier pullSupplier) throws AlreadyConnected {
	connect_any_pull_supplier(pullSupplier);
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
