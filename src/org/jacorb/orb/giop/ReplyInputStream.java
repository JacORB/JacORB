package org.jacorb.orb.connection;

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

import java.io.*;
import org.jacorb.orb.*;
import org.omg.GIOP.*;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class ReplyInputStream
    extends CDRInputStream
{
    private org.omg.GIOP.ReplyHeader_1_0 rep_hdr;
    private int _request_id;
    private boolean _response_expected;
    private String _operation;
    private boolean ready = false;
    private boolean communicationException = false;
    private boolean remarshalException = false;
    private org.omg.CORBA.Object target;
    public org.omg.GIOP.MessageHeader_1_0 msg_hdr=null;

    public ReplyInputStream( org.omg.CORBA.ORB orb, int request_id)
    {
	super( orb, new byte[0] );
	_request_id = request_id;
    }

    /**
     * called from org.jacorb.orb.Connection
     * @param buf - the reply message buffer
     * @param target - the target object that was called (necessary 
     * for determining the correct interceptors)
     */

    public synchronized void init( byte[] buf, org.omg.CORBA.Object target )
    {
	super.buffer = buf;
	this.target = target;
	ready = true;
	this.notify();
    }

    /** 
     * this thread takes over again, start by
     * inspecting the reply message in buffer
     */

    private void wakeup()
    {
	if( buffer[6] != 0 ) // big-endian
	{
	    littleEndian = true;
	    setLittleEndian(true);
	}
	if( buffer[7] != (byte)org.omg.GIOP.MsgType_1_0._Reply )
	    throw new RuntimeException("Trying to initialize ReplyInputStream from non-reply msg.!");

	if (buffer[5]==1){
	    skip(12);	    
	}
	else	    
	    msg_hdr= org.omg.GIOP.MessageHeader_1_0Helper.read(this);

	rep_hdr = org.omg.GIOP.ReplyHeader_1_0Helper.read(this );

	if( _request_id != rep_hdr.request_id )
	    throw new RuntimeException("Fatal, request ids don\'t match");
    }

    /**
     * called by the ORB to notify a client blocked on this object
     * of a communication error
     */

    public synchronized void cancel()
    {
	communicationException = true;
	ready = true;
	this.notify();
    }

    public synchronized void retry()
    {
	remarshalException = true;
	ready = true;
	this.notify();
    }

    public int requestId()
    {
	return _request_id;
    }

    public org.omg.GIOP.ReplyHeader_1_0 getHeader()
    {
	return rep_hdr;
    }

    /** 
     * to be called from within Delegate. The result is returned to
     *  the waiting client.
     */

    public synchronized org.omg.CORBA.portable.InputStream result() 
	throws ApplicationException, 
        RemarshalException, 
        org.omg.PortableServer.ForwardRequest
    {
	try
	{
	    while( !ready ) 
	    {
		wait();
	    }
	} 
	catch ( java.lang.InterruptedException e )
	{}

	if( communicationException )
	{
	    throw new org.omg.CORBA.COMM_FAILURE();
	}
	else if( remarshalException )
	{
	    throw new org.omg.CORBA.portable.RemarshalException();
	}

	wakeup();

	int read = 0;

	switch( rep_hdr.reply_status.value() ) 
	{
	    case org.omg.GIOP.ReplyStatusType_1_0._NO_EXCEPTION : 
		return this;	       
	    case  org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION : 
	    {
		String id = read_string();
		unread_string(id);
		throw new ApplicationException(id, this);
	    }
 	    case  org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION: 
	    {
		throw( org.jacorb.orb.SystemExceptionHelper.read(this) );
	    }
	    case  org.omg.GIOP.ReplyStatusType_1_0._LOCATION_FORWARD: 
		throw new org.omg.PortableServer.ForwardRequest( this.read_Object());
	}
	return this;
    }

    /*  Method for proxy. The raw buffer
	will be returned without any processing of CORBA exceptions  */
 	
    public synchronized org.omg.CORBA.portable.InputStream rawResult() 
    {
	try
	{
	    while( !ready ) 
	    {
		wait();
	    }
	} 
	catch ( java.lang.InterruptedException e )
	{}

	if( communicationException )
	{
	    throw new org.omg.CORBA.COMM_FAILURE();
	}
	/* is this needed for the Appligator ??  */
//  	else if( remarshalException )
//  	{
//  	    throw new org.omg.CORBA.portable.RemarshalException();
//  	}

	wakeup();

	return this;
    }
}






