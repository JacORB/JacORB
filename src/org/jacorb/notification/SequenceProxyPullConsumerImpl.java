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


import org.omg.CosNotifyChannelAdmin.SequenceProxyPullConsumerOperations;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.apache.log4j.Logger;
import org.omg.CosNotifyComm.SequencePullSupplier;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CORBA.UserException;
import org.omg.CORBA.SystemException;
import java.util.List;
import java.util.Collections;
import org.jacorb.notification.framework.EventDispatcher;

/**
 * SequenceProxyPullConsumerImpl.java
 *
 *
 * Created: Sat Jan 11 17:10:48 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class SequenceProxyPullConsumerImpl 
    extends StructuredProxyPullConsumerImpl 
    implements SequenceProxyPullConsumerOperations, 
	       Runnable {

    SequencePullSupplier sequencePullSupplier_;

    public SequenceProxyPullConsumerImpl(ApplicationContext appContext,
					 ChannelContext channelContext, 
					 SupplierAdminTieImpl supplierAdminServant, 
					 SupplierAdmin supplierAdmin,
					 Integer key) {
	super(
	      appContext, 
	      channelContext,
	      supplierAdminServant,
	      supplierAdmin,
	      key
	      );
    }

    public void disconnect_sequence_pull_consumer() {
	dispose();
    }

    public void connect_sequence_pull_supplier(SequencePullSupplier sequencePullSupplier) throws AlreadyConnected {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	active_ = true;

	sequencePullSupplier_ = sequencePullSupplier;
	new Thread(this).start();
    }

    public void run() {
	runSequence();
    }

    public void runSequence() {
	BooleanHolder _hasEvent = new BooleanHolder();
	StructuredEvent[] _events = null;
	synchronized(this) {
	    while(connected_ && active_) {
		try {
		    _hasEvent.value = false;
		    _events = sequencePullSupplier_.try_pull_structured_events(1, _hasEvent);
		} catch (UserException e) {
		    connected_ = false;
		    return;
		} catch (SystemException e) {
		    connected_ = false;
		    return;
		}

		if (_hasEvent.value) {
		    for (int x=0; x<_events.length; ++x) {
			NotificationEvent _notifyEvent = notificationEventFactory_.newEvent(_events[x], this);
			channelContext_.getEventChannelServant().dispatchEvent(_notifyEvent);
		    }
		}
		
		try {
		    Thread.sleep(pollInterval_);
		} catch (InterruptedException ie) {}
	    }
	}
    }

    protected void disconnectClient() {
	if (connected_) {
	    if (sequencePullSupplier_ != null) {
		sequencePullSupplier_.disconnect_sequence_pull_supplier();
		sequencePullSupplier_ = null;
	    }
	}
	connected_ = false;
    }
    
}// SequenceProxyPullConsumerImpl
