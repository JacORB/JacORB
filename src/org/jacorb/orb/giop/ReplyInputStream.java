package org.jacorb.orb.connection;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.util.*;

import org.omg.GIOP.*;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.PortableServer.ForwardRequest;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class ReplyInputStream
    extends ServiceContextTransportingInputStream
{
    public ReplyHeader_1_2 rep_hdr = null;
    private int body_start;

    public ReplyInputStream( org.omg.CORBA.ORB orb, byte[] buffer )
    {
	super( orb, buffer );

        //check message type
	if( Messages.getMsgType( buffer ) != MsgType_1_1._Reply )
        {
	    throw new Error( "Error: not a reply!" );
        }
        
        switch( giop_minor )
        { 
            case 0 : 
            {
                //GIOP 1.0 = GIOP 1.1, fall through
            }
            case 1 : 
            {
                //GIOP 1.1
                ReplyHeader_1_0 hdr = 
                    ReplyHeader_1_0Helper.read( this );

                body_start = pos;

                rep_hdr = 
                    new ReplyHeader_1_2( hdr.request_id,
                                         ReplyStatusType_1_2.from_int( hdr.reply_status.value() ),
                                         hdr.service_context );
                break;
            }
            case 2 : 
            {
                //GIOP 1.2
                rep_hdr = ReplyHeader_1_2Helper.read( this );
                
                skipHeaderPadding();

                body_start = pos;

                break;
            }
            default : {
                throw new Error( "Unknown GIOP minor version: " + giop_minor );
            }
        }
    }

    public ReplyStatusType_1_2 getStatus()
    {
        return rep_hdr.reply_status;
    }
    
    /**
     * Returns a copy of the body of this reply.  This does not include
     * the GIOP header and the reply header.
     */
    public byte[] getBody()
    {
        int body_length = msg_size - (body_start - Messages.MSG_HEADER_SIZE);
        byte[] body = new byte[body_length];
        System.arraycopy (buffer, body_start, body, 0, body_length);
        return body;
    }

    public void finalize()
    {
	try
	{
	    close();
	}
	catch( java.io.IOException iox )
	{
	    //ignore
	}
    }
}










