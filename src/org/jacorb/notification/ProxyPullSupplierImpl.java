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

import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyChannelAdmin.ObtainInfoMode;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyPullSupplierOperations;
import org.apache.log4j.Logger;
import org.omg.CORBA.Any;
import org.omg.CosEventComm.PullConsumer;
import org.omg.PortableServer.POA;
import org.omg.CORBA.BooleanHolder;
import org.jacorb.notification.framework.EventDispatcher;
import org.omg.CosEventComm.Disconnected;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ProxyPullSupplierImpl
    extends ProxyBase
    implements ProxyPullSupplierOperations,
	       org.omg.CosEventChannelAdmin.ProxyPullSupplierOperations,
	       EventDispatcher {

    private PullConsumer myPullConsumer_ = null;
    private boolean connected = false;
    private LinkedList pendingEvents = new LinkedList();
    private final int maxListSize = 200;
    private static Any undefinedAny = null;

    ProxyPullSupplierImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   ConsumerAdminTieImpl adminServant,
			   ConsumerAdmin myAdmin) {

	super(adminServant, 
	      appContext,
	      channelContext, 
	      Logger.getLogger("Proxy.ProxyPullSupplier"));

	setProxyType(ProxyType.PULL_ANY);
        connected = false;
        undefinedAny = appContext.getOrb().create_any();
    }

    ProxyPullSupplierImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   ConsumerAdminTieImpl adminServant,
			   ConsumerAdmin myAdmin,
			   Integer key) {

	super(adminServant, appContext ,channelContext, key, Logger.getLogger("Proxy.ProxyPullSupplier"));

	setProxyType(ProxyType.PULL_ANY);
        connected = false;
        undefinedAny = appContext.getOrb().create_any();
    }

    public void disconnect_pull_supplier() {
	dispose();
    }

    private void disconnect() {
	if (myPullConsumer_ != null) {
	    logger_.debug("disconnect()");
	    myPullConsumer_.disconnect_pull_consumer();
	    myPullConsumer_ = null;
	}
    }

    public Any pull ()
        throws Disconnected {
        Any event = null;
        BooleanHolder hasEvent = new org.omg.CORBA.BooleanHolder();
        while (true) {
            event = try_pull( hasEvent );
            if ( hasEvent.value ) {
                return event;
            }
            Thread.yield();
        }
    }

    /**
     * PullSupplier Interface.
     * section 2.1.3 states that "The <b>try_pull</b> operation does not block:
     *   if  the event data is available, it returns the event data and sets the
     *   <b>has_event</b> parameter to true; if the event is not available, it
     *   sets the <b>has_event</b> parameter to false and the event data is
     *   returned as long with an undefined value.
     * It seems that the event queue should be defined as a LIFO queue.  Finton
     * Bolton in his book Pure CORBA states that this is the "norm".  I think
     * that is really stupid.  Who wants events in reverse order with a
     * possibility of never getting the first messge?  I will therefore implement
     * this as a FIFO queue and wait for someone to convince me otherwise.
     */

    public Any try_pull (BooleanHolder hasEvent)
        throws Disconnected {

        if (!connected) { 
	    throw new Disconnected(); 
	}

        Any event = null;

        synchronized(pendingEvents) {
            int listSize = pendingEvents.size();
            if (listSize > 0) {
                event = (Any)pendingEvents.getFirst();
                pendingEvents.remove( event );
                hasEvent.value = true;
                return event;
            } else {
                hasEvent.value = false;
                return undefinedAny;
            }
        }
    }

    /**
     * Have to be able to get to the internal list of events.  This is how
     * to add stuff to this list.
     * I have to decide whether to a) just ignore the event, b) add the event
     * to the queue and remove the oldest event, c) throw an runtime exception.
     * Right now, I'm going with option b.
     */

    public void dispatchEvent(NotificationEvent event) {
         synchronized(pendingEvents) {
             if (pendingEvents.size() > maxListSize) {
                 pendingEvents.remove(pendingEvents.getFirst());
             }
             pendingEvents.add(event.toAny());
         }
     }

    public void connect_any_pull_consumer(PullConsumer pullConsumer) throws AlreadyConnected {
	logger_.info("connect_any_pull_consumer()");

	if (connected) {
	    throw new AlreadyConnected();
	}
	connected = true;
	myPullConsumer_ = pullConsumer;
    }

    public void connect_pull_consumer(PullConsumer consumer) throws AlreadyConnected {
	logger_.info("connect_pull_consumer()");

	if (connected) {
	    throw new AlreadyConnected();
	}
	connected = true;
	myPullConsumer_ = consumer;
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
	disconnect();
    }

    public void markError() {
	disconnect();
    }
}
