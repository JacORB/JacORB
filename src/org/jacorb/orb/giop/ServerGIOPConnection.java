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
import org.apache.avalon.framework.logger.Logger;

import java.io.*;

import org.jacorb.orb.iiop.*;

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

    private GIOPConnectionManager manager = null;
    private Logger logger = null;
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
        logger = ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.giop.conn");
        delayClose =
            configuration.getAttribute("jacorb.connection.delay_close","off").equals("on");
    }


    /*
     * <code>tryClose</close> called by GIOPConnectionManager::createServerGIOPConnection
     * if there are too many GIOP connections or if we have timed out with nothing pending.
     */
    boolean tryClose()
    {
        if( tryDiscard() )
        {
            sendCloseConnection();

            closeOnReadTimeout = true;

            if( connection_listener != null )
            {
                connection_listener.connectionClosed();
            }

            return true;
        }
        else
        {
            return false;
        }
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
        else
        {
            return false;
        }
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

            if (statistics_provider != null)
            {
                statistics_provider.flushed();
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
            if (logger.isErrorEnabled())
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


    protected void readTimedOut()
    {
        if( closeOnReadTimeout )
        {
            close();
        }
        else
        {
            /*
             * If we don't have any more pending messages, we'll send a
             * GIOP CloseConnection message (done by tryClose() ).
             */
            tryClose();
        }
    }

    /**
     * We're server side and can't reopen, therefore close completely
     * if stream closed.
     */
    protected void streamClosed()
    {
        close();
    }
    
    /**
     * @overrides close in GIOPConnection
     */   
    public void close()
    {
        super.close();
        if( manager != null )
        {
            manager.unregisterServerGIOPConnection( this );
        }
    }

}// ServerGIOPConnection
