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

import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierOperations;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.apache.log4j.Logger;
import java.util.LinkedList;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotifyComm.SequencePushConsumer;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

/**
 * SequenceProxyPushSupplierImpl.java
 *
 *
 * Created: Sat Jan 11 16:47:42 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class SequenceProxyPushSupplierImpl extends StructuredProxyPushSupplierImpl
    implements SequenceProxyPushSupplierOperations,
	       EventDispatcher{
    
    SequencePushConsumer sequencePushConsumer_;
    private List pendingEvents_;
    boolean active_;
    static StructuredEvent[] ARRAY_TEMPLATE = new StructuredEvent[0];

    public SequenceProxyPushSupplierImpl(ApplicationContext appContext,
					 ChannelContext channelContext,
					 ConsumerAdminTieImpl myAdminServant,
					 ConsumerAdmin myAdmin,
					 Integer key) {
	super(appContext, 
	      channelContext, 
	      myAdminServant,
	      myAdmin,
	      key);
	pendingEvents_ = new LinkedList();
    }

    // overwrite
    public void dispatchEvent(NotificationEvent event) {
	if (connected_) {
	    try {
		StructuredEvent[] _eventArray = 
		    new StructuredEvent[] {event.toStructuredEvent()};
		sequencePushConsumer_.push_structured_events(_eventArray);
	    } catch (Disconnected d) {
		connected_ = false;
		logger_.debug("push failed - Recipient is Disconnected");
	    }
	} else {
	    logger_.debug("Not connected");
	}
    }

    // new
    public void connect_sequence_push_consumer(SequencePushConsumer consumer) throws AlreadyConnected, TypeError {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	sequencePushConsumer_ = consumer;
	connected_ = true;
    }

    // overwrite
    public void resume_connection() throws NotConnected, ConnectionAlreadyActive {
	if (!connected_) {
	    throw new NotConnected();
	}
	if (active_) {
	    throw new ConnectionAlreadyActive();
	}
	if (!pendingEvents_.isEmpty()) {
	    try {
		StructuredEvent[] _events = (StructuredEvent[])pendingEvents_.toArray(ARRAY_TEMPLATE);
		sequencePushConsumer_.push_structured_events(_events);
	    } catch (Disconnected e) {
		connected_ = false;
		throw new NotConnected();
	    }
	}
	active_ = true;
    }

    public void disconnect_sequence_push_supplier() {
	dispose();
    }

    // overwrite
    protected void disconnectClient() {
	if (connected_) {
	    if (sequencePushConsumer_ != null) {
		sequencePushConsumer_.disconnect_sequence_push_consumer();
		sequencePushConsumer_ = null;
		connected_ = false;
	    }
	}
    }
    
}// SequenceProxyPushSupplierImpl
