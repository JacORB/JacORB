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

import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import java.util.*;
import org.jacorb.orb.*;
import java.net.*;

/**
 * Simple implementation of the event channel spec.
 * The event channel acts as a factory for proxy push/pull consumers/suppliers
 * and interacts with the implementation objects locally, i.e. using Java
 * references only.
 *
 * @author Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose, Jeff Carlson
 * @version $Id$
 */

public class EventChannelImpl extends JacORBEventChannelPOA
{
  private Vector pullSuppliers = new Vector();
  private Vector pullConsumers = new Vector();
  private Vector pushSuppliers = new Vector();
  private Vector pushConsumers = new Vector();
  private Vector pendingEvents = new Vector();
  private org.omg.CORBA.Any nullAny = null;

  private org.omg.CORBA.ORB myOrb = null;
  private org.omg.PortableServer.POA myPoa = null;


  /**
   * EventChannel constructor.
   */
  public EventChannelImpl(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa)
  {
    myOrb = orb;
    myPoa = poa;

    _this_object(myOrb);
    nullAny = myOrb.create_any();
    nullAny.type(myOrb.get_primitive_tc( TCKind.tk_null));

    try
    {
      this.myPoa = poa;
      myPoa.the_POAManager().activate();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * send the ConsumerAdmin vectors off for destrcution.
   */
  private void consumerAdminDestroy()
  {
    releaseList( pullSuppliers );
    releaseList( pushSuppliers );
  }

  /**
   * send the SupplierAdmin vectors off for destrcution.
   */
  private void supplierAdminDestroy()
  {
    releaseList( pullConsumers );
    releaseList( pushConsumers );
  }

  /**
   * Iteratre a list and send the servant off to be destroyed.
   */
  private void releaseList( Vector list )
  {
    for ( Enumeration e = list.elements(); e.hasMoreElements(); )
    {
      org.omg.PortableServer.Servant servant =
          (org.omg.PortableServer.Servant)e.nextElement();
      releaseServant( servant );
    }
  }

  /**
   * Destroy / deactivate the servant.
   */
  private void releaseServant( org.omg.PortableServer.Servant servant )
  {
    try
    {
      servant._poa().deactivate_object( servant._object_id() );
    }
    catch (org.omg.PortableServer.POAPackage.WrongPolicy wpEx)
    {
      wpEx.printStackTrace();
    }
    catch (org.omg.PortableServer.POAPackage.ObjectNotActive onaEx)
    {
      onaEx.printStackTrace();
    }
  }

  /**
   * Destroy all objects which are managed by the POA.
   */
  public void destroy()
  {
    consumerAdminDestroy();
    supplierAdminDestroy();
    releaseServant(this);
  }


  /**
   * Return the consumerAdmin interface
   */
  public ConsumerAdmin for_consumers()
  {
    try
    {
      return ConsumerAdminHelper.narrow(myPoa.servant_to_reference(this));
    }
    catch( Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Return the supplierAdmin interface
   */
  public SupplierAdmin for_suppliers()
  {
    try
    {
      return SupplierAdminHelper.narrow(myPoa.servant_to_reference(this));
    }
    catch( Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Return a ProxyPullConsumer reference to be used to connect to a
   * PullSupplier.
   */
  public ProxyPullConsumer obtain_pull_consumer()
  {
    synchronized( pullConsumers )
    {
      ProxyPullConsumerImpl p =  new ProxyPullConsumerImpl( this, _orb(), myPoa );
      pullConsumers.addElement( p );
      return p._this( myOrb );
    }
  }

  /**
   * Return a ProxyPullSupplier reference to be used to connect to a
   * PullConsumer.
   */
  public ProxyPullSupplier obtain_pull_supplier()
  {
    synchronized( pullSuppliers )
    {
      ProxyPullSupplierImpl p =  new ProxyPullSupplierImpl ( this, _orb(), myPoa );
      pullSuppliers.addElement( p );
      return p._this( myOrb );
    }
  }

  /**
   * Return a ProxyPushConsumer reference to be used to connect to a
   * PushSupplier.
   */
  public ProxyPushConsumer obtain_push_consumer()
  {
    synchronized( pushConsumers )
    {
      ProxyPushConsumerImpl p = new ProxyPushConsumerImpl( this, _orb(), myPoa );
      pushConsumers.addElement( p );
      return p._this( myOrb );
    }
  }

  /**
   * Return a ProxyPushSupplier reference to be used to connect to a
   * PushConsumer.
   */
  public ProxyPushSupplier obtain_push_supplier()
  {
    synchronized( pushSuppliers )
    {
      ProxyPushSupplierImpl p = new ProxyPushSupplierImpl( this, _orb(), myPoa );
      pushSuppliers.addElement( p );
      return p._this( myOrb );
    }
  }


  /**
   * Send event to all registered consumers.
   */
  protected void push_event( org.omg.CORBA.Any event )
  {
    ProxyPushSupplierImpl push = null;
    ProxyPullSupplierImpl pull = null;
    synchronized( pushSuppliers )
    {
      for (int i=0, n=pushSuppliers.size(); i < n; i++ )
      {
        push = (ProxyPushSupplierImpl)pushSuppliers.elementAt( i );
        try
        {
          push.push_to_consumer( event );
        }
        catch( org.omg.CORBA.COMM_FAILURE comm )
        {
          pullSuppliers.removeElementAt( i );
          --i;
        }
      }
    }
    synchronized( pullSuppliers )
    {
      for (int i=0, n=pullSuppliers.size(); i < n; i++ )
      {
        pull = (ProxyPullSupplierImpl)pullSuppliers.elementAt( i );
        try
        {
          pull.push_to_supplier( event );
        }
        catch( org.omg.CORBA.COMM_FAILURE comm )
        {
          pullSuppliers.removeElementAt( i );
          --i;
        }
      }
    }
  }

  static public void main( String[] args )
  {
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
    try
    {
      org.omg.PortableServer.POA poa =
          org.omg.PortableServer.POAHelper.narrow(
              orb.resolve_initial_references("RootPOA"));

      EventChannelImpl channel = new EventChannelImpl(orb,poa);

      poa.the_POAManager().activate();

      org.omg.CORBA.Object o = poa.servant_to_reference(channel);

      NamingContextExt nc =
          NamingContextExtHelper.narrow(
              orb.resolve_initial_references("NameService"));

      String channelName = ( args.length > 0 ? args[0] : "Generic.channel" );

      nc.bind(nc.to_name( channelName  ), o);
      orb.run();
    }
    catch( Exception e )
    {
      e.printStackTrace();
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
