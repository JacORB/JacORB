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
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ApplicationException;

import java.util.*;

/**
 * A special ReplyPlaceholder that is used for normal client requests,
 * which can be both synchronous and asynchronous.  A ReplyReceiver
 * handles all ORB-internal work that needs to be done for the reply, 
 * such as checking for exceptions and invoking the interceptors.
 * The client stub can either do a blocking wait on the ReplyReceiver 
 * (via getInputStream()), or a ReplyHandler can be supplied when the 
 * ReplyReceiver is created; then the reply is delivered to that 
 * ReplyHandler. 
 *
 * @author Andre Spiegel
 * @version $Id$
 */
public class ReplyReceiver extends ReplyPlaceholder
{
    private org.jacorb.orb.Delegate  delegate     = null;
    private ClientInterceptorHandler interceptors = null;

    private org.omg.Messaging.ReplyHandler replyHandler = null;
    
    private SystemException      systemException      = null;
    private ApplicationException applicationException = null;

    public ReplyReceiver( org.jacorb.orb.Delegate        delegate,
                          ClientInterceptorHandler       interceptors,
                          org.omg.Messaging.ReplyHandler replyHandler )
    {        
        this.delegate     = delegate;
        this.interceptors = interceptors;
        this.replyHandler = replyHandler;
    }

    public synchronized void replyReceived ( MessageInputStream in )
    {
        if ( timeoutException )
            return; // discard reply

        this.in = in;
        ReplyInputStream reply = ( ReplyInputStream ) in;
        Set pending_replies    = delegate.get_pending_replies();

        try
        {
            if ( !delegate.doNotCheckExceptions() )
            {
                // This will check the reply status and
                // throw arrived exceptions
                reply.checkExceptions();
            }
            interceptors.handle_receive_reply ( reply );
        }
        catch ( RemarshalException re )
        {
            // Wait until the thread that received the actual
            // forward request rebound the Delegate
            delegate.waitOnBarrier();
            remarshalException = true;
        }            
        catch ( org.omg.PortableServer.ForwardRequest f )
        {
            intercept_location_forward ( reply, f.forward_reference );
            
            // make other threads that have unreturned replies wait
            delegate.lockBarrier();

            // tell every pending request to remarshal
            // they will be blocked on the barrier
            synchronized ( pending_replies )
            {
                for ( Iterator i = pending_replies.iterator(); i.hasNext(); )
                {
                    ReplyPlaceholder p = ( ReplyPlaceholder ) i.next();
                    p.retry();
                }
            }
            
            // do the actual rebind
            delegate.rebind ( f.forward_reference );
            
            // now other threads can safely remarshal
            delegate.openBarrier();
            
            remarshalException = true;
        }
        catch ( SystemException se )
        {
            intercept_exception ( se, reply );
            systemException = se;
        }
        catch ( ApplicationException ae )
        {
            intercept_exception ( ae, reply );
            applicationException = ae;
        }
            
        pending_replies.remove ( this );
        ready   = true;
        notifyAll();
    }       

    public synchronized ReplyInputStream getReplyInputStream()
        throws RemarshalException, ApplicationException
    {
        // Call to super implementation handles RemarshalException,
        // COMM_FAILURE, or timeout (IMP_LIMIT).
        MessageInputStream result = super.getInputStream();

        if ( systemException != null )
        {
            throw systemException;
        }
        else if ( applicationException != null )
        {
            throw applicationException;
        }
        else
        {
            return ( ReplyInputStream ) result;
        }
    }

    private void intercept_location_forward 
                                    ( ReplyInputStream reply,
                                      org.omg.CORBA.Object forward_reference )
    {
        try
        {
            interceptors.handle_location_forward ( reply, forward_reference );
        }
        catch ( RemarshalException re )
        {
            remarshalException = true;
        }
    }
    
    private void intercept_exception ( SystemException ex, 
                                       ReplyInputStream reply )
    {
        try
        {
            interceptors.handle_receive_exception ( ex, reply );    
        }
        catch ( RemarshalException re )
        {
            remarshalException = true;
        }
    }

    private void intercept_exception ( ApplicationException ex, 
                                       ReplyInputStream reply )
    {
        try
        {
            interceptors.handle_receive_exception ( ex, reply );    
        }
        catch ( RemarshalException re )
        {
            remarshalException = true;
        }
    }
}














