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

package org.jacorb.orb.connection;

import org.jacorb.orb.*;
import org.omg.GIOP.*;
import org.omg.IOP.ServiceContext;

/**
 * 
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 * 
 */

public class RequestInputStream
    extends ServiceContextTransportingInputStream
{
    private static byte[] reserved = new byte[3];
    private static ServiceContext[] ctx = new ServiceContext[0];

    public RequestHeader_1_2 req_hdr = null;

    public RequestInputStream( org.omg.CORBA.ORB orb, byte[] buf )
    {
	super( orb,  buf );

	if( Messages.getMsgType( buffer ) == MsgType_1_1._Request )
        {
            switch( giop_minor )
            { 
                case 0 : 
                {
                    //GIOP 1.0
                    RequestHeader_1_0 hdr = 
                        RequestHeader_1_0Helper.read( this );

                    TargetAddress addr = new TargetAddress();
                    addr.object_key( hdr.object_key );

                    req_hdr = 
                        new RequestHeader_1_2( hdr.request_id,
                                               Messages.responseFlags( hdr.response_expected ),
                                               reserved,
                                               addr, //target
                                               hdr.operation, 
                                               hdr.service_context );
                    break;
                }
                case 1 : 
                {
                    //GIOP 1.1
                    RequestHeader_1_1 hdr = 
                        RequestHeader_1_1Helper.read( this );

                    TargetAddress addr = new TargetAddress();
                    addr.object_key( hdr.object_key );

                    req_hdr = 
                        new RequestHeader_1_2( hdr.request_id,
                                               Messages.responseFlags( hdr.response_expected ),
                                               reserved,
                                               addr, //target
                                               hdr.operation, 
                                               hdr.service_context );
                    break;
                }
                case 2 : 
                {
                    //GIOP 1.2
                    req_hdr = RequestHeader_1_2Helper.read( this );
                    ParsedIOR.unfiyTargetAddress( req_hdr.target );

                    skipHeaderPadding();

                    break;
                }
                default : {
                    throw new Error( "Unknown GIOP minor version: " + giop_minor );
                }
            }
        }
        else if( Messages.getMsgType( buffer ) == MsgType_1_1._LocateRequest )
        {
            /*
            switch( giop_minor )
            { 
                case 0 : 
                {
                    //GIOP 1.0 = GIOP 1.1, fall through
                }
                case 1 : 
                {
                    //GIOP 1.1
                    LocateRequestHeader_1_0 locate_req_hdr = 
                        LocateRequestHeader_1_0Helper.read( this );

                    TargetAddress addr = new TargetAddress();
                    addr.object_key( locate_req_hdr.object_key );

                    req_hdr = 
                        new RequestHeader_1_2( locate_req_hdr.request_id, 
                                               (byte) 0x03,//response_expected 
                                               reserved,
                                               addr, 
                                               "_non_existent", 
                                               ctx ); 
                    break;
                }
                case 2 : 
                {
                    //GIOP 1.2
                    LocateRequestHeader_1_2 locate_req_hdr = 
                        LocateRequestHeader_1_2Helper.read( this );
                
                    ParsedIOR.unfiyTargetAddress( locate_req_hdr.target );
                    req_hdr = 
                        new RequestHeader_1_2( locate_req_hdr.request_id, 
                                               (byte) 0x03,//response_expected 
                                               reserved,
                                               locate_req_hdr.target, 
                                               "_non_existent", 
                                               ctx ); 
                    break;
                }
                default : 
                {
                    throw new Error( "Unknown GIOP minor version: " + giop_minor );
                }
            }
            */
        }
        else
        {
	    throw new Error( "Error: not a request!" );
        }
    }

    public ServiceContext getServiceContext( int id )
    {
        for( int i = 0; i < req_hdr.service_context.length; i++ )
        {
            if( req_hdr.service_context[i].context_id == id )
            {
                return req_hdr.service_context[i];
            }
        }
        
        return null;
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






