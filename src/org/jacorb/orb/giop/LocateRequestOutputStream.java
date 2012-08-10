package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.omg.CORBA.MARSHAL;
import org.omg.GIOP.LocateRequestHeader_1_0;
import org.omg.GIOP.LocateRequestHeader_1_0Helper;
import org.omg.GIOP.LocateRequestHeader_1_2;
import org.omg.GIOP.LocateRequestHeader_1_2Helper;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.TargetAddress;

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class LocateRequestOutputStream
    extends MessageOutputStream
{
    private final int request_id;

    public LocateRequestOutputStream( org.omg.CORBA.ORB orb,
                                      byte[] object_key,
                                      int request_id,
                                      int giop_minor )
    {
        super(orb);

        this.request_id = request_id;

        setGIOPMinor( giop_minor );

        writeGIOPMsgHeader( MsgType_1_1._LocateRequest, giop_minor );

        switch( giop_minor )
        {
            case 0 :
            {
                // GIOP 1.0 == GIOP 1.1, fall through
            }
            case 1 :
            {
                //GIOP 1.1
                LocateRequestHeader_1_0 req_hdr =
                    new LocateRequestHeader_1_0( request_id, object_key );

                LocateRequestHeader_1_0Helper.write( this, req_hdr );

                break;
            }
            case 2 :
            {
                //GIOP 1.2
                TargetAddress addr = new TargetAddress();
                addr.object_key( object_key );

                LocateRequestHeader_1_2 req_hdr =
                    new LocateRequestHeader_1_2( request_id, addr );

                LocateRequestHeader_1_2Helper.write( this, req_hdr );

                break;
            }
            default :
            {
                throw new MARSHAL( "Unknown GIOP minor: " + giop_minor );
            }
        }
    }

    public int getRequestId()
    {
        return request_id;
    }
}
