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

import java.io.IOException;
import org.jacorb.orb.CDROutputStream;

/**
 * MessageOutputStream.java
 *
 *
 * Created: Sat Aug 18 12:12:22 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class MessageOutputStream
    extends CDROutputStream
{
    public MessageOutputStream()
    {
        super( (org.omg.CORBA.ORB) null );
    }

    /**
     * Writes a GIOPMessageHeader of the required type to the beginning of
     * the buffer and sets the start position and index.
     */

    public void writeGIOPMsgHeader( int message_type,
                                    int minor_version )
    {
        //attribute: magic (4 bytes)
        buffer[0] = (byte) 'G';
        buffer[1] = (byte) 'I';
        buffer[2] = (byte) 'O';
        buffer[3] = (byte) 'P';

        //version
        buffer[4] = 1; //GIOP major
        buffer[5] = (byte) minor_version; //GIOP minor

        //endianess in GIOP 1.0, flags in GIOP 1.1/1.2. Always use big
        //endian.
        //For 1.1/1.2: 2nd LSB is 1 for fragments, but this
        //isn't supported (yet?) by JacORB. 6 MSBs must stay 0
        buffer[6] = 0;

        buffer[7] = (byte) message_type;

        // Skip the header + leave 4 bytes for message size
        skip( Messages.MSG_HEADER_SIZE );
    }

    public void insertMsgSize( int size )
    {
        //using big endian byte ordering
        buffer[8]  = (byte)((size >> 24) & 0xFF);
        buffer[9]  = (byte)((size >> 16) & 0xFF);
        buffer[10] = (byte)((size >>  8) & 0xFF);
        buffer[11] = (byte) (size        & 0xFF);
    }


    public void insertMsgSize()
    {
        insertMsgSize( size() - Messages.MSG_HEADER_SIZE );
    }

    public void write_to( GIOPConnection conn )
        throws IOException
    {
        insertMsgSize();

        write( conn, 0, size() );

        close();
    }
}// MessageOutputStream
