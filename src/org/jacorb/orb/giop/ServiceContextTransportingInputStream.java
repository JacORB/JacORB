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

package org.jacorb.orb.giop;

/**
 * ServiceContextTransportingInputStream.java
 *
 *
 * Created: Sat Aug 18 21:07:07 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServiceContextTransportingInputStream 
    extends MessageInputStream 
{
    public ServiceContextTransportingInputStream( org.omg.CORBA.ORB orb, 
                                                  byte[] buffer)
    {
        super( orb, buffer );
    }
    
    /**
     * For GIOP 1.2 only: The message body is aligned on an 8 byte
     * boundary, if a body is present.  
     */
    protected void skipHeaderPadding()
    {
        int pos = get_pos();

        int header_padding = 8 - (pos % 8); //difference to next 8 byte border
        header_padding = (header_padding == 8)? 0 : header_padding;

        //skip header_padding bytes anyway, because if no body is
        //present, nobody will try to read it
        skip( header_padding );
    }
}// ServiceContextTransportingInputStream











