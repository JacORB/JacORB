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
import org.omg.GIOP.*;
import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class LocateReplyOutputStream
    extends MessageOutputStream
{
    public LocateReplyOutputStream ( int request_id, 
				     int status, 
                                     int giop_minor )
    {
        super();
        setGIOPMinor( giop_minor );
        
        writeGIOPMsgHeader( MsgType_1_1._LocateReply, giop_minor );

        switch( giop_minor )
        {
            case 0 :
            { 
                // GIOP 1.0 Reply == GIOP 1.1 Reply, fall through
            }
            case 1 :
            {
                //this currently doesn't work because GIOP.idl only
                //allows either LocateStatusType_1_0 or
                //LocateStatusType_1_2, but not both. The only solution
                //would be to go low-level and write directly to the
                //stream

                //GIOP 1.1
                LocateReplyHeader_1_0 repl_hdr = 
                    new LocateReplyHeader_1_0( request_id,
                                               LocateStatusType_1_0.from_int( status ));

                LocateReplyHeader_1_0Helper.write( this, repl_hdr );
               
                break;
            }
            case 2 :
            {
                //GIOP 1.2
                LocateReplyHeader_1_2 repl_hdr = 
                    new LocateReplyHeader_1_2( request_id,
                                               LocateStatusType_1_2.from_int( status ));

                LocateReplyHeader_1_2Helper.write( this, repl_hdr );

                break;
            }
            default :
            {
                throw new Error( "Unknown GIOP minor: " + giop_minor );
            }
        }
    }
}






