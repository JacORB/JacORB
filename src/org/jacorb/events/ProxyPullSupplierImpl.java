package org.jacorb.events;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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

/**
 * @authors Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 * @version $Id$
 */

import org.omg.CosEventComm.*;
import org.jacorb.orb.*;


public class ProxyPullSupplierImpl 
    extends org.omg.CosEventChannelAdmin.ProxyPullSupplierPOA
{
    private EventChannelImpl myEventChannel;
    private PullConsumer myPullConsumer;


    // Constructor - to be called by EventChannel 

    protected ProxyPullSupplierImpl ( EventChannelImpl ec, org.omg.CORBA.ORB orb ) 
    {
	myEventChannel = ec;
	_this_object(orb);
    }   
    //  ProxyPullSupplier Interface:

    public void connect_pull_consumer ( PullConsumer pull_consumer ) 
    {
	myPullConsumer = pull_consumer;
    }   
    public void disconnect_pull_supplier() 
    {
	myEventChannel.disconnect_pull_supplier ( this );
    }   

    //  PullSupplier Interface:

    public org.omg.CORBA.Any pull () 
	throws org.omg.CosEventComm.Disconnected 
    {
	return myEventChannel.internal_pull( this );
    }   

    public org.omg.CORBA.Any try_pull ( org.omg.CORBA.BooleanHolder has_event ) 
	throws org.omg.CosEventComm.Disconnected 
    {
	Boolean b = new Boolean ( has_event.value );
	org.omg.CORBA.Any event;

	event = myEventChannel.internal_try_pull( this, has_event );
	return event;
    }   
}



