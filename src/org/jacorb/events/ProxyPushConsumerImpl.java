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


import org.omg.CosEventComm.*;
import org.jacorb.orb.*;

/**
 * @author Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose
 * @version $Id$
 */

public class ProxyPushConsumerImpl 
    extends org.omg.CosEventChannelAdmin.ProxyPushConsumerPOA
{
    private EventChannelImpl myEventChannel;
    private PushSupplier myPushSupplier;

    // Konstruktor - wird von EventChannel aufgerufen

    protected ProxyPushConsumerImpl ( EventChannelImpl ec,org.omg.CORBA.ORB orb ) 
    {
	myEventChannel = ec;
	_this_object( orb );
    }   

    // fuers ProxyPushConsumer Interface:

    public void connect_push_supplier ( PushSupplier push_supplier ) 
    {
	myPushSupplier = push_supplier;
    }   

    // fuers PushConsumer Interface:

    public void disconnect_push_consumer() 
    {
	myEventChannel.disconnect_push_consumer (this );
    }   

    public void push (org.omg.CORBA.Any event ) 
	throws Disconnected 
    {
	myEventChannel.internal_push ( event );
    }   
}



