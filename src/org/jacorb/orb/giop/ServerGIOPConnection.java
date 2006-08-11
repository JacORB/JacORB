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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.orb.iiop.IIOPConnection;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServerGIOPConnection
    extends GIOPConnection
{
    private static final byte[] CLOSE_CONNECTION_MESSAGE =
        new byte[] {
            // Byte casts are for JDK1.2 compatibility.
            (byte )'G', (byte )'I', (byte )'O', (byte )'P', //magic start
            1, //GIOP major
            0, //GIOP minor
            0, //endianess, big-endian
            5, //message type, CloseConnection message
            0, 0, 0, 0 // message size, 0 because CloseConnection has no body
        };

    private final GIOPConnectionManager manager;
    private boolean closeOnReadTimeout = false;
    private boolean delayClose = false;

    public ServerGIOPConnection( org.omg.ETF.Profile profile,
                                 org.omg.ETF.Connection transport,
                                 RequestListener request_listener,
                                 ReplyListener reply_listener,
                                 StatisticsProvider statistics_provider,
                                 GIOPConnectionManager manager )
    {
        super( profile, transport, request_listener, reply_listener, statistics_provider );
        this.manager = manager;
    }



    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        delayClose =
            configuration.getAttribute("jacorb.connection.delay_close","off").equals("on");
    }


    /*
     * Try an orderly shutdown of this connection by sending a
     * CloseConnection message.  The CORBA spec only allows us to
     * do that if we have no more pending messages for which we
     * haven't sent a reply yet (CORBA 3.0, 15.5.1.1).  If there are
     * pending messages, this method does nothing and returns false.
     * If there are no pending messages, it sets the connection into
     * discarding mode, sends the CloseConnection message, and reports
     * the connection as closed to any connection_listener that's registered
     * with this connection.  The actual closing of the connection will happen
     * later, when the transport gets closed by the client, or the final
     * timeout has passed.
     */
    boolean tryClose()
    {
        if( tryDiscard() )
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(this.toString()
                             + ": tryClose() -- will send close connection");
            }

            sendCloseConnection();

            closeOnReadTimeout = true;

            if( connection_listener != null )
            {
                connection_listener.connectionClosed();
            }

            return true;
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(this.toString()
                    + ": tryClose() -- cannot close connection");
        }
        return false;
    }


    /**
     * Atomically try to set this connection into discarding mode, if
     * it doesn't have any pending messages.
     *
     * @return true, if the connection has been idle and discarding
     * has been set
     */
    private boolean tryDiscard()
    {
        if( ! hasPendingMessages() )
        {
            synchronized( pendingUndecidedSync )
            {
                discard_messages = true;
            }

            return true;
        }
        return false;
    }


    /**
     * <code>sendCloseConnection</code> sends a close connection message
     * flushing the transport.
     */
    private void sendCloseConnection()
    {
        try
        {
            getWriteLock();

            write( CLOSE_CONNECTION_MESSAGE,
                   0,
                   CLOSE_CONNECTION_MESSAGE.length );

            transport.flush();

            if (getStatisticsProviderAdapter() != null)
            {
                getStatisticsProviderAdapter().flushed();
            }

            if( delayClose && transport instanceof IIOPConnection )
            {
                ((IIOPConnection)transport).turnOnFinalTimeout();
            }
            else
            {
                // Set do_close to true so anything waiting in waitUntilConnection
                // doesn't think there is a possibility of connecting. I think.
                do_close = true;

                transport.close();
            }
        }
        catch( org.omg.CORBA.COMM_FAILURE e )
        {
            logger.error("COMM_FAILURE" , e );
        }
        finally
        {
            releaseWriteLock();
        }

        if( manager != null )
        {
            manager.unregisterServerGIOPConnection( this );
        }
    }

    /**
     * Server-side implementation what to do when a read timeout occurs.
     * We react by trying an orderly shutdown that's initiated with
     * a CloseConnection message.  If this timeout occured after we have
     * already sent CloseConnection, just close down unconditionally.
     */
    protected void readTimedOut()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug (this.toString() + ": readTimedOut()");
        }

        if( closeOnReadTimeout )
        {
            // we get here if we have sent a CloseConnection message
            // on this connection before
            close();
        }
        else
        {
            // attempt an orderly shutdown by sending a CloseConnection
            // message
            tryClose();
        }
    }

    /**
     * Server-side implementation what to do if the underlying transport
     * gets closed during a read operation. Since we're server-side and
     * can't reopen, we simply close completely.
     */
    protected void streamClosed()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug (this.toString() + ": streamClosed()");
        }
        close();
    }

    /**
     * @see GIOPConnection#close()
     */
    public void close()
    {
        super.close();
        if( manager != null )
        {
            manager.unregisterServerGIOPConnection( this );
        }
    }

    public String toString()
    {
        if (profile != null)
        {
          return "ServerGIOPConnection to "
                + profile.toString()
                + " (" + Integer.toHexString(this.hashCode()) + ")";
        }
        return super.toString();
    }

}// ServerGIOPConnection
