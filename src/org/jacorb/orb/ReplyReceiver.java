package org.jacorb.orb;

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

import org.jacorb.orb.connection.MessageInputStream;
import org.jacorb.orb.connection.ReplyInputStream;
import org.jacorb.orb.connection.ReplyPlaceholder;
import org.jacorb.util.*;

import org.omg.GIOP.*;
import org.omg.Messaging.ExceptionHolder;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.ServantObject;

import java.util.*;

/**
 * A special ReplyPlaceholder that receives replies to normal requests,
 * either synchronously or asynchronously.  A ReplyReceiver
 * handles all ORB-internal work that needs to be done for the reply, 
 * such as checking for exceptions and invoking the interceptors.
 * The client stub can either do a blocking wait on the ReplyReceiver 
 * (via getReply()), or a ReplyHandler can be supplied when the 
 * ReplyReceiver is created; then the reply is delivered to that 
 * ReplyHandler. 
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class ReplyReceiver extends ReplyPlaceholder
{
    private org.jacorb.orb.Delegate  delegate     = null;
    private ClientInterceptorHandler interceptors = null;

    private org.omg.Messaging.ReplyHandler replyHandler = null;

    private String operation;
    
    private SystemException      systemException      = null;
    private ApplicationException applicationException = null;

    private static final ResponseHandler dummyResponseHandler =
                                                new DummyResponseHandler();

    public ReplyReceiver( org.jacorb.orb.Delegate        delegate,
                          String                         operation,
                          ClientInterceptorHandler       interceptors,
                          org.omg.Messaging.ReplyHandler replyHandler )
    {        
        this.delegate     = delegate;
        this.operation    = operation;
        this.interceptors = interceptors;
        this.replyHandler = replyHandler;
    }

    public synchronized void replyReceived ( MessageInputStream in )
    {
        if ( timeoutException )
            return; // discard reply

        this.in = in;

        if ( replyHandler != null )
        {
            performCallback ( (ReplyInputStream)in );
        }

        Set pending_replies = delegate.get_pending_replies();
        synchronized ( pending_replies )
        {            
            pending_replies.remove ( this );
        }

        ready   = true;
        notifyAll();
    }       

    private void performCallback ( ReplyInputStream reply )
    {
        // TODO: Call interceptors.
        
        ServantObject so = delegate.servant_preinvoke( replyHandler,
                                                       operation,
                                                       InvokeHandler.class );
        try
        {
            switch ( reply.getStatus().value() )
            {
                case ReplyStatusType_1_2._NO_EXCEPTION:
                {                                                    
                    ((InvokeHandler)so.servant)._invoke( operation,
                                                         reply,
                                                         dummyResponseHandler );
                    break;
                }
                case ReplyStatusType_1_2._USER_EXCEPTION:
                case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
                {
                    ExceptionHolderImpl holder = 
                        new ExceptionHolderImpl( reply );

                    org.omg.CORBA_2_3.ORB orb = 
                        ( org.omg.CORBA_2_3.ORB ) delegate.orb( null );
                    orb.register_value_factory
                        ( "IDL:omg.org/Messaging/ExceptionHolder:1.0",
                          new ExceptionHolderFactory() );

                    CDRInputStream input = 
                        new CDRInputStream( orb, holder.marshal() );

                    ((InvokeHandler)so.servant)._invoke( operation + "_excep",
                                                         input,
                                                         dummyResponseHandler );
                    break;                
                }
            }
        }
        catch ( Exception e )
        {
            Debug.output( Debug.IMPORTANT, 
                          "Exception during callback: " + e );
        }
        finally
        {
            delegate.servant_postinvoke( replyHandler, so );
        }
    }

    /**
     * This method blocks until a reply becomes available.
     * If the reply contains any exceptions, they are rethrown.
     */
    public synchronized ReplyInputStream getReply()
        throws RemarshalException, ApplicationException
    {
        try
        {
            super.getInputStream();  // block until reply is available
        }
        catch ( SystemException se )
        {
            interceptors.handle_receive_exception( se );
            throw se;
        }
        catch ( RemarshalException re )
        {
            // Wait until the thread that received the actual
            // forward request rebound the Delegate
            delegate.waitOnBarrier();
            throw new RemarshalException();   
        }

        ReplyInputStream reply = ( ReplyInputStream ) in;
        
        ReplyStatusType_1_2 status = delegate.doNotCheckExceptions()
                                     ? ReplyStatusType_1_2.NO_EXCEPTION
                                     : reply.getStatus();
                                       
        switch ( status.value() )
        {
            case ReplyStatusType_1_2._NO_EXCEPTION:
            {
                interceptors.handle_receive_reply ( reply );
                return reply;
            }
            case ReplyStatusType_1_2._USER_EXCEPTION:
            {
                ApplicationException ae = getApplicationException ( reply );
                interceptors.handle_receive_exception( ae, reply );
                throw ae;
            }
            case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
            {
                SystemException se = SystemExceptionHelper.read ( reply );
                interceptors.handle_receive_exception( se, reply );
                throw se;
            }
            case ReplyStatusType_1_2._LOCATION_FORWARD:
            case ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
            {
                org.omg.CORBA.Object forward_reference = reply.read_Object();
                interceptors.handle_location_forward( reply, forward_reference );
                doRebind( forward_reference );
                throw new RemarshalException();               
            }
            case ReplyStatusType_1_2._NEEDS_ADDRESSING_MODE:
            {
                throw new org.omg.CORBA.NO_IMPLEMENT( 
                            "WARNING: Got reply status NEEDS_ADDRESSING_MODE "
                          + "(not implemented)." );
            }
            default:
            {
                throw new Error( "Received unexpected reply status: " +
                                 status.value() );
            }                
        }
    }

    private void doRebind ( org.omg.CORBA.Object forward_reference )
    {
        // make other threads that have unreturned replies wait
        delegate.lockBarrier();

        // tell every pending request to remarshal
        // they will be blocked on the barrier
        Set pending_replies = delegate.get_pending_replies();
        synchronized ( pending_replies )
        {
            for ( Iterator i = pending_replies.iterator(); i.hasNext(); )
            {
                ReplyPlaceholder p = ( ReplyPlaceholder ) i.next();
                p.retry();
            }
        }
            
        // do the actual rebind
        delegate.rebind ( forward_reference );
            
        // now other threads can safely remarshal
        delegate.openBarrier();
    }

    private ApplicationException getApplicationException ( ReplyInputStream reply )
    {
        reply.mark( 0 ); 
        String id = reply.read_string();
                
        try
        {
            reply.reset();
        }
        catch( java.io.IOException ioe )
        {
            //should not happen anyway
            Debug.output( 1, ioe );
        }

        return new ApplicationException( id, reply );
    }
    
    private static class DummyResponseHandler 
        implements org.omg.CORBA.portable.ResponseHandler
    {
        public org.omg.CORBA.portable.OutputStream createReply() 
        {
            return null;
        }
        
        public org.omg.CORBA.portable.OutputStream createExceptionReply() 
        {
            return null;
        }
    }
    
    private static class ExceptionHolderFactory
        implements org.omg.CORBA.portable.ValueFactory
    {
        public java.io.Serializable read_value
                        ( org.omg.CORBA_2_3.portable.InputStream is )
        {
            ExceptionHolder result = new ExceptionHolderImpl();
            result._read( is );
            return result;
        }
    }

}














