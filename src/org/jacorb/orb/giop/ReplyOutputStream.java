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

import org.omg.GIOP.*;

import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class ReplyOutputStream
    extends ServiceContextTransportingOutputStream
{
    /*
      private int request_id = -1;
      private ReplyStatusType_1_2 reply_status = null;
    */

    public ReplyOutputStream ( int request_id,
                               ReplyStatusType_1_2 reply_status,
                               int giop_minor )
    {
        super();

        /*
        this.request_id = request_id;
        this.reply_status = reply_status;
        */

        setGIOPMinor( giop_minor );
 
        System.out.println(">>>>>>>>>Created reply with GIOP 1." + 
                           giop_minor);

        writeGIOPMsgHeader( MsgType_1_1._Reply,
                            giop_minor );
        
        switch( giop_minor )
        {
            case 0 :
            { 
                // GIOP 1.0 Reply == GIOP 1.1 Reply, fall through
            }
            case 1 :
            {
                //this currently doesn't work because GIOP.idl only
                //allows either ReplyStatusType_1_0 or
                //ReplyStatusType_1_2, but not both. The only solution
                //would be to go low-level and write directly to the
                //stream

                /*
                //GIOP 1.1
                ReplyHeader_1_0 repl_hdr = 
                    new ReplyHeader_1_0( ctx,
                                         request_id,
                                         ReplyStatusType_1_0.from_int( reply_status.value() ));

                ReplyHeader_1_0Helper.write( out, repl_hdr );
               
                break;
                */
            }
            case 2 :
            {
                //GIOP 1.2
                ReplyHeader_1_2 repl_hdr = 
                    new ReplyHeader_1_2( request_id,
                                         reply_status,
                                         alignment_ctx );

                ReplyHeader_1_2Helper.write( this, repl_hdr );

                markHeaderEnd(); //use padding if minor 2

                break;
            }
            default :
            {
                throw new Error( "Unknown GIOP minor: " + giop_minor );
            }
        }        
    }

    /*
    public int requestId()
    {
        return request_id;
    }
    */
}














