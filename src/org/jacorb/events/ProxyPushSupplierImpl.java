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
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.jacorb.orb.*;

/**
 * Implementation of COSEventChannelAdmin interface; ProxyPushSupplier.
 * This defines connect_push_consumer() and disconnect_push_supplier().  Helper
 * method will push a method to the registered consumer.
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
public class ProxyPushSupplierImpl
    extends org.omg.CosEventChannelAdmin.ProxyPushSupplierPOA
{
  private EventChannelImpl myEventChannel;
  private PushConsumer myPushConsumer;
  private org.omg.PortableServer.POA myPoa;
  private boolean connected;

  /**
   * Constructor - to be called by EventChannel
   */
  protected ProxyPushSupplierImpl ( EventChannelImpl ec,
                                    org.omg.CORBA.ORB orb,
                                    org.omg.PortableServer.POA poa )
  {
    myEventChannel = ec;
    myPoa = poa;
    connected = false;
    _this_object( orb );
  }

  /**
   * fuers ProxyPushSupplier Interface.
   * As stated by the EventService specification 1.1 section 2.3.7:
   * "If a ProxyPushSupplier is already connected to a PushConsumer, then the
   *  AlreadyConnected exception is raised."
   *  and
   * "Implementations shall raise the CORBA standard BAD_PARAM exception if
   *  a nil object reference is passed to the connect_push_consumer."
   */
  public void connect_push_consumer ( PushConsumer pushConsumer )
      throws org.omg.CosEventChannelAdmin.AlreadyConnected,
             org.omg.CORBA.BAD_PARAM
  {
    if ( connected ) { throw new org.omg.CosEventChannelAdmin.AlreadyConnected(); }
    if ( pushConsumer == null ) { throw new org.omg.CORBA.BAD_PARAM(); }

    myPushConsumer = pushConsumer;
    connected = true;
  }

  /**
   * fuers PushSupplier Interface
   * See EventService v 1.1 specification section 2.1.2.
   *   'disconnect_push_supplier terminates the event communication; it releases
   *   resources used at the supplier to support event communication.  Calling
   *   this causes the implementation to call disconnect_push_consumer operation
   *   on the corresponding PushSupplier interface (if that iterface is known).'
   * See EventService v 1.1 specification section 2.1.5.  This method should
   *   adhere to the spec as it a) causes a call to the corresponding disconnect
   *   on the connected supplier, b) 'If a consumer or supplier has received a
   *   disconnect call and subsequently receives another disconnect call, it
   *   shall raise a CORBA::OBJECT_NOT_EXIST exception.
   */
  public void disconnect_push_supplier()
  {
    if ( connected )
    {
      if ( myPushConsumer != null )
      {
        myPushConsumer.disconnect_push_consumer();
        myPushConsumer = null;
      }
      connected = false;
    }
    else
    {
      throw new OBJECT_NOT_EXIST();
    }
  }


  /**
   * Methoden, die von unserem EventChannel aufgerufen werden
   */
  protected void push_to_consumer(org.omg.CORBA.Any event )
  {
    if ( connected )
    {
      try
      {
        myPushConsumer.push ( event );
      }
      catch( Disconnected e )
      {
        connected = false;
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