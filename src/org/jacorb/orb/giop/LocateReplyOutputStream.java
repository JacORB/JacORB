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

import java.io.*;
import org.omg.GIOP.*;
import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class LocateReplyOutputStream
    extends ReplyOutputStream
{
    private org.omg.GIOP.LocateReplyHeader_1_0 locate_rep_hdr;

    public LocateReplyOutputStream ( ServerConnection c,
                                     int request_id, 
				     int status, 
                                     org.omg.CORBA.Object arg )
    {
        super(c);
	locate_rep_hdr = 
            new org.omg.GIOP.LocateReplyHeader_1_0( request_id, 
                                                    org.omg.GIOP.LocateStatusType_1_0.from_int(status));

        writeGIOPMsgHeader( (byte)org.omg.GIOP.MsgType_1_0._LocateReply );
	org.omg.GIOP.LocateReplyHeader_1_0Helper.write(this, locate_rep_hdr);
        
	if( status == org.omg.GIOP.LocateStatusType_1_0._OBJECT_FORWARD )
	{
	    write_Object( arg );
	} 
    }


}












