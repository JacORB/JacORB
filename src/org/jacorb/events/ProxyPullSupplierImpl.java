package org.jacorb.events;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import org.omg.CosEventComm.*;
import org.jacorb.orb.*;
import java.util.*;


/**
 * Implementation of COSEventChannelAdmin interface; ProxyPullSupplier.
 * This defines connect_pull_consumer(), disconnect_pull_supplier() and the all
 * important pull() and try_pull() methods that the Consumer can call to
 * actuall deliver a message.
 *
 * 2001/23/08 JFC OMG EventService Specification 1.1 page 2-7 states:
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
    extends org.omg.CosEventChannelAdmin.ProxyPullSupplierPOA
{
    private EventChannelImpl myEventChannel = null;
    private PullConsumer myPullConsumer = null;
    private org.omg.PortableServer.POA myPoa = null;
    private boolean connected = false;
    private LinkedList pendingEvents = new LinkedList();
    private final int maxListSize = 200;
    private static org.omg.CORBA.Any undefinedAny = null;

    /**
     * Constructor - to be called by EventChannel
     */
    protected ProxyPullSupplierImpl ( EventChannelImpl ec,
                                      org.omg.CORBA.ORB orb,
                                      org.omg.PortableServer.POA poa )
    {
        myEventChannel = ec;
        myPoa = poa;
        connected = false;
        _this_object(orb);
        undefinedAny = org.omg.CORBA.ORB.init().create_any();
    }

    /**
     * ProxyPullSupplier Interface:
     *  As stated by the EventService specification 1.1 section 2.3.5:
     * "If a ProxyPullSupplier is already connected to a PullConsumer, then the
     *  AlreadyConnected exception is raised."
     *  and
     * "If a non-nil reference is passed to connect_push_supplier..." implying
     *  that a null reference is acceptable.
     */

    public void connect_pull_consumer ( PullConsumer pullConsumer )
        throws org.omg.CosEventChannelAdmin.AlreadyConnected
    {
        if ( connected ) { throw new org.omg.CosEventChannelAdmin.AlreadyConnected(); }
        myPullConsumer = pullConsumer;
        connected = true;
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

    public void disconnect_pull_supplier()
    {
        if ( connected )
        {
            if ( myPullConsumer != null )
            {
                myPullConsumer.disconnect_pull_consumer();
                myPullConsumer = null;
            }
            connected = false;
        }
        else
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    /**
     * PullSupplier Interface.
     * section 2.1.3 states that "The <b>pull</b> operation blocks until the
     *   event data is available or an exception is raised.  It returns data to
     *   the consumer."
     */

    public org.omg.CORBA.Any pull ()
        throws org.omg.CosEventComm.Disconnected
    {
        org.omg.CORBA.Any event = null;
        org.omg.CORBA.BooleanHolder hasEvent = new org.omg.CORBA.BooleanHolder();
        while ( true )
        {
            event = try_pull( hasEvent );
            if ( hasEvent.value )
            {
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

    public org.omg.CORBA.Any try_pull ( org.omg.CORBA.BooleanHolder hasEvent )
        throws org.omg.CosEventComm.Disconnected
    {
        if ( !connected ) { throw new org.omg.CosEventComm.Disconnected(); }

        org.omg.CORBA.Any event = null;

        synchronized( pendingEvents )
        {
            int listSize = pendingEvents.size();
            if ( listSize > 0 )
            {
                event = (org.omg.CORBA.Any)pendingEvents.getFirst();
                pendingEvents.remove( event );
                hasEvent.value = true;
                return event;
            }
            else
            {
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

    public void push_to_supplier( org.omg.CORBA.Any event)
    {
        synchronized( pendingEvents )
        {
            if ( pendingEvents.size() > maxListSize )
            {
                pendingEvents.remove( pendingEvents.getFirst() );
            }
            pendingEvents.add( event );
        }
    }

    /**
     * Override this method from the Servant baseclass.  Fintan Bolton
     * in his book "Pure CORBA" suggests that you override this method to
     * avoid the risk that a servant object (like this one) could be
     * activated by the <b>wrong</b> POA object.
     */

    public org.omg.PortableServer.POA _default_POA()
    {
        return myPoa;
    }
}
