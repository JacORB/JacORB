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

package org.jacorb.orb.connection;


/**
 * This class is used to collect statistical data about
 * transports. Since the nature of the data is specific to the type of
 * the statistics, no method is provided for access to data. Instead,
 * the accessor method is to be defined in the implementation, that
 * works together with the SelectionStrategy.
 *
 * @author Nicolas Noffke
 * @version $Id$ */

public interface StatisticsProvider 
{
    /**
     * A message chunk with the given size has been sent over the associated
     * Transport.  
     */
    public void messageChunkSent( int size );

    /**
     * The transport has been flushed. This means that sending of a
     * message is complete.  
     */
    public void flushed();

    /**
     * A message with the given size has been received by the
     * associated Transport.  
     */
    public void messageReceived( int size );
}// StatisticsProvider



