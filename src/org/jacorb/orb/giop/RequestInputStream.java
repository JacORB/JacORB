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

import org.jacorb.orb.*;
import org.omg.GIOP.*;

/**
 * 
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 * 
 */

public class RequestInputStream
    extends org.jacorb.orb.CDRInputStream
{
    public RequestHeader_1_2 req_hdr = null;
    //public MessageHeader_1_1 msg_hdr = null;

    public int minor_version = -1;
    public int msg_length = -1;
    /**
     * used by subclass, flag is a dummy
     */
    protected RequestInputStream( org.omg.CORBA.ORB orb, 
                                  byte [] buf, 
                                  boolean flag )
    {
	super( orb, buf );
    }

    public RequestInputStream( org.omg.CORBA.ORB orb, byte[] buf )
    {
	super( orb,  buf );

        //check message type
	if( buffer[7] != (byte) MsgType_1_1._Request )
        {
	    throw new Error( "Error: not a request!" );
        }

        //check major version
        if( buffer[4] != 1 )
	{
            throw new Error( "Unknown GIOP major version: " + buffer[4] );
        }

        //although the attribute is renamed, this should work for 1.0
        //and 1.1/1.2
        setLittleEndian( (0x1 & buffer[6]) != 0 );

        //skip the message header. Its attributes are read directly
        skip( 12 );	    

        minor_version = buffer[5];
        
        switch( minor_version )
        { 
            case 0 : {
                //GIOP 1.0
                RequestHeader_1_0 hdr = 
                    RequestHeader_1_0Helper.read( this );

                TargetAddress addr = new TargetAddress();
                addr.object_key( hdr.object_key );

                req_hdr = 
                    new RequestHeader_1_2( hdr.request_id,
                                           (byte) ((hdr.response_expected)? 0x03 : 0x00),//flags
                                           new byte[3], //reserved
                                           addr, //target
                                           hdr.operation, 
                                           hdr.service_context );
                break;
            }
            case 1 : {
                //GIOP 1.1
                RequestHeader_1_1 hdr = 
                    RequestHeader_1_1Helper.read( this );

                TargetAddress addr = new TargetAddress();
                addr.object_key( hdr.object_key );

                req_hdr = 
                    new RequestHeader_1_2( hdr.request_id,
                                           (byte) ((hdr.response_expected)? 0x03 : 0x00), //flags
                                           new byte[3], //reserved
                                           addr, //target
                                           hdr.operation, 
                                           hdr.service_context );
                break;
            }
            case 2 : {
                //GIOP 1.2
                req_hdr = RequestHeader_1_2Helper.read( this );
                break;
            }
            default : {
                throw new Error( "Unknown GIOP minor version: " + minor_version );
            }
        }
    }
}






