package org.jacorb.events;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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
import java.util.*;
import org.jacorb.orb.*;
import java.net.*;

/**
 * Simple implementation of the event channel spec.
 * The event channel acts as a factory for proxy push/pull consumers/suppliers
 * and interacts with the implementation objects locally, i.e. using Java
 * references only.
 *
 * @author Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose
 * @version $Id$
 */

public class EventChannelImpl 
    extends JacORBEventChannelPOA
{
    private Vector pull_suppliers;	
    private Vector pull_consumers;
    private Vector push_suppliers;		
    private Vector push_consumers;
    private Vector pending_events; 
    private org.omg.CORBA.Any nullAny;

    org.omg.PortableServer.POA poa; 

    class Sender implements Runnable 
    {
	org.omg.CORBA.Any asynchEvent;
	Vector p_suppliers= (Vector)push_suppliers.clone();

	public Sender(org.omg.CORBA.Any event) 
	{
	    asynchEvent = event;
	}
	
	public synchronized void run() 
	{  
	    // hand over the event asynchronously to 
	    // our ProxyPushSuppliers  
	    for (Enumeration e = push_suppliers.elements(); e.hasMoreElements();)
	    {
		ProxyPushSupplierImpl p = null;
		try 
		{
		    p = (ProxyPushSupplierImpl)e.nextElement();
		    p.internal_push( asynchEvent );
		} 
		catch ( org.omg.CosEventComm.Disconnected d) 
		{ 
		    if( p != null )
			p.disconnect_push_supplier();
		    else
			System.out.println(d); 
		}
	    }	   
	}
    }


    /* EventChannel */

    public EventChannelImpl(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa)
    {
	pull_suppliers = new Vector();
	pull_consumers = new Vector();
	push_suppliers = new Vector();
	push_consumers = new Vector();
	pending_events = new Vector();
	_this_object(orb);
	nullAny = orb.create_any();	
	nullAny.type(orb.get_primitive_tc( TCKind.tk_null));
	try
	{
	    this.poa = poa;
	    poa.the_POAManager().activate();
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
    }

    public void destroy()
    {
	// destroy lists !
    }


    /* management of local proxies */

    protected void disconnect_pull_consumer( ProxyPullConsumerImpl p )
    {
	pull_consumers.removeElement( p );
    }

    protected void disconnect_pull_supplier( ProxyPullSupplierImpl p )
    {
	pull_suppliers.removeElement( p );
    }

    protected void disconnect_push_consumer( ProxyPushConsumerImpl p )
    {
	push_consumers.removeElement( p );
    }

    protected void disconnect_push_supplier( ProxyPushSupplierImpl p )
    {
	push_suppliers.removeElement( p );
    }


    /* public admin interface */

    public ConsumerAdmin for_consumers()
    {
	try
	{
	    return ConsumerAdminHelper.narrow(poa.servant_to_reference(this));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    public SupplierAdmin for_suppliers()
    {
	try
	{
	    return SupplierAdminHelper.narrow(poa.servant_to_reference(this));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }


    public synchronized ProxyPullConsumer obtain_pull_consumer()
    {
	try
	{
	    ProxyPullConsumerImpl p =  new ProxyPullConsumerImpl( this, _orb() );
	    pull_consumers.addElement( p );
	    return ProxyPullConsumerHelper.narrow(poa.servant_to_reference(p));
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	    return null;
	}    
    }

    public ProxyPullSupplier obtain_pull_supplier()
    {
	try
	{
	    ProxyPullSupplierImpl p =  new ProxyPullSupplierImpl ( this, _orb() );
	    pull_suppliers.addElement( p );
	    return ProxyPullSupplierHelper.narrow(poa.servant_to_reference(p));
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	    return null;
	}
    }

    // SupplierAdmin-Interface
    public synchronized ProxyPushConsumer obtain_push_consumer()
    {
	try
	{
	    ProxyPushConsumerImpl p = new ProxyPushConsumerImpl( this, _orb() );	
	    push_consumers.addElement( p );
	    return ProxyPushConsumerHelper.narrow(poa.servant_to_reference(p));
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	    return null;
	}
    }

    // ConsumerAdmin-Interface
    public ProxyPushSupplier obtain_push_supplier()
    {
	try
	{
	    ProxyPushSupplierImpl p = new ProxyPushSupplierImpl( this, _orb() );
	    push_suppliers.addElement( p );
	    return ProxyPushSupplierHelper.narrow(poa.servant_to_reference(p));
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	    return null;
	}
    }


    /* internal interface */

    protected synchronized org.omg.CORBA.Any internal_pull( ProxyPullSupplierImpl p ) 
	throws Disconnected
    {
	Vector tmp;
	boolean found_event = false;
	org.omg.CORBA.Any event = null;   

	while ( !found_event )
	{
	    // pending_events ?
	    for (Enumeration e = pending_events.elements(); e.hasMoreElements() 
		     && !found_event;)
	    {
		EventListElement evt = (EventListElement)e.nextElement();
		if (evt.is_for_proxy(p)) 
		{
		    event = evt.event;
		    evt.consumers.removeElement(p);
		    if( evt.consumers.isEmpty() )
			pending_events.removeElement( evt );
		    return event;
		}
	    } 
	    
	    if( !found_event && pull_consumers.isEmpty()) 
	    {
		try 
		{
		    wait();     // notify for consumers_add_element and push.

		    /* notify can happen at two places: either a new pull supplier 
		     * has registered, or a new event was pushed into the channel 
		     */
		    if( pull_consumers.isEmpty() ) // i.e. a new event has arrived
			continue;
		} 
		catch  (InterruptedException e) 
		{}
	    }
	
	    // no pending_events, but suppliers exixt
	    if ( !found_event && !pull_consumers.isEmpty() ) 
	    {
		// pull exiting suppliers (ie proxyconsumers!) 
		for (Enumeration e = pull_consumers.elements(); e.hasMoreElements() && !found_event;) 
		{
		    ProxyPullConsumerImpl supplier = (ProxyPullConsumerImpl)e.nextElement();
		    /* pull suppliers might have disconnected */
		    try
		    {
			event = supplier.internal_pull();
			found_event = true;
		    }
		    catch ( Disconnected dis)
		    {
			disconnect_pull_consumer( supplier );
			continue;
		    }
		    if (found_event)
		    {
			// und fuer (andere!) Konsumenten (also: ProxyPullSuppliers) in den Puffer schreiben.
			tmp = (Vector)pull_suppliers.clone();
			tmp.removeElement( p );
			EventListElement x = new EventListElement( event, tmp );
			pending_events.addElement( x );
			notify();
		    }
		}
	    }
	
	    // no events, but suppliers exited and pulling failed:
	    if (!found_event) 
	    {
		try 
		{
		    wait(20);  
		} 
		catch  (InterruptedException e) {}
	    }

	} // end while
	
	return event;
    }

    protected synchronized void internal_push( org.omg.CORBA.Any event)
	throws Disconnected
    {
	Thread pushThread;
	if (!push_suppliers.isEmpty())
	{
	    //  pushThread = new Thread( new Sender(event, push_suppliers ));
	    pushThread = new Thread( new Sender(event ));
	    pushThread.start();
	} 

	if (!pull_suppliers.isEmpty())
	{
	    EventListElement e = new EventListElement( event, pull_suppliers );
	    pending_events.addElement( e );
	}
	notify();
    }


    protected synchronized org.omg.CORBA.Any internal_try_pull ( ProxyPullSupplierImpl p, 
								 BooleanHolder has_event ) 
	throws Disconnected
    {
	Vector tmp;
	boolean found_event = false;
	BooleanHolder found_event_ref;
	org.omg.CORBA.Any event = null;    

	found_event_ref = new BooleanHolder();
	found_event_ref.value =false;
	for (Enumeration e = pending_events.elements(); e.hasMoreElements() && !found_event;)
	{
	    EventListElement evt = (EventListElement)e.nextElement();
	    if (evt.is_for_proxy(p)) 
	    {
		found_event = true;  		
		event = evt.event;
		evt.consumers.removeElement(p);
		if( evt.consumers.isEmpty() )
		    pending_events.removeElement( evt );
		break;
	    }
	} 

	// no pending events. try-pull existing suppliers
	if ( !found_event && !pull_consumers.isEmpty())
	{
	    for (Enumeration e = pull_consumers.elements(); 
		 e.hasMoreElements() && !found_event;)
	    {
		ProxyPullConsumerImpl supplier = (ProxyPullConsumerImpl)e.nextElement();
		try
		{
		    event = supplier.internal_try_pull( found_event_ref );
		}
		catch( Disconnected dis )
		{
		    disconnect_pull_consumer( supplier );
		    continue;
		}
		found_event = found_event_ref.value;
		if (found_event) 
		{
		    // und fuer (andere!) Konsumenten (also: ProxyPullSuppliers) in den Puffer schreiben.
		    tmp = (Vector)pull_suppliers.clone();
		    tmp.removeElement( p );
		    EventListElement x = new EventListElement( event, tmp );
		    pending_events.addElement( x );
		    notify();
		    break;
		}
	    }  
	}    // endif

	has_event.value = found_event;
	if( ! found_event )
	{
	    /* we have to return something, and it may not be a null reference */
	    event = nullAny;	    
	}
	return event;
    }




    static public void main( String[] args ) 
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try 
	{           
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    EventChannelImpl channel = new EventChannelImpl(orb,poa);

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(channel);

	    NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	
	    String channelName = ( args.length > 0 ? args[0] : "Generic.channel" );

	    nc.bind(nc.to_name( channelName  ), o);
	    orb.run();
	} 
	catch ( Exception e) 
	{
	    e.printStackTrace();
	}

    }

}



