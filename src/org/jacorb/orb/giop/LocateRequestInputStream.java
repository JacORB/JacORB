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
import org.omg.GIOP.*;
import org.omg.IOP.ServiceContext;

public class LocateRequestInputStream
    extends RequestInputStream
{
    private static ServiceContext[] ctx = new ServiceContext[0];

    public LocateRequestHeader_1_2 locate_req_hdr = null;

    public LocateRequestInputStream( org.omg.CORBA.ORB orb, byte[] buf )
    {
        //this is to bypass the constructor of RequestInputStream to
        //"reach" CDRInputStream
	super( orb, buf, true ); 

        //check message type
	if( buffer[7] != (byte) MsgType_1_1._LocateRequest )
        {
	    throw new Error( "Error: not a locate request!" );
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

        //giop_minor is defined in superclass
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
                LocateRequestHeader_1_0 hdr = 
                    LocateRequestHeader_1_0Helper.read( this );

                TargetAddress addr = new TargetAddress();
                addr.object_key( hdr.object_key );

                locate_req_hdr = 
                    new LocateRequestHeader_1_2( hdr.request_id, addr );

                //req_header is declared in superclass
                req_hdr = 
                    new RequestHeader_1_2( locate_req_hdr.request_id, 
                                           (byte) 0x03, //response_expected 
                                           new byte[3],
                                           addr, 
                                           "_non_existent", 
                                           ctx ); 
                break;
            }
            case 2 : 
            {
                //GIOP 1.2
                locate_req_hdr = LocateRequestHeader_1_2Helper.read( this );
                
                //req_header is declared in superclass
                req_hdr = 
                    new RequestHeader_1_2( locate_req_hdr.request_id, 
                                           (byte) 0x03, //response_expected 
                                           new byte[3],
                                           locate_req_hdr.target, 
                                           "_non_existent", 
                                           ctx ); 

                break;
            }
            default : {
                throw new Error( "Unknown GIOP minor version: " + giop_minor );
            }
        }
    }
}






