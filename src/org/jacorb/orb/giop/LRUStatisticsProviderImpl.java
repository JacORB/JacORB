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


/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class LRUStatisticsProviderImpl
    implements StatisticsProvider
{
    private long last_usage = 0;
    
    /**
     * A message chunk with the given size has been sent over the associated
     * Transport.  
     */
    public void messageChunkSent( int size )
    {
    }

    /**
     * The transport has been flushed. This means that sending of a
     * message is complete.  
     */
    public void flushed()
    {
        last_usage = System.currentTimeMillis();
    }


    /**
     * A message with the given size has been received by the
     * associated Transport.  
     */
    public void messageReceived( int size )
    {
        last_usage = System.currentTimeMillis();
    }
    
    public long getLastUsage()
    {
        return last_usage;
    }
}



