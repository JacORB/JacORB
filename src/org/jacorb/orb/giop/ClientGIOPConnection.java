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

import org.apache.avalon.framework.configuration.*;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ClientGIOPConnection
    extends GIOPConnection
    implements Configurable
{
    private boolean ignore_pending_messages_on_timeout = false;

    public ClientGIOPConnection( org.omg.ETF.Profile profile,
                                 org.omg.ETF.Connection transport,
                                 RequestListener request_listener,
                                 ReplyListener reply_listener,
                                 StatisticsProvider statistics_provider )
    {
        super( profile, transport, request_listener, reply_listener, statistics_provider );
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        ignore_pending_messages_on_timeout =
            configuration.getAttribute("jacorb.connection.client.timeout_ignores_pending_messages","off").equals("on");
    }

    /**
     * Client-side implementation what to do when a read on the
     * underlying transport times out.  If we have no pending messages
     * for which we haven't received a reply yet, or if the property
     * jacorb.connection.client.timeout_ignores_pending_messages is on,
     * then we close the transport, but allow it to be reopened later.
     * If we have pending message and are not allowed to ignore that,
     * do nothing.
     */
    protected void readTimedOut()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug (this.toString() + ": readTimedOut()");
        }

        synchronized( pendingUndecidedSync )
        {
            if (ignore_pending_messages_on_timeout)
            {
                this.streamClosed();
            }
            else if (! hasPendingMessages())
            {
                closeAllowReopen();
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug
                    (
                        this.toString()
                        + ": cannot close because there are pending messages"
                    );
                }
            }
        }
    }

    /**
     * Client-side implementation what to do when the underlying transport
     * is closed during a read operation.  We mark the transport as closed
     * and allow it to be reopened later, when the client retries.
     */
    protected void streamClosed()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug (this.toString() + ": streamClosed()");
        }

        closeAllowReopen();

        if( connection_listener != null )
        {
            connection_listener.streamClosed();
        }
    }

    /**
     * Closes the underlying transport, but keeps this ClientGIOPConnection
     * alive.  If, subsequently, another request is sent to this connection,
     * it will try to reopen the transport.
     */
    public void closeAllowReopen()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug (this.toString() + ": closeAllowReopen()");
        }

        try
        {
            synchronized (connect_sync)
            {
                getWriteLock();
                transport.close();
                // We expect that the same transport can be reconnected
                // after a close, something that the ETF draft isn't
                // particularly clear about.
            }
        }
        finally
        {
            releaseWriteLock();
        }
    }

    public String toString()
    {
        return "ClientGIOPConnection to "
             + profile.toString()
             + " (" + Integer.toHexString(this.hashCode()) + ")";
    }

}// ClientGIOPConnection
