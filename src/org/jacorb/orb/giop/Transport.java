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

package org.jacorb.orb.connection;

import java.io.IOException;

/**
 * Transport.java
 *
 *
 * Created: Sun Aug 12 20:14:16 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface Transport 
{
    // ETF methods
    
    void write (boolean is_first, 
                boolean is_last, 
                byte[] data, 
                int offset, 
                int length, 
                long time_out);
                
    void read (org.omg.ETF.BufferHolder data, 
               int offset, 
               int min_length, 
               int max_length, 
               long time_out);
               
    void flush();

    void connect (org.omg.ETF.Profile server_profile, long time_out);
    
    void close();
    
    boolean is_connected();

    org.omg.ETF.Profile get_server_profile(); 

    // Non-ETF methods below this line

    /**
     * Get the statistics provider for transport usage statistics.
     */
    public StatisticsProvider getStatisticsProvider();

    /**
     * Set the transport listener used for upcalls.
     */
    public void setTransportListener( TransportListener listener );

    /**
     * This is used to tell the transport that a CloseConnection has
     * been sent, and that it should set a timeout in case the client
     * doesn't close its side of the connection right away.
     *
     * This should only be called on the thread that listens on the
     * socket because timeouts are not applied until read() is called
     * the next time.  
     */
    public void turnOnFinalTimeout();
    
}// Transport



