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

import org.omg.CosEventComm.*;
import org.omg.CosEventChannelAdmin.*;
import org.omg.PortableServer.*;
import org.omg.CORBA.*;
import org.jacorb.orb.*;

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPushConsumer.
 * This defines connect_push_supplier(), disconnect_push_consumer() and the all
 * important push() method that the Supplier can call to actuall deliver a
 * message.
 *
 * 2002/23/08 JFC OMG EventService Specification 1.1 page 2-7 states:
 *      "Registration is a two step process.  An event-generating application
 *      first obtains a proxy consumer from a channel, then 'connects' to the
 *      proxy consumer by providing it with a supplier.  ...  The reason for
 *      the two step registration process..."
 *    Modifications to support the above have been made as well as to support
 *    section 2.1.5 "Disconnection Behavior" on page 2-4.
 *
 * @author Jeff Carlson, Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose
 * @version $Id$
 */
public class ProxyPushConsumerImpl
    extends org.omg.CosEventChannelAdmin.ProxyPushConsumerPOA
{
  private EventChannelImpl myEventChannel;
  private PushSupplier myPushSupplier;
  private org.omg.PortableServer.POA myPoa;
  private boolean connected;

  /**
   * Konstruktor - wird von EventChannel aufgerufen
   */
  protected ProxyPushConsumerImpl ( EventChannelImpl ec,
                                    org.omg.CORBA.ORB orb,
                                    org.omg.PortableServer.POA poa )
  {
    myEventChannel = ec;
    myPoa = poa;
    connected = false;
    _this_object( orb );
  }

  /**
   * fuers ProxyPushConsumer Interface:
   * As stated by the EventService specification 1.1 section 2.3.4:
   * "If a ProxyPushConsumer is already connected to a PushSupplier, then the
   *  AlreadyConnected exception is raised."
   *  and
   * "If a non-nil reference is passed to connect_push_supplier..." implying
   * that a null reference is acceptable.
   */
  public void connect_push_supplier ( PushSupplier pushSupplier )
      throws org.omg.CosEventChannelAdmin.AlreadyConnected
  {
    if ( connected ) { throw new org.omg.CosEventChannelAdmin.AlreadyConnected(); }
    myPushSupplier = pushSupplier;
    connected = true;
  }

  /**
   * fuers PushConsumer Interface:
   * See EventService v 1.1 specification section 2.1.1.
   *   'disconnect_push_consumer terminates the event communication; it releases
   *   resources used at the consumer to support event communication.  Calling
   *   this causes the implementation to call disconnect_push_supplier operation
   *   on the corresponding PushSupplier interface (if that iterface is known).'
   * See EventService v 1.1 specification section 2.1.5.  This method should
   *   adhere to the spec as it a) causes a call to the corresponding disconnect
   *   on the connected supplier, b) 'If a consumer or supplier has received a
   *   disconnect call and subsequently receives another disconnect call, it
   *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
   */
  public void disconnect_push_consumer()
  {
    if ( connected )
    {
      if ( myPushSupplier != null )
      {
        myPushSupplier.disconnect_push_supplier();
        myPushSupplier = null;
      }
      connected = false;
    }
    else
    {
      throw new org.omg.CORBA.OBJECT_NOT_EXIST();
    }
  }

  /**
   * Supplier sends data to the consumer (this object) using this call.
   */
  public void push (org.omg.CORBA.Any event )
      throws org.omg.CosEventComm.Disconnected
  {
    if ( !connected )  { throw new org.omg.CosEventComm.Disconnected(); }
    myEventChannel.push_event( event );
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
