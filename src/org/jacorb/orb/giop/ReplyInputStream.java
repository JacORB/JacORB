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
    }

    /** 
     * This is called from within Delegate and will throw arrived exceptions.
     */

    public synchronized void checkExceptions() 
	throws ApplicationException, 
        ForwardRequest
    {
	switch( rep_hdr.reply_status.value() ) 
	{
	    case ReplyStatusType_1_2._NO_EXCEPTION :
            { 
		break; //no exception	       
            }
	    case ReplyStatusType_1_2._USER_EXCEPTION : 
	    {
                mark( 0 ); 
		String id = read_string();
                
                try
                {
                    reset();
                }
                catch( java.io.IOException ioe )
                {
                    //should not happen anyway
                    Debug.output( 1, ioe );
                }

		throw new ApplicationException( id, this );
	    }
 	    case  ReplyStatusType_1_2._SYSTEM_EXCEPTION: 
	    {
		throw SystemExceptionHelper.read( this );
	    }
	    case  ReplyStatusType_1_2._LOCATION_FORWARD:
            {
                //fall through
            }
            case  ReplyStatusType_1_2._LOCATION_FORWARD_PERM: 
            {
		throw new ForwardRequest( read_Object() );
            }
            case  ReplyStatusType_1_2._NEEDS_ADDRESSING_MODE :
            {
                throw new org.omg.CORBA.NO_IMPLEMENT( "WARNING: Received reply with status NEEDS_ADRESSING_MODE, but this isn't implemented yet" );
            }
            default :
            {
                throw new Error( "Received unexpected reply status: " +
                                 rep_hdr.reply_status.value() );
            }
	}
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










