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


import org.omg.CosNotifyChannelAdmin.SequenceProxyPullSupplierOperations;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosNotifyComm.SequencePullConsumer;
import java.util.LinkedList;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.apache.log4j.Logger;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventHeader;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CORBA.BooleanHolder;
import java.util.List;
import java.util.Collections;

/**
 * SequenceProxyPullSupplierImpl.java
 *
 *
 * Created: Sat Jan 11 16:57:08 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class SequenceProxyPullSupplierImpl 
    extends StructuredProxyPullSupplierImpl 
    implements SequenceProxyPullSupplierOperations, 
	       EventDispatcher {

    SequencePullConsumer sequencePullConsumer_;
    static StructuredEvent[] undefinedSequence_;

    public SequenceProxyPullSupplierImpl(ApplicationContext appContext, 
					   ChannelContext channelContext,
					   ConsumerAdminTieImpl myAdminServant, 
					   ConsumerAdmin myAdmin,
					   Integer key) {

	super(
	      appContext, 
	      channelContext, 
	      myAdminServant, 
	      myAdmin,
	      key
	      );


	if (undefinedSequence_ == null) {
	    synchronized(getClass()) {
		if (undefinedSequence_ == null) {
		    undefinedSequence_ = new StructuredEvent[] {undefinedStructuredEvent_};
		}
	    }
	}
    }
    
    public void connect_sequence_pull_consumer(SequencePullConsumer consumer) throws AlreadyConnected {
	if (connected_) {
	    throw new AlreadyConnected();
	}
	connected_ = true;
	sequencePullConsumer_ = consumer;
    }

    public StructuredEvent[] pull_structured_events(int number) throws Disconnected {
	logger_.debug("pull_structured_events(" + number + ")");
	StructuredEvent[] _event = null;
	BooleanHolder _hasEvent = new BooleanHolder();
	while(true) {
	    _event = try_pull_structured_events(number, _hasEvent);
	    if(_hasEvent.value) {
		return _event;
	    }
	    Thread.yield();
	}	
    }

    public StructuredEvent[] try_pull_structured_events(int number, BooleanHolder success) throws Disconnected {
	logger_.debug("try_pull_events");

	synchronized(pendingEvents_) {
	    int _size = pendingEvents_.size();
	    logger_.debug("size: " + _size);
	    if (_size > 0) {
		int _retSize = (number > _size) ? _size : number;
		logger_.debug("retSize = " + _retSize);

		StructuredEvent _ret[] = new StructuredEvent[_retSize];
		for (int x=0; x<_retSize; ++x) {
		    _ret[x] = (StructuredEvent)pendingEvents_.getFirst();
		}
		success.value = true;
		return _ret;
	    } else {
		success.value = false;
		return undefinedSequence_;
	    }
	}
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

    private void disconnectClient() {
	if (connected_) {
	    if (sequencePullConsumer_ != null) {
		sequencePullConsumer_.disconnect_sequence_pull_consumer();
		connected_ = false;
		sequencePullConsumer_ = null;
	    }
	}
    }

    public ConsumerAdmin MyAdmin() {
	return (ConsumerAdmin)myAdmin_.getThisRef();
    }

    public void disconnect_sequence_pull_supplier() {
	dispose();
    }

}// SequenceProxyPullSupplierImpl
