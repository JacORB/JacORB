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

import java.io.*;
import java.util.Vector;

import org.omg.GIOP.*;
import org.omg.IOP.*;

import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class RequestOutputStream
    extends ServiceContextTransportingOutputStream
{
    private static byte[] principal = new byte[ 0 ];
    private static byte[] reserved = new byte[ 3 ];

    private int request_id = -1;
    private boolean response_expected = true;
    private String operation = null;

    private org.jacorb.orb.dii.Request request = null;

    private ClientConnection connection = null;

    public RequestOutputStream( ClientConnection connection,
                                int request_id,
                                String operation, 
                                boolean response_expected,
                                byte[] object_key,
                                int giop_minor )
    {
        super();
        
        setGIOPMinor( giop_minor );

        
        this.request_id = request_id;
        this.response_expected = response_expected;
        this.operation = operation;
        this.connection = connection;

        writeGIOPMsgHeader( MsgType_1_1._Request,
                            giop_minor );

        switch( giop_minor )
        {
            case 0 :
            { 
                // GIOP 1.0
                RequestHeader_1_0 req_hdr = 
                    new RequestHeader_1_0( service_context,
                                           request_id,
                                           response_expected,
                                           object_key,
                                           operation,
                                           principal );

                RequestHeader_1_0Helper.write( this, req_hdr );
                break;
            }
            case 1 :
            {
                //GIOP 1.1
                RequestHeader_1_1 req_hdr = 
                    new RequestHeader_1_1( service_context,
                                           request_id,
                                           response_expected,
                                           reserved,
                                           object_key,
                                           operation,
                                           principal );

                RequestHeader_1_1Helper.write( this, req_hdr );
               
                break;
            }
            case 2 :
            {
                //GIOP 1.2
                TargetAddress addr = new TargetAddress();
                addr.object_key( object_key );

                RequestHeader_1_2 req_hdr = 
                    new RequestHeader_1_2( request_id,
                                           (byte) ((response_expected)? 0x03 : 0x00),
                                           reserved,
                                           addr,
                                           operation,
                                           service_context );

                RequestHeader_1_2Helper.write( this, req_hdr );

                markHeaderEnd(); //use padding if GIOP minor == 2

                break;
            }
            default :
            {
                throw new Error( "Unknown GIOP minor: " + giop_minor );
            }
        }               
    }
    
    public int requestId()
    {
        return request_id;
    }

    public boolean response_expected()
    {
        return response_expected;
    }
   
    public String operation()
    {
        return operation;
    }

    public void setRequest(org.jacorb.orb.dii.Request request)
    {
        this.request = request;
    }

    public org.jacorb.orb.dii.Request getRequest()
    {
        return request;
    }
    
    public ClientConnection getConnection()
    {
        return connection;
    }
}
