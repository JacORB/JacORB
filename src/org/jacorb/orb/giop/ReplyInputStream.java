package org.jacorb.orb.connection;

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

import java.io.*;
import org.jacorb.orb.*;
import org.jacorb.util.*;
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
    private ReplyHeader_1_2 rep_hdr;
    private int _request_id;
    private boolean _response_expected;
    private String _operation;
    private boolean ready = false;
    private boolean communicationException = false;
    private boolean remarshalException = false;
    private boolean timeoutException = false;

    private int giop_minor = -1;

    private int msg_size = -1;

    public ReplyInputStream( org.omg.CORBA.ORB orb, int request_id )
    {
	super( orb );
	_request_id = request_id;
    }

    /**
     * called from org.jacorb.orb.Connection
     * @param buf - the reply message buffer
     * @param target - the target object that was called (necessary 
     * for determining the correct interceptors)
     */

    public synchronized void init( byte[] buf )
    {
	super.buffer = buf;
	ready = true;
	this.notify();
    }

    /** 
     * this thread takes over again, start by
     * inspecting the reply message in buffer
     */
    private void wakeup()
    {
        //check message type
	if( Messages.getMsgType( buffer ) != MsgType_1_1._Reply )
        {
	    throw new Error( "Error: not a reply!" );
        }

        //check major version
        if( Messages.getGIOPMajor( buffer ) != 1 )
	{
            throw new Error( "Unknown GIOP major version: " + 
                             Messages.getGIOPMajor( buffer ));
        }

        //although the attribute is renamed, this should work for 1.0
        //and 1.1/1.2
        setLittleEndian( Messages.isLittleEndian( buffer ));

        msg_size = Messages.getMsgSize( buffer );
        
        //skip the message header. Its attributes are read directly
        skip( Messages.MSG_HEADER_SIZE );	    

        giop_minor = Messages.getGIOPMinor( buffer );

        //tell CDR stream which version to use
        super.setGIOPMinor( giop_minor );
        
        switch( giop_minor )
        { 
            case 0 : 
            {
                //GIOP 1.0 = GIOP 1.1, fall through
            }
            case 1 : 
            {
                /*
                //GIOP 1.1
                ReplyHeader_1_0 hdr = 
                    ReplyHeader_1_0Helper.read( this );

                rep_hdr = 
                    new ReplyHeader_1_2( hdr.request_id,
                                         ReplyStatusType.from_int( hdr.reply_status.value() ),
                                         hdr.service_context );
                break;
                */
            }
            case 2 : 
            {
                //GIOP 1.2
                rep_hdr = ReplyHeader_1_2Helper.read( this );
                
                skipHeaderPadding();

                break;
            }
            default : {
                throw new Error( "Unknown GIOP minor version: " + giop_minor );
            }
        }


        System.out.println(">>>>>>>>>Received reply with GIOP 1." + 
                           giop_minor);


	if( _request_id != rep_hdr.request_id )
        {
	    throw new Error("Fatal, request ids don\'t match");
        }
    }

    /**
     * called by the ORB to notify a client blocked on this object
     * of a communication error
     */

    public synchronized void cancel( boolean timeout )
    {
	timeoutException = timeout;
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

    public ReplyHeader_1_2 getHeader()
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
	catch( InterruptedException e )
	{}

        if( remarshalException )
	{
	    throw new org.omg.CORBA.portable.RemarshalException();
	}	
        else if( communicationException )
	{
            if( timeoutException )
                throw new org.omg.CORBA.IMP_LIMIT("Client timeout reached.");
            else
                throw new org.omg.CORBA.COMM_FAILURE();
	}

	wakeup();

	int read = 0;

	switch( rep_hdr.reply_status.value() ) 
	{
	    case ReplyStatusType_1_2._NO_EXCEPTION : 
		return this;	       
	    case ReplyStatusType_1_2._USER_EXCEPTION : 
	    {
                mark( 0 ); //arg readlimit (0) is not used
		String id = read_string();
                
                try
                {
                    reset();
                }
                catch( IOException ioe )
                {
                    //should not happen anyway
                    Debug.output( 1, ioe );
                }

		throw new ApplicationException(id, this);
	    }
 	    case  ReplyStatusType_1_2._SYSTEM_EXCEPTION: 
	    {
		throw( org.jacorb.orb.SystemExceptionHelper.read(this) );
	    }
	    case  ReplyStatusType_1_2._LOCATION_FORWARD: 
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
    
    public int getMsgSize()
    {
        return msg_size;
    }

    public void finalize()
    {
	try
	{
	    close();
	}
	catch( IOException iox )
	{
	    //ignore
	}
    }
}

