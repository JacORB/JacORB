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

import org.jacorb.orb.*;
import org.jacorb.util.*;

import org.omg.GIOP.*;
import org.omg.CORBA.portable.RemarshalException;

/**
 * Connections deliver replies to instances of this class.
 * The mechanism by which the ORB can retrieve the replies is
 * implemented in subclasses.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */
public abstract class ReplyPlaceholder 
{
    protected boolean ready = false;
    protected boolean communicationException = false;
    protected boolean remarshalException = false;
    protected boolean timeoutException = false;

    protected MessageInputStream in = null;

    protected int timeout = -1;

    public ReplyPlaceholder()
    {
        //get the client-side timeout property value
        String prop = 
            Environment.getProperty( "jacorb.client.pending_reply_timeout" );
        
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
                Debug.output( 1, "Please check property \"jacorb.client.pending_reply_timeout\"" );
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
        if( in == null )
        {
            communicationException = true;
            ready = true;
            this.notify();
        }
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

    /**
     * Non-public implementation of the blocking method that
     * returns a reply when it becomes available.  Subclasses
     * should specify a different method, under a different
     * name, that does any specific processing of the reply before
     * returning it to the caller.
     */
    protected synchronized MessageInputStream getInputStream() 
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
            throw new org.omg.CORBA.COMM_FAILURE(
                0, 
                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
        }

        if( timeoutException )
        {
            throw new org.omg.CORBA.TIMEOUT ("client timeout reached");
        }
                
        return in;
    }
}// ReplyPlaceholder














