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
import org.jacorb.orb.connection.*;

import org.omg.GIOP.*;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class LocateReplyInputStream
    extends CDRInputStream
{
    private LocateReplyHeader_1_2 rep_hdr;
    private int _request_id;
    private boolean ready = false;

    private int giop_minor = -1;

    public LocateReplyInputStream( org.omg.CORBA.ORB orb,  int request_id )
    {
	super( orb );
	this._request_id = request_id;
    }

    public synchronized void init( byte[] buf )
    {
	super.buffer = buf;
        //check message type
	if( buffer[7] != (byte) MsgType_1_1._LocateReply )
        {
	    throw new Error( "Error: not a reply!" );
        }

        //check major version
        if( buffer[4] != 1 )
	{
            throw new Error( "Unknown GIOP major version: " + buffer[4] );
        }

        //although the attribute is renamed, this should work for 1.0
        //and 1.1/1.2
        setLittleEndian( Messages.isLittleEndian( buffer ));

        //skip the message header. Its attributes are read directly
        skip( Messages.MSG_HEADER_SIZE );	    

        giop_minor = buffer[5];
        
        switch( giop_minor )
        { 
            case 0 : 
            {
                //GIOP 1.0 = GIOP 1.1, fall through
            }
            case 1 : 
            {
                
                //GIOP 1.1
                LocateReplyHeader_1_0 hdr = 
                    LocateReplyHeader_1_0Helper.read( this );

                rep_hdr = 
                    new LocateReplyHeader_1_2( hdr.request_id,
                                               LocateStatusType_1_2.from_int( hdr.locate_status.value() ));
                break;
                
            }
            case 2 : 
            {
                //GIOP 1.2
                rep_hdr = LocateReplyHeader_1_2Helper.read( this );

                break;
            }
            default : {
                throw new Error( "Unknown GIOP minor version: " + giop_minor );
            }
        }


	if( this._request_id != rep_hdr.request_id )
        {
	    throw new Error("Fatal, request ids don\'t match");
        }

	ready = true;
	this.notify();
    }

    public int requestId()
    {
	return _request_id;
    }


    /** 
     *  called from within Connection. The result is returned to
     *  the waiting client.
     */

    public synchronized LocateStatusType_1_2 status() 
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

	return rep_hdr.locate_status;
    }
}








