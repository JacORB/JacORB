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

import java.util.Set;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.giop.MessageInputStream;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.ReplyPlaceholder;
import org.jacorb.util.SelectorManager;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.jacorb.util.Time;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.Messaging.ExceptionHolder;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.TimeBase.UtcT;
import org.slf4j.Logger;

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
 */

public final class ReplyReceiver
        extends ReplyPlaceholder
        implements Configurable
{
    private final org.jacorb.orb.Delegate  delegate;
    private final ClientInterceptorHandler interceptors;

    private final org.omg.Messaging.ReplyHandler replyHandler;

    private final String operation;
    private final Timer timer;
    private final SelectorTimer selectorTimer;
    private final SelectorRequest timeoutRequest;
    private final SelectorManager selectorManager;
    private UtcT replyEndTime = null;

    private Logger logger;
    private ReplyGroup group;

    /** configuration properties */
    private boolean retry_on_failure = false;

    public ReplyReceiver( org.jacorb.orb.Delegate        delegate,
			  ReplyGroup                     group,
                          String                         operation,
                          org.omg.TimeBase.UtcT          replyEndTime,
                          ClientInterceptorHandler       interceptors,
                          org.omg.Messaging.ReplyHandler replyHandler,
                          SelectorManager                selectorManager)
    {
	this.group = group;

        this.delegate         = delegate;
        this.operation        = operation;
        this.interceptors     = interceptors;
        this.replyHandler     = replyHandler;
        this.replyEndTime     = replyEndTime;
        this.selectorManager  = selectorManager;

        if (replyEndTime != null)
        {
            if (selectorManager == null)
            {
                selectorTimer = null;
                timeoutRequest = null;
                timer = new Timer(replyEndTime);
                timer.setName("ReplyReceiver Timer" );
                timer.start();
            }
            else
            {
                timer = null;
                selectorTimer = new SelectorTimer ();
                long duration = org.jacorb.util.Time.millisTo (replyEndTime);
                timeoutRequest = new SelectorRequest (selectorTimer,
                                                      System.nanoTime() + duration*1000000);
                selectorManager.add (timeoutRequest);
            }
        }
        else
        {
            timer = null;
            selectorTimer = null;
            timeoutRequest = null;
        }
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
       super.configure (configuration);

        logger = configuration.getLogger("jacorb.orb.rep_recv");
        retry_on_failure = configuration.getAttributeAsBoolean("jacorb.connection.client.retry_on_failure", false);
    }


    public void replyReceived( MessageInputStream in )
    {
        if (timeoutException)
        {
            return; // discard reply
        }

        if (replyEndTime != null)
        {
            if (selectorTimer != null)
            {
                selectorManager.remove(timeoutRequest);
                selectorTimer.wakeup ();

            }
            else
            {
                timer.wakeup();
            }
        }

	if (group != null)
	{
            Set pending = group.getReplies();
	    // grab pending_replies lock BEFORE my own,
	    // then I will already have it in the replyDone call below.
	    synchronized ( pending )
            {
		// This internal synchronization prevents a deadlock
		// when a timeout and a reply coincide, suggested
		// by Jimmy Wilson, 2005-01.  It is only a temporary
		// work-around though, until I can simplify this entire
		// logic much more thoroughly, AS.
		synchronized (lock)
		{
		    if (timeoutException)
		    {
			return; // discard reply
		    }

		    this.in = in;
		    pending.remove (this);

		    if (replyHandler != null)
		    {
			// asynchronous delivery
			performCallback ((ReplyInputStream)in);
		    }
		    else
		    {
			// synchronous delivery
			ready = true;
			lock.notifyAll();
		    }
		}
	    }
	}
	else
	{
	    synchronized (lock)
	    {
		if (timeoutException)
	        {
		    return; // discard reply
		}

		this.in = in;

		if (replyHandler != null)
		{
		    // asynchronous delivery
		    performCallback ((ReplyInputStream)in);
		}
		else
		{
		    // synchronous delivery
		    ready = true;
		    lock.notifyAll();
		}
	    }
	}
    }

    private void performCallback ( ReplyInputStream reply )
    {
        /**
         * Calls to interceptors are now done in the servant_preinvoke
         * method.  When handling local calls using the servant_preinvoke
         * and servant_postinvoke method the pre invocation interceptor
         * calls are done in servant_preinvoke, the invocation is then
         * made and the server side response interception calls are done
         * in the normalCompletion/exceptionalCompletion methods in the
         * ServantObject.  These methods are normally called by the stubs
         * but in this case there is no stub so the calls must be made
         * here.  The servant_postinvoke method will call the client
         * interception points according to the reply e.g. successful,
         * exception etc.
         */

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
                        new ExceptionHolderImpl((ORB) delegate.orb(null), reply );

                    org.omg.CORBA_2_3.ORB orb = ( org.omg.CORBA_2_3.ORB )replyHandlerDelegate.orb( null );

                    orb.register_value_factory
                    ( "IDL:omg.org/Messaging/ExceptionHolder:1.0",
                      new ExceptionHolderFactory((ORB)orb) );

                    CDRInputStream input =
                        new CDRInputStream( orb, holder.marshal() );

                    ((InvokeHandler)so.servant)
                    ._invoke( operation + "_excep",
                              input,
                              new DummyResponseHandler() );
                    break;
                }
            }

            if (so instanceof org.omg.CORBA.portable.ServantObjectExt)
            {
                ( (org.omg.CORBA.portable.ServantObjectExt)so).normalCompletion();
            }
        }
        catch ( Exception e )
        {
            logger.warn("Exception during callback", e);

            if (so instanceof org.omg.CORBA.portable.ServantObjectExt)
            {
                ( (org.omg.CORBA.portable.ServantObjectExt)
                  so).exceptionalCompletion (e);
            }
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
        /**
         * Calls to interceptors are now done in the servant_preinvoke
         * method.  When handling local calls using the servant_preinvoke
         * and servant_postinvoke method the pre invocation interceptor
         * calls are done in servant_preinvoke, the invocation is then
         * made and the server side response interception calls are done
         * in the normalCompletion/exceptionalCompletion methods in the
         * ServantObject.  These methods are normally called by the stubs
         * but in this case there is no stub so the calls must be made
         * here.  The servant_postinvoke method will call the client
         * interception points according to the reply e.g. successful,
         * exception etc.
         */

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
                  new ExceptionHolderFactory((ORB) orb));

            CDRInputStream input =
                new CDRInputStream( orb, holder.marshal() );

            ((InvokeHandler)so.servant)
                ._invoke( operation + "_excep",
                          input,
                          new DummyResponseHandler() );

            if (so instanceof org.omg.CORBA.portable.ServantObjectExt)
            {
                ( (org.omg.CORBA.portable.ServantObjectExt)so).normalCompletion();
            }
        }
        catch ( Exception e )
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Exception during callback: " + e.toString() );
            }

            if (so instanceof org.omg.CORBA.portable.ServantObjectExt)
            {
                ( (org.omg.CORBA.portable.ServantObjectExt)
                  so).exceptionalCompletion (e);
            }
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
                getInputStream (replyEndTime != null);  // block until reply is available
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
            try
            {
                interceptors.handle_receive_exception( se );
            }
            catch (ForwardRequest fwd)
            {
                //should  not happen with a remote request
            }

            throw se;
        }
        catch ( RemarshalException re )
        {
            // Wait until the thread that received the actual
            // forward request rebound the Delegate
            group.waitOnBarrier();
            throw new RemarshalException();
        }

        final ReplyInputStream reply = ( ReplyInputStream ) in;

        final ReplyStatusType_1_2 status = reply.getStatus();

        switch ( status.value() )
        {
            case ReplyStatusType_1_2._NO_EXCEPTION:
            {
                try
                {
                    interceptors.handle_receive_reply ( reply );
                }
                catch (ForwardRequest fwd)
                {
                    // should not happen with a remote request
                }

                checkTimeout();
                return reply;
            }
            case ReplyStatusType_1_2._USER_EXCEPTION:
            {
                ApplicationException ae = getApplicationException ( reply );
                try
                {
                    interceptors.handle_receive_exception( ae, reply );
                }
                catch (ForwardRequest fwd)
                {
                    // should not happen with a remote request
                }

                checkTimeout();
                throw ae;
            }
            case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
            {
                SystemException se = SystemExceptionHelper.read ( reply );
                try
                {
                    interceptors.handle_receive_exception( se, reply );
                }
                catch (ForwardRequest fwd)
                {
                    // should not happen with a remote request
                }

                checkTimeout();
                throw se;
            }
            case ReplyStatusType_1_2._LOCATION_FORWARD:
            case ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
            {
                org.omg.CORBA.Object forward_reference = reply.read_Object();
                try
                {
                    interceptors.handle_location_forward( reply, forward_reference );
                }
                catch (ForwardRequest fwd)
                {
                    // should not happen with a remote request
                }

                checkTimeout();
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

    /**
     * This method is used to check that if there is a timeout set for this request that is
     * has elapsed while an interceptor was invoked.
     */
    private void checkTimeout()
    {
        if (replyEndTime != null && Time.hasPassed (replyEndTime))
        {
            throw new TIMEOUT("Reply End Time exceeded",
                              3,
                              CompletionStatus.COMPLETED_NO);
        }
    }

    private void doRebind ( org.omg.CORBA.Object forward_reference )
    {
        // make other threads that have unreturned replies wait
        group.lockBarrier();

        try
        {
            // tell every pending request to remarshal
            // they will be blocked on the barrier
	    group.retry();

            // do the actual rebind
            delegate.rebind ( forward_reference );
        }
        finally
        {
            // now other threads can safely remarshal
            group.openBarrier();
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
        catch ( java.io.IOException ioe )
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
        private final ORB orb;

        public ExceptionHolderFactory(ORB orb)
        {
            this.orb = orb;
        }

        public java.io.Serializable read_value
            ( org.omg.CORBA_2_3.portable.InputStream is )
        {
            ExceptionHolder result = new ExceptionHolderImpl(orb);
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
        private final UtcT endTime;
        private boolean awakened = false;

        public Timer (UtcT endTime)
        {
            super("ReplyReceiverTimer");
            this.endTime = endTime;
        }

        public void run()
        {
            synchronized (lock)
            {
                timeoutException = false;
                if (!awakened)
                {
                    long time = org.jacorb.util.Time.millisTo (endTime);
                    if (time > 0)
                    {
                        try
                        {
                            lock.wait (time);
                        }
                        catch (InterruptedException ex)
                        {
                            logger.info("Interrupted while waiting for timeout");
                        }
                    }
                    if (!awakened)
                    {
                        timeoutException = true;

                        if (replyHandler != null)
                        {
                            ExceptionHolderImpl exHolder =
                                new ExceptionHolderImpl((ORB)delegate.orb(null), new org.omg.CORBA.TIMEOUT());
                            performExceptionCallback(exHolder);
                        }
                        ready = true;
                        lock.notifyAll();
                    }
                }
            }
        }

        public void wakeup()
        {
            synchronized (lock)
            {
                awakened         = true;
                timeoutException = false;
                lock.notifyAll();
            }
        }
    }

    /**
     * And alternative timer helper. This one integrates with the
     * SelectorManager framework, extending the SelectorRequestCallback.
     * The timer is computed as a wait limit for the selector when the
     * timeout event is registered.
     * When the timeout goes off, this Timer makes sure
     * that the enclosing ReplyReceiver is deactivated, and that
     * everybody associated with it is notified appropriately.
     * The timeout can be cancelled by calling wakeup() on a Timer.
     */
    class SelectorTimer extends SelectorRequestCallback
    {

        private boolean awakened = false;

        public boolean call (SelectorRequest request)
        {

            if (logger.isDebugEnabled())
            {
                logger.debug ("Request callback. Request type: " + request.type.toString()
                              + ", request status: " + request.status.toString());
            }

            synchronized (lock)
            {
                if (request.status == SelectorRequest.Status.EXPIRED)
                {
                    if (!awakened)
                    {
                        timeoutException = true;

                        if (replyHandler != null)
                        {
                            ExceptionHolderImpl exHolder =
                                new ExceptionHolderImpl((ORB)delegate.orb(null), new org.omg.CORBA.TIMEOUT());
                            performExceptionCallback(exHolder);
                        }
                    }
                }
                else
                {
                    // something bad happened (SHUTDOWN, FAILED throw a COM_FAILURE)
                    communicationException = true;
                }

                ready = true;
                lock.notifyAll();
            }

            return false;
        }

        private void wakeup ()
        {
            synchronized (lock)
            {
                awakened         = true;
                timeoutException = false;
                lock.notifyAll();
            }
        }
    }

}
