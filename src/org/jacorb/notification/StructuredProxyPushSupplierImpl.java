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

import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushSupplierOperations;
import org.apache.log4j.Logger;
import org.omg.CosEventComm.Disconnected;
import java.util.Collections;
import java.util.List;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierOperations;
import org.omg.CosNotifyComm.SequencePushConsumer;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import java.util.LinkedList;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import java.util.Iterator;

/**
 * StructuredProxyPushSupplierImpl.java
 *
 *
 * Created: Sun Nov 03 22:41:38 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class StructuredProxyPushSupplierImpl 
    extends ProxyBase 
    implements StructuredProxyPushSupplierOperations,
	       EventDispatcher {

    private StructuredPushConsumer pushConsumer_;
    protected List pendingEvents_;
    protected boolean active_;

    public StructuredProxyPushSupplierImpl(ApplicationContext appContext,
					   ChannelContext channelContext,
					   ConsumerAdminTieImpl myAdminServant,
					   ConsumerAdmin myAdmin,
					   Integer key) {
	super(myAdminServant,
	      appContext, 
	      channelContext, 
	      key, 
	      Logger.getLogger("Proxy.StructuredProxyPushSupplier"));
	pendingEvents_ = new LinkedList();
    }
 
    public void dispatchEvent(NotificationEvent event) {
	if (connected_) {
	    try {
		pushConsumer_.push_structured_event(event.toStructuredEvent());
	    } catch (Disconnected d) {
		connected_ = false;
		logger_.debug("push failed - Recipient is Disconnected");
	    }
	} else {
	    logger_.debug("Not connected");
	}
    }
    
    public void connect_structured_push_consumer(StructuredPushConsumer consumer) throws AlreadyConnected, TypeError {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	pushConsumer_ = consumer;
    }

    public void disconnect_structured_push_supplier() {
	dispose();
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
    
    public void resume_connection() throws NotConnected, ConnectionAlreadyActive {
	if (!connected_) {
	    throw new NotConnected();
	}
	if (active_) {
	    throw new ConnectionAlreadyActive();
	}
	if (!pendingEvents_.isEmpty()) {
	    Iterator _i = pendingEvents_.iterator();
	    while (_i.hasNext()) {
		try {
		    pushConsumer_.push_structured_event((StructuredEvent)_i.next());
		} catch (Disconnected e) {
		    connected_ = false;
		    throw new NotConnected();
		}
	    }
	}
	active_ = true;
    }

    protected void disconnectClient() {
	if (connected_) {
	    if (pushConsumer_ != null) {
		pushConsumer_.disconnect_structured_push_consumer();
		pushConsumer_= null;
		connected_ = false;
	    }
	}
    }

    public ConsumerAdmin MyAdmin() {
	return (ConsumerAdmin)myAdmin_.getThisRef();
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
}// StructuredProxyPushSupplierImpl
