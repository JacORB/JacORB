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
    private org.omg.GIOP.LocateReplyHeader_1_0 rep_hdr;
    private int _request_id;
    private boolean ready = false;

    public LocateReplyInputStream(org.omg.CORBA.ORB orb,  int request_id)
    {
	super( orb,new byte[0]);
	_request_id = request_id;
    }

    public synchronized void init( byte[] buf )
    {
	super.buffer = buf;

	if( buf[6] != 0 ) // big-endian
	{
	    littleEndian = true;
	    setLittleEndian(true);
	}
	if( buf[7] != (byte)org.omg.GIOP.MsgType_1_1._LocateReply )
	    throw new RuntimeException("Trying to initialize ReplyInputStream from non-reply msg.!");

	skip(12);
	rep_hdr = org.omg.GIOP.LocateReplyHeader_1_0Helper.read(this );

	if( _request_id != rep_hdr.request_id )
	    throw new RuntimeException("Fatal, request ids don\'t match");
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

    public synchronized org.omg.GIOP.LocateStatusType_1_0 status() 
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
	int read = 0;

	return rep_hdr.locate_status;
    }
}






