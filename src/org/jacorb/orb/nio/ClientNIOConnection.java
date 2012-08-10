/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2011-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;

public class ClientNIOConnection
        extends NIOConnection
        implements Configurable
{
    private int noOfRetries  = 5;
    private int retryInterval = 0;

    public void configure(Configuration configuration)
    throws ConfigurationException
    {
        super.configure(configuration);

        noOfRetries = configuration.getAttributeAsInteger("jacorb.retries", 5);
        retryInterval = configuration.getAttributeAsInteger("jacorb.retry_interval", 500);
    }

    // time_out is in milliseconds
    public synchronized void connect(org.omg.ETF.Profile server_profile, long timeout)
    {

        long nanoDeadline = (timeout == 0 ? Long.MAX_VALUE : System.nanoTime() + timeout * 1000000);

        if ( !is_connected() )
        {
            if (server_profile instanceof IIOPProfile)
            {
                this.profile = (IIOPProfile) server_profile;
            }
            else
            {
                throw new org.omg.CORBA.BAD_PARAM
                ( "attempt to connect an IIOP connection "
                  + "to a non-IIOP profile: " + server_profile.getClass());
            }

            if (isDebugEnabled)
            {
                logger.debug("Trying to establish an NIO client connection with timeout " + timeout);
            }

            int retryCount = 0;
            for (retryCount = 0; retryCount < noOfRetries; retryCount++)
            {

                try
                {
                    connectChannel (nanoDeadline);

                    SocketChannel myChannel;
                    synchronized (this)
                    {
                        myChannel = channel;
                    }

                    if (logger.isInfoEnabled())
                    {
                        logger.info("Connected to " + connection_info +
                                    " from local port " +
                                    myChannel.socket().getLocalPort() +
                                    ( (timeout == 0) ? "" : " Timeout: " + timeout));
                    }

                    synchronized (this)
                    {
                        failedWriteAttempts = 0;
                    }
                    setConnected (true);
                    return;
                }
                catch (TIMEOUT ex)
                {
                    profile = null;
                    throw ex;
                }
                catch (IOException ex)
                {
                    if (isDebugEnabled)
                    {
                        logger.debug("Exception connecting {}", ex.getMessage (), ex);
                    }

                    //only sleep and print message if we're actually
                    //going to retry
                    if (retryCount < noOfRetries - 1)
                    {
                        if (logger.isInfoEnabled())
                        {
                            logger.info("Retrying attempt " + retryCount +
                                        " to connect to " +
                                        connection_info );
                        }

                        try
                        {
                            Thread.sleep( retryInterval );
                        }
                        catch ( InterruptedException i )
                        {
                        }
                    }
                }
            }

            if (retryCount == noOfRetries)
            {
                profile = null;
                throw new org.omg.CORBA.TRANSIENT
                ( "Retries exceeded, couldn't reconnect to " +
                  connection_info );
            }
        }
    }

    public synchronized void close()
    {

        if (!is_connected())
        {
            return;
        }

        SocketChannel myChannel;
        synchronized (this)
        {
            myChannel = channel;
        }

        try
        {
            if (myChannel != null)
            {
                myChannel.close();
            }

            setConnected (false);

            // this was copied from ClientIIOPConnection. Don't see why
            //  this would be required, but its included anyways
            if (in_stream != null)
            {
                in_stream.close();
            }
            if (out_stream != null)
            {
                out_stream.close();
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Client-side TCP transport to " +
                            connection_info + " closed.");
            }
        }
        catch (IOException ex)
        {
            if (isDebugEnabled)
            {
                logger.debug ("Exception when closing the channel", ex);
            }

            throw handleCommFailure(ex);
        }
    }

    private synchronized void connectChannel (long nanoDeadline)
    throws IOException
    {

        List addressList = new ArrayList();
        addressList.add(((IIOPProfile)profile).getAddress());
        addressList.addAll(((IIOPProfile)profile).getAlternateAddresses());

        Iterator addressIterator = addressList.iterator();

        Exception exception = null;

        while (addressIterator.hasNext())
        {

            SocketChannel myChannel = null;
            try
            {
                myChannel = SocketChannel.open();
                myChannel.configureBlocking(false);

                IIOPAddress address = (IIOPAddress)addressIterator.next();

                final String ipAddress = address.getIP();
                final int port = address.getPort();
                if (ipAddress.indexOf(':') == -1)
                {
                    connection_info = ipAddress + ":" + port;
                }
                else
                {
                    connection_info = "[" + ipAddress + "]:" + port;
                }

                if (isDebugEnabled)
                {
                    logger.debug("Trying to connect to " + connection_info);
                }

                myChannel.connect (new InetSocketAddress (ipAddress, port));

                SelectorRequest request =
                    new SelectorRequest (SelectorRequest.Type.CONNECT,
                                         myChannel,
                                         new ConnectCallback (),
                                         nanoDeadline);
                selectorManager.add (request);
                request.waitOnCompletion (nanoDeadline);

                if (request.status == SelectorRequest.Status.IOERROR)
                {
                    throw new TRANSIENT ("unable to connect");
                }
                else if (request.status == SelectorRequest.Status.EXPIRED ||
                    !request.isFinalized())
                {
                    throw new TIMEOUT("connection timeout expired");
                }
                else if (request.status == SelectorRequest.Status.FAILED ||
                         request.status == SelectorRequest.Status.SHUTDOWN ||
                         request.status == SelectorRequest.Status.CLOSED)
                {
                    // Somethings wrong with SelectorManager. Unwise to proceed
                    throw new IOException ("SelectorManager is corrupted");
                }
                else if (myChannel.isConnected())
                {
                    // we are connected
                    synchronized (this)
                    {
                        channel = myChannel;
                    }

		    // if there are multiple endpoints, an earlier
		    // connect attempt may have failed and set the
		    // exception but now we've succeeded we don't want
		    // to throw a spurious exception.
		    exception = null;

                    break;
                }
            }
            catch (Exception ex)
            {
                exception = ex;
            }

            // TBD: selectorManager.remove (request);
            if (myChannel != null)
            {
                myChannel.close ();
            }
        }

        if (exception != null)
        {
            if (exception instanceof TIMEOUT)
            {
                throw (TIMEOUT) exception;
            }
	    else if ( exception instanceof TRANSIENT )
	    {
		throw (TRANSIENT) exception;
	    }
            else if ( exception instanceof IOException )
            {
                throw (IOException) exception;
            }
            else
            {
                //not expected, because all used methods just throw IOExceptions or TIMEOUT
                //but... never say never ;o)
                throw new IOException ( "Unexpected exception occured: " + exception.toString() );
            }
        }
    }

    private class ConnectCallback extends SelectorRequestCallback
    {
        public boolean call (SelectorRequest request)
        {

            SocketChannel myChannel = request.channel;

            if (isDebugEnabled)
            {
                logger.debug("Connect callback. Request status: " + request.status.toString());
            }

            try
            {
                if (request.status == SelectorRequest.Status.READY)
                {
                    myChannel.finishConnect ();

                    if (isDebugEnabled)
                    {
                        logger.debug("Connection establishment finished");
                    }
                }
            }
            catch (Exception ex)
            {
                request.setStatus(SelectorRequest.Status.IOERROR);
                logger.error ("Exception while finishing connection {} ", ex.getMessage (), ex);
            }

            return false;
        }
    }

}