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

import org.jacorb.orb.CDRInputStream;
import org.omg.CORBA.MARSHAL;

/**
 * MessageInputStream.java
 *
 *
 * Created: Sat Aug 18 21:07:07 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class MessageInputStream
    extends CDRInputStream
{

    public int msg_size = -1;

    public MessageInputStream( org.omg.CORBA.ORB orb, byte[] buffer)
    {
        super( orb, buffer );

        //check major version
        if( Messages.getGIOPMajor( buffer ) != 1 )
        {
            throw new MARSHAL
                ("Unknown GIOP major version: " + Messages.getGIOPMajor(buffer));
        }

        //although the attribute is renamed, this should work for 1.0
        //and 1.1/1.2
        setLittleEndian( Messages.isLittleEndian( buffer ));

        setGIOPMinor( Messages.getGIOPMinor( buffer ) );

        msg_size = Messages.getMsgSize( buffer );

        //skip the message header. Its attributes are read directly
        skip( Messages.MSG_HEADER_SIZE );
    }
}// MessageInputStream
