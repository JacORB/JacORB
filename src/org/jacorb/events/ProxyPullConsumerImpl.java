package org.jacorb.events;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
    extends org.omg.CosEventChannelAdmin.ProxyPullConsumerPOA
    implements Runnable
{
    private EventChannelImpl myEventChannel;
    private org.omg.CosEventComm.PullSupplier myPullSupplier;
    private org.omg.PortableServer.POA myPoa = null;
    private boolean connected = false;

    /**
     * Constructor - gets called by the EventChannel
     */
    protected ProxyPullConsumerImpl ( EventChannelImpl ec,
                                      org.omg.CORBA.ORB orb,
                                      org.omg.PortableServer.POA poa )
    {
        myEventChannel = ec;
        myPoa = poa;
        connected = false;
        _this_object( orb );
    }

    /**
     * ProxyPullConsumer Interface:
     *  As stated by the EventService specification 1.1 section 2.3.6:
     * "If a <b>ProxyPullSupplier</b> is already connected to a <b>PullConsumer</b>,
     *  then the <b>AlreadyConnected</b> exception is raised."
     *  and
     * "Implementations shall raise the CORBA standard <b>BAD_PARAM</b> exception if
     *  a nil object reference is passed to the <b>connect_pull_supplier</b> operation.
     *  and
     * "An implementation of a <b>ProxyPullConsumer</b> may put additional
     *  requirements on the interface supported by the pull supplier.  If t he pull
     *  supplier does not meet those requirements, the <b>ProxyPullConsumer</b>
     *  raises the <b>TypeError</b> exception. (See section 2.5.2 on page 2-15
     *  for an example)"
     */

    public void connect_pull_supplier ( org.omg.CosEventComm.PullSupplier pullSupplier )
        throws org.omg.CosEventChannelAdmin.AlreadyConnected, 
        org.omg.CosEventChannelAdmin.TypeError
    {
        if ( connected ) { throw new org.omg.CosEventChannelAdmin.AlreadyConnected(); }
        if ( pullSupplier == null ) { throw new org.omg.CORBA.BAD_PARAM(); }

        myPullSupplier = pullSupplier;
        connected = true;
        new Thread(this).start();
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

    public void disconnect_pull_consumer()
    {

        if ( connected )
        {
            if ( myPullSupplier != null )
            {
                myPullSupplier.disconnect_pull_supplier();
                myPullSupplier = null;
            }
            connected = false;
        }
        else
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }

    /**
     * Start being a good PullConsumer and ask for loads of events.
     */

    public void run()
    {
        org.omg.CORBA.BooleanHolder hasEvent = new org.omg.CORBA.BooleanHolder();
        org.omg.CORBA.Any event = null;
        while( connected )
        {
            synchronized(this)
            {
                try
                {
                    event = myPullSupplier.try_pull( hasEvent );
                }
                catch( org.omg.CORBA.UserException userEx )
                {
                    connected = false;
                    // userEx.printStackTrace();
                    return;
                }
                catch( org.omg.CORBA.SystemException sysEx )
                {
                    connected = false;
                    // sysEx.printStackTrace();
                    return;
                }

                if ( hasEvent.value )
                {
                    myEventChannel.push_event( event );
                }
                // Let other threads get some time on the CPU in case we're
                // in a cooperative environment.
                Thread.yield();
            }
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
