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

package org.jacorb.orb.giop;

import org.jacorb.orb.*;

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
    protected final Object lock = new Object();
    protected boolean ready = false;
    protected boolean communicationException = false;
    protected boolean remarshalException = false;
    protected boolean timeoutException = false;

    protected MessageInputStream in = null;

    protected final int timeout ;

    /**
     * self-configuring c'tor
     */

    public ReplyPlaceholder(ORB orb)
    {
        timeout =
            orb.getConfiguration().getAttributeAsInteger("jacorb.connection.client.pending_reply_timeout", 0);
    }

    public void replyReceived( MessageInputStream in )
    {
        synchronized(lock)
        {
            if( ! timeoutException )
            {
                this.in = in;
                ready = true;
                lock.notifyAll();
            }
        }
    }

    public void cancel()
    {
        synchronized(lock)
        {
            if( in == null )
            {
                communicationException = true;
                ready = true;
                lock.notify();
            }
        }
    }

    public void retry()
    {
        synchronized(lock)
        {
            remarshalException = true;
            ready = true;
            lock.notify();
        }
    }

    /**
     * Non-public implementation of the blocking method that
     * returns a reply when it becomes available.  Subclasses
     * should specify a different method, under a different
     * name, that does any specific processing of the reply before
     * returning it to the caller.
     */
    protected MessageInputStream getInputStream(boolean hasTimeoutPolicy)
        throws RemarshalException
    {
        final boolean _shouldUseTimeout = !hasTimeoutPolicy && timeout > 0;
        final long _maxWait = _shouldUseTimeout ? System.currentTimeMillis() + timeout : Long.MAX_VALUE;
        final long _timeout = _shouldUseTimeout ? timeout : 0;

        synchronized(lock)
        {
            try
            {
                while(!ready && System.currentTimeMillis() < _maxWait)
                {
                    lock.wait( _timeout );
                }
            }
            catch( InterruptedException e )
            {
                // ignored
            }

            if (!ready && _shouldUseTimeout)
            {
                timeoutException = true;
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
    }
}
