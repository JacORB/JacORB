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

import org.jacorb.orb.*;
import org.jacorb.util.*;

import org.omg.GIOP.*;
import org.omg.CORBA.portable.RemarshalException;

/**
 * ReplyPlaceholder.java
 *
 *
 * Created: Sat Aug 18 21:43:19 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ReplyPlaceholder 
{
    private boolean ready = false;
    private boolean communicationException = false;
    private boolean remarshalException = false;
    private boolean timeoutException = false;

    private MessageInputStream in = null;

    private int timeout = -1;

    public ReplyPlaceholder()
    {        
        //get the client-side timeout property value
        String prop = 
            Environment.getProperty( "jacorb.connection.client_timeout" );
        
        if( prop != null )
        {
            try
            {
                timeout = Integer.parseInt(prop);
            }
            catch( NumberFormatException nfe )
            {
                Debug.output( 1, "Unable to create int from string >" +
                              prop + '<' );
                Debug.output( 1, "Please check property \"jacorb.connection.client_timeout\"" );
            }
        }

    }
    
    public synchronized void replyReceived( MessageInputStream in )
    {
        if( ! timeoutException )
        {
            this.in = in;
        
            ready = true;
            
            notifyAll();
        }
    }
        
    public synchronized void cancel()
    {
	communicationException = true;
	ready = true;
	this.notify();
    }


    public synchronized void retry()
    {
	remarshalException = true;
	ready = true;
	this.notify();
    }

    public synchronized void timeout()
    {
	timeoutException = true;
	ready = true;
	this.notify();
    }

    public synchronized MessageInputStream getInputStream() 
	throws RemarshalException
    {
        while( !ready ) 
        {
            try
            {
                if( timeout > 0 )
                {
                    wait( timeout ); //wait only "timeout" long
                    
                    //timeout
                    if( ! ready )
                    {
                        ready = true; //break loop
                        timeoutException = true;
                    }
                }
                else
                {
                    wait(); //wait infinitely
                }
            } 
            catch( InterruptedException e )
            {}
        }

        if( remarshalException )
	{
	    throw new org.omg.CORBA.portable.RemarshalException();
	}	

        if( communicationException )
	{
            throw new org.omg.CORBA.COMM_FAILURE();
	}

        if( timeoutException )
        {
            throw new org.omg.CORBA.IMP_LIMIT("Client timeout reached.");
        }
                
        return in;
    }
}// ReplyPlaceholder














