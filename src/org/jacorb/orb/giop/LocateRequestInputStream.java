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

package org.jacorb.orb.connection;

/**
 * 
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 * Hack for locate requests: turn a locate request into
 * a _non_existent() request and actually ping the object. This appears
 * to be necessary because we have to get around potentially holding
 * POAs - which can only be done using proper requests in our design.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import org.omg.PortableServer.POA;

public class LocateRequestInputStream
    extends RequestInputStream
{
    public org.omg.GIOP.LocateRequestHeader_1_0 locate_req_hdr;

    public LocateRequestInputStream( ServerConnection c,byte [] buf )
    {
	super( c, buf, true );
	if( buffer[7] != (byte)org.omg.GIOP.MsgType_1_0._LocateRequest )
	    throw new RuntimeException("Error: not a locate request!");
	setLittleEndian( buffer[6]!=0);

	skip(12);
	locate_req_hdr = 
            org.omg.GIOP.LocateRequestHeader_1_0Helper.read(this);	   
	req_hdr = 
            new org.omg.GIOP.RequestHeader_1_0( null,
                                                locate_req_hdr.request_id, 
                                                true, 
                                                locate_req_hdr.object_key, 
                                                "_non_existent", 
                                                null ); 
    }
}






