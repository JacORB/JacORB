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
import java.util.*;
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

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPullSupplier.
 * This defines connect_pull_consumer(), disconnect_pull_supplier() and the all
 * important pull() and try_pull() methods that the Consumer can call to
 * actuall deliver a message.
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
 * @version $Id$
 */

public class ProxyPullSupplierImpl
    extends ProxyBase
    implements ProxyPullSupplierOperations,
	       org.omg.CosEventChannelAdmin.ProxyPullSupplierOperations,
	       TransmitEventCapable {

    private PullConsumer myPullConsumer_ = null;
    private boolean connected = false;
    private LinkedList pendingEvents = new LinkedList();
    private final int maxListSize = 200;
    private static Any undefinedAny = null;
    private ConsumerAdminTieImpl adminServant_;
    private ConsumerAdmin myAdmin_;
    private ProxyType myType_ = ProxyType.PULL_ANY;

    ProxyPullSupplierImpl (ApplicationContext appContext,
			   ChannelContext channelContext,
			   ConsumerAdminTieImpl adminServant,
			   ConsumerAdmin myAdmin) {

	super(appContext ,channelContext, Logger.getLogger("Proxy.ProxyPullSupplier"));
	myAdmin_ = myAdmin;
	adminServant_ = adminServant;
        connected = false;
        //_this_object(orb);
        undefinedAny = appContext.getOrb().create_any();
    }

    /**
     * See EventService v 1.1 specification section 2.1.3.
     *   'disconnect_pull_supplier terminates the event communication; it releases
     *   resources used at the consumer to support event communication.  Calling
     *   this causes the implementation to call disconnect_pull_consumer operation
     *   on the corresponding PullConsumer interface (if that iterface is known).'
     * See EventService v 1.1 specification section 2.1.5.  This method should
     *   adhere to the spec as it a) causes a call to the corresponding disconnect
     *   on the connected supplier, b) 'If a consumer or supplier has received a
     *   disconnect call and subsequently receives another disconnect call, it
     *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
     * See EventService v 1.1 specification section 2.3.5. If [a nil object
     *   reference is passed to connect_pull_consumer] a channel cannot invoke a
     *   disconnect_pull_consumer operation on the consumer.
     */

    public void disconnect_pull_supplier() {
        if (connected) {
	    disconnect();
            connected = false;
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    private boolean disconnect() {
	if (myPullConsumer_ != null) {
	    logger_.debug("disconnect()");
	    myPullConsumer_.disconnect_pull_consumer();
	    myPullConsumer_ = null;
	    return true;
	}
	return false;
    }

    /**
     * PullSupplier Interface.
     * section 2.1.3 states that "The <b>pull</b> operation blocks until the
     *   event data is available or an exception is raised.  It returns data to
     *   the consumer."
     */

    public Any pull ()
        throws org.omg.CosEventComm.Disconnected {
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

    public org.omg.CORBA.Any try_pull (org.omg.CORBA.BooleanHolder hasEvent)
        throws org.omg.CosEventComm.Disconnected {

        if (!connected) { throw new org.omg.CosEventComm.Disconnected(); }

        org.omg.CORBA.Any event = null;

        synchronized(pendingEvents) {
            int listSize = pendingEvents.size();
            if (listSize > 0) {
                event = (org.omg.CORBA.Any)pendingEvents.getFirst();
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

    public void transmit_event(NotificationEvent event) {
	logger_.info("transmit_event()");

         synchronized(pendingEvents) {
             if ( pendingEvents.size() > maxListSize ) {
                 pendingEvents.remove( pendingEvents.getFirst() );
             }
             pendingEvents.add(event.toAny());
         }
     }

    public void connect_any_pull_consumer(org.omg.CosEventComm.PullConsumer pullConsumer) throws AlreadyConnected {
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
	logger_.info("dispose()");
	disconnect();
    }
}
