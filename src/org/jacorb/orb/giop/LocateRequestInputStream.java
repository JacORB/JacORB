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
import org.omg.IOP.ServiceContext;

/**
 * 
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 * 
 */

public class LocateRequestInputStream
    extends MessageInputStream
{
    public LocateRequestHeader_1_2 req_hdr = null;

    public LocateRequestInputStream( org.omg.CORBA.ORB orb, byte[] buf )
    {
	super( orb,  buf );

        if( Messages.getMsgType( buffer ) == MsgType_1_1._LocateRequest )
        {
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
                        new LocateRequestHeader_1_2( locate_req_hdr.request_id, 
                                                     addr ); 
                    break;
                }
                case 2 : 
                {
                    //GIOP 1.2
                    LocateRequestHeader_1_2 req_hdr = 
                        LocateRequestHeader_1_2Helper.read( this );
                
                    ParsedIOR.unfiyTargetAddress( req_hdr.target );

                    break;
                }
                default : 
                {
                    throw new Error( "Unknown GIOP minor version: " + giop_minor );
                }
            }
        }
        else
        {
	    throw new Error( "Error: not a Locate request!" );
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






