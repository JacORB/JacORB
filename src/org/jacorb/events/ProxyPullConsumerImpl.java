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

import org.jacorb.orb.*;

/**
 * @author Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 * $Id$
 */


public class ProxyPullConsumerImpl 
    extends org.omg.CosEventChannelAdmin.ProxyPullConsumerPOA
{
    private EventChannelImpl myEventChannel;
    private org.omg.CosEventComm.PullSupplier myPullSupplier;

    protected ProxyPullConsumerImpl ( EventChannelImpl ec, org.omg.CORBA.ORB orb ) 
    {
	myEventChannel = ec;
	_this_object( orb );
    }   

    //  ProxyPullConsumer Interface:

    public void connect_pull_supplier ( org.omg.CosEventComm.PullSupplier pull_supplier ) 
    {
	myPullSupplier = pull_supplier;
	synchronized( myEventChannel )
	{
	    myEventChannel.notify();
	}
    }   

    //  PullConsumer Interface:

    public void disconnect_pull_consumer() 
    {
	myEventChannel.disconnect_pull_consumer ( this );
    }   
    
    // Methods called by the EventChannel:

    protected org.omg.CORBA.Any internal_try_pull ( org.omg.CORBA.BooleanHolder has_event ) 
	throws org.omg.CosEventComm.Disconnected
    {
	try 
	{           
	    return myPullSupplier.try_pull( has_event );
	} 
	catch( Exception e)
	{
	    throw new org.omg.CosEventComm.Disconnected();
	}
    }   


    protected org.omg.CORBA.Any internal_pull () 
	throws org.omg.CosEventComm.Disconnected
    {
	try 
	{           
	    return myPullSupplier.pull();
	} 
	catch( Exception e)
	{
	    throw new org.omg.CosEventComm.Disconnected();
	}
    }   
}



