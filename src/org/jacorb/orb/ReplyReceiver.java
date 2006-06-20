package org.jacorb.orb;

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

import java.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.jacorb.orb.giop.MessageInputStream;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.ReplyPlaceholder;
import org.jacorb.util.Time;

import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.Messaging.ExceptionHolder;
import org.omg.TimeBase.UtcT;

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

public class ReplyReceiver
    extends ReplyPlaceholder
    implements Configurable
{
    private org.jacorb.orb.Delegate  delegate     = null;
    private ClientInterceptorHandler interceptors = null;

    private org.omg.Messaging.ReplyHandler replyHandler = null;

    private String operation;
    private Timer  timer;

    private Logger logger;

    /** configuration properties */
    private boolean retry_on_failure = false;


    public ReplyReceiver( org.jacorb.orb.Delegate        delegate,
                          String                         operation,
                          org.omg.TimeBase.UtcT          replyEndTime,
                          ClientInterceptorHandler       interceptors,
                          org.omg.Messaging.ReplyHandler replyHandler )
    {
        super((org.jacorb.orb.ORB)delegate.orb(null));

        this.delegate         = delegate;
        this.operation        = operation;
        this.interceptors     = interceptors;
        this.replyHandler     = replyHandler;

        if (replyEndTime != null)
        {
            timer = new Timer(replyEndTime);
            timer.setName("ReplyReceiver Timer" );
            timer.start();
        }
        else
        {
            timer = null;
        }

    }

    public void configure(org.apache.avalon.framework.configuration.Configuration configuration)
        throws org.apache.avalon.framework.configuration.ConfigurationException
    {
        logger =
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.orb.rep_recv");
        retry_on_failure =
            configuration.getAttribute("jacorb.connection.client.retry_on_failure","off").equals("on");
    }


    public void replyReceived( MessageInputStream in )
    {
        if (timeoutException)
            return; // discard reply
        if (timer != null)
            timer.wakeup();

        Set pending_replies = delegate.get_pending_replies();
        // grab pending_replies lock BEFORE my own,
        // then I will already have it in the replyDone call below.
        synchronized ( pending_replies )
        {
        // This internal synchronization prevents a deadlock
        // when a timeout and a reply coincide, suggested
        // by Jimmy Wilson, 2005-01.  It is only a temporary
        // work-around though, until I can simplify this entire
        // logic much more thoroughly, AS.
        synchronized (this)
        {
            if (timeoutException)
                return; // discard reply

            this.in = in;
            delegate.replyDone (this);

            if (replyHandler != null)
            {
                // asynchronous delivery
                performCallback ((ReplyInputStream)in);
            }
            else
            {
                // synchronous delivery
                ready = true;
                notifyAll();
            }
        }
        }
    }

    private void performCallback ( ReplyInputStream reply )
    {
        // TODO: Call interceptors.

        org.omg.CORBA.portable.Delegate replyHandlerDelegate =
            ( ( org.omg.CORBA.portable.ObjectImpl ) replyHandler )
                                                         ._get_delegate();

        ServantObject so =
            replyHandlerDelegate.servant_preinvoke( replyHandler,
                                                    operation,
                                                    InvokeHandler.class );
        try
        {
            switch ( reply.getStatus().value() )
            {
                case ReplyStatusType_1_2._NO_EXCEPTION:
                {
                    ((InvokeHandler)so.servant)
                        ._invoke( operation,
                                  reply,
                                  new DummyResponseHandler() );
                    break;
                }
                case ReplyStatusType_1_2._USER_EXCEPTION:
                case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
                {
                    ExceptionHolderImpl holder =
                        new ExceptionHolderImpl( reply );

                    org.omg.CORBA_2_3.ORB orb =
                        ( org.omg.CORBA_2_3.ORB )replyHandlerDelegate
                                                              .orb( null );
                    orb.register_value_factory
                        ( "IDL:omg.org/Messaging/ExceptionHolder:1.0",
                          new ExceptionHolderFactory() );

                    CDRInputStream input =
                        new CDRInputStream( orb, holder.marshal() );

                    ((InvokeHandler)so.servant)
                        ._invoke( operation + "_excep",
                                  input,
                                  new DummyResponseHandler() );
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            logger.warn("Exception during callback", e);
        }
        finally
        {
            replyHandlerDelegate.servant_postinvoke( replyHandler, so );
        }
    }

    /**
     * There's a lot of code duplication in this method right now.
     * This should be merged with performCallback() above.
     */
    private void performExceptionCallback (ExceptionHolderImpl holder)
    {
        // TODO: Call interceptors.

        org.omg.CORBA.portable.Delegate replyHandlerDelegate =
            ( ( org.omg.CORBA.portable.ObjectImpl ) replyHandler )
                                                         ._get_delegate();

        ServantObject so =
            replyHandlerDelegate.servant_preinvoke( replyHandler,
                                                    operation,
                                                    InvokeHandler.class );
        try
        {
            org.omg.CORBA_2_3.ORB orb =
                    ( org.omg.CORBA_2_3.ORB )replyHandlerDelegate
                                                              .orb( null );
            orb.register_value_factory
                ( "IDL:omg.org/Messaging/ExceptionHolder:1.0",
                  new ExceptionHolderFactory() );

            CDRInputStream input =
                new CDRInputStream( orb, holder.marshal() );

            ((InvokeHandler)so.servant)
                ._invoke( operation + "_excep",
                          input,
                          new DummyResponseHandler() );
        }
        catch ( Exception e )
        {
            if (logger.isWarnEnabled())
                logger.warn("Exception during callback: " + e.toString() );
        }
        finally
        {
            replyHandlerDelegate.servant_postinvoke( replyHandler, so );
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
           // On NT connection closure due to service shutdown is not
           // detected until this point, resulting in a COMM_FAILURE.
           // Map to RemarshalException to force rebind attempt.
           try
           {
               getInputStream(timer != null);  // block until reply is available
           }
           catch (org.omg.CORBA.COMM_FAILURE ex)
           {
               if (retry_on_failure)
               {
                   throw new RemarshalException();
               }
               //rethrow
               throw ex;
           }
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
                throw new MARSHAL
                    ("Received unexpected reply status: " + status.value() );
            }
        }
    }

    private void doRebind ( org.omg.CORBA.Object forward_reference )
    {
        try
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
        }
        finally
        {
            // now other threads can safely remarshal
            delegate.openBarrier();
        }
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
            logger.error("unexpected Exception in reset()", ioe );
        }

        return new ApplicationException( id, reply );
    }

    /**
     * A ResponseHandler that is passed to the ReplyHandler's POA
     * when we invoke it.  Since ReplyHandler operations never generate
     * replies, this ResponseHandler does nothing to this effect.
     * The createReply() method, however, is the last method that
     * is called before control goes to the ReplyHandler servant,
     * so we use it to check for timing constraints.
     */
    private class DummyResponseHandler
        implements org.omg.CORBA.portable.ResponseHandler
    {
        public org.omg.CORBA.portable.OutputStream createReply()
        {
            // the latest possible time at which we can do this
            Time.waitFor (delegate.getReplyStartTime());
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

    /**
     * This class implements timeouts while we are waiting for
     * replies.  When it is instantiated, it takes a CORBA UtcT
     * constructor parameter that specifies the timeout expiration
     * time.  The timer starts running as soon as the Thread is
     * started.  When the timeout goes off, this Timer makes sure
     * that the enclosing ReplyReceiver is deactivated, and that
     * everybody associated with it is notified appropriately.
     * The timeout can be cancelled by calling wakeup() on a Timer.
     */
    private class Timer extends Thread
    {
        private boolean awakened = false;
        private UtcT    endTime;

        public Timer (UtcT endTime)
        {
            super("ReplyReceiverTimer");
            this.endTime = endTime;
        }

        public void run()
        {
            synchronized (this)
            {
                ReplyReceiver.this.timeoutException = false;
                if (!awakened)
                {
                    long time = org.jacorb.util.Time.millisTo (endTime);
                    if (time > 0)
                    {
                        try
                        {
                            this.wait (time);
                        }
                        catch (InterruptedException ex)
                        {
                            if (logger.isInfoEnabled())
                                logger.info("Interrupted while waiting for timeout");
                        }
                    }
                    if (!awakened)
                    {
                        synchronized (ReplyReceiver.this)
                        {
                            ReplyReceiver.this.timeoutException = true;

                            if (replyHandler != null)
                            {
                                ExceptionHolderImpl exHolder =
                                    new ExceptionHolderImpl(new org.omg.CORBA.TIMEOUT());
                                performExceptionCallback(exHolder);
                            }
                            ReplyReceiver.this.ready = true;
                            ReplyReceiver.this.notifyAll();
                        }
                    }
                }
            }
        }

        public void wakeup()
        {
            synchronized (this)
            {
                awakened         = true;
                ReplyReceiver.this.timeoutException = false;
                this.notifyAll();
            }
        }
    }
}
