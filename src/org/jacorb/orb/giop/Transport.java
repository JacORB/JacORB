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

import java.io.IOException;

/**
 * Transport.java
 *
 *
 * Created: Sun Aug 12 20:14:16 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface Transport 
{
    /**
     * Receive a GIOP message. This message must be complete, i.e. not
     * fragmented.
     *
     * @return a message or null, if an ill-formatted message has been
     * received.  
     *
     * @throws IOException If the transport layer has shut down
     * irrevokably.  
     */
    public byte[] getMessage()
        throws IOException;

    /**
     * Writes to the wire. The buffer may only be a fragment of a
     * full message (in this case independant of GIOP Fragments), and
     * it also depends on the implementation, if it will already start
     * to send the message over the wire, or wait until sendMessages()
     * is called. <br>
     * 
     * This is not supposed to be synchronized. Synchronization issues
     * should be handled on the GIOP connection layer.
     *
     * @param message the buffer containing the message. 
     */
    public void write( byte[] message, int start, int size )
        throws IOException;
    

    /**
     * Send all messages that have been added since the last call to
     * this method.  
     */
    public void flush()
        throws IOException;
    
    /**
     * Close this transport (and free resources).  
     */
    public void close()
        throws IOException;
    
    /**
     * Test, if the transport is using SSL.
     */
    public boolean isSSL();
}// Transport



