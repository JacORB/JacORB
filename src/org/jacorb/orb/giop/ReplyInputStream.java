package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
import org.omg.CORBA.MARSHAL;
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
            throw new MARSHAL("Not a reply!");
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
                throw new MARSHAL("Unknown GIOP minor version: " + giop_minor);
            }
        }
    }

    /**
     * Returns the reply status of this reply.
     */
    public ReplyStatusType_1_2 getStatus()
    {
        return rep_hdr.reply_status;
    }

    /**
     * Returns any exception that is indicated by this reply.  If
     * the reply status is USER_EXCEPTION, SYSTEM_EXCEPTION, LOCATION_FORWARD,
     * or LOCATION_FORWARD_PERM, an appropriate exception object is returned.
     * For any other status, returns null.
     */
    public synchronized Exception getException()
    {
        switch( rep_hdr.reply_status.value() )
        {
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
                    // Debug.output( 1, ioe );
                }
                return new ApplicationException( id, this );
            }
            case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
            {
                return SystemExceptionHelper.read( this );
            }
            case  ReplyStatusType_1_2._LOCATION_FORWARD:
            case  ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
            {
                return new ForwardRequest( read_Object() );
            }
            default:
            {
                return null;
            }
        }
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

    protected void finalize() throws Throwable
    {
        try
        {
            close();
        }
        catch( java.io.IOException iox )
        {
            //ignore
        }
        finally
        {
            super.finalize();
        }
    }
}
