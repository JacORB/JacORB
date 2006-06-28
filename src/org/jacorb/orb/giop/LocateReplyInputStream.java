package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import org.omg.GIOP.LocateReplyHeader_1_0;
import org.omg.GIOP.LocateReplyHeader_1_0Helper;
import org.omg.GIOP.LocateReplyHeader_1_2;
import org.omg.GIOP.LocateReplyHeader_1_2Helper;
import org.omg.GIOP.LocateStatusType_1_2;
import org.omg.GIOP.MsgType_1_1;

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class LocateReplyInputStream
    extends MessageInputStream
{
    public LocateReplyHeader_1_2 rep_hdr = null;

    public LocateReplyInputStream( org.omg.CORBA.ORB orb,  byte[] buf )
    {
        super( orb, buf );

        //check message type
        if( buffer[7] != (byte) MsgType_1_1._LocateReply )
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
                LocateReplyHeader_1_0 hdr =
                    LocateReplyHeader_1_0Helper.read( this );

                rep_hdr =
                    new LocateReplyHeader_1_2( hdr.request_id,
                                               LocateStatusType_1_2.from_int( hdr.locate_status.value() ));
                break;
            }
            case 2 :
            {
                //GIOP 1.2
                rep_hdr = LocateReplyHeader_1_2Helper.read( this );

                break;
            }
            default :
            {
                throw new MARSHAL("Unknown GIOP minor version: " + giop_minor);
            }
        }
    }
}
