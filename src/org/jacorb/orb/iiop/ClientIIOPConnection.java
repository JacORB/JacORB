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

package org.jacorb.orb.iiop;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.IIOPAddress;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.orb.giop.TransportManager;

import org.omg.CSIIOP.*;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;


/**
 * ClientIIOPConnection.java
 *
 *
 * Created: Sun Aug 12 20:56:32 2002
 *
 * @author Nicolas Noffke / Andre Spiegel
 * @version $Id$
 */

public class ClientIIOPConnection
    extends IIOPConnection
    implements Configurable
{
    private IIOPProfile target_profile;
    private int timeout = 0;

    private boolean use_ssl  = false;
    private int     ssl_port = -1;
    private int noOfRetries  = 5;
    private int retryInterval = 0;
    private boolean doSupportSSL = false;
    private TransportManager transportManager;

    //for testing purposes only: # of open transports
    //used by org.jacorb.test.orb.connection[Client|Server]ConnectionTimeoutTest
    public static int openTransports = 0;

    //for storing exceptions during connect
    private Exception exception = null;

    public ClientIIOPConnection()
    {}

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);
        //get the client-side timeout property value

        timeout = 
            configuration.getAttributeAsInteger("jacorb.connection.client.idle_timeout",0 );
        noOfRetries = 
            configuration.getAttributeAsInteger("jacorb.retries", 5);
        retryInterval = 
            configuration.getAttributeAsInteger("jacorb.retry_interval",500);
        doSupportSSL =
            configuration.getAttribute("jacorb.security.support_ssl","off").equals("on");
        transportManager = 
            this.configuration.getORB().getTransportManager();

    }

    public ClientIIOPConnection (ClientIIOPConnection other)
    {
        super (other);
        this.target_profile = other.target_profile;
        this.timeout = other.timeout;
        this.use_ssl = other.use_ssl;
        this.ssl_port = other.ssl_port;
    }

    /**
     * Attempts to establish a 1-to-1 connection with a server using the
     * Listener endpoint from the given Profile description.  It shall
     * throw a COMM_FAILURE exception if it fails (e.g. if the endpoint
     * is unreachable) or a TIMEOUT exception if the given time_out period
     * has expired before a connection is established. If the connection
     * is successfully established it shall store the used Profile data.
     *
     */
    public synchronized void connect(org.omg.ETF.Profile server_profile, long time_out)
    {
        if( ! connected )
        {
            if (server_profile instanceof IIOPProfile)
            {
                this.target_profile = (IIOPProfile)server_profile;
            }
            else
            {
                throw new org.omg.CORBA.BAD_PARAM
                    ( "attempt to connect an IIOP connection "
                    + "to a non-IIOP profile: " + server_profile.getClass());
            }

            checkSSL();

            int retries = noOfRetries;
            while( retries >= 0 )
            {
                try
                {
                    createSocket(time_out);

                    if( timeout != 0 )
                    {
                        /* re-set the socket timeout */
                        socket.setSoTimeout( timeout /*note: this is another timeout!*/ );
                    }

                    in_stream =
                        socket.getInputStream();

                    out_stream =
                        new BufferedOutputStream( socket.getOutputStream());

                    if (logger.isInfoEnabled())
                    {
                        logger.info("Connected to " + connection_info +
                                    " from local port " +
                                    socket.getLocalPort() +
                                    ( this.isSSL() ? " via SSL" : "" ));
                    }

                    connected = true;

                    //for testing purposes
                    ++openTransports;

                    return;
                }
                catch ( IOException c )
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Exception", c );

                    //only sleep and print message if we're actually
                    //going to retry
                    retries--;
                    if( retries >= 0 )
                    {
                        if (logger.isInfoEnabled())
                            logger.info("Retrying to connect to " +
                                        connection_info );
                        try
                        {
                            Thread.sleep( retryInterval );
                        }
                        catch( InterruptedException i )
                        {
                        }
                    }
                }
                catch (TIMEOUT e)
                {
                   //thrown if timeout is expired
                   target_profile = null;
                   use_ssl = false;
                   ssl_port = -1;
                   throw e;
                }

            }

            if( retries < 0 )
            {
                target_profile = null;
                use_ssl = false;
                ssl_port = -1;
                throw new org.omg.CORBA.TRANSIENT
                    ( "Retries exceeded, couldn't reconnect to " +
                      connection_info );
            }
        }
    }

    /**
     * Tries to create a socket connection to any of the addresses in
     * the target profile, starting with the primary IIOP address,
     * and then any alternate IIOP addresses that have been specified.
     */
    private void createSocket(long time_out) 
        throws IOException
    {
        List addressList = new ArrayList();
        addressList.add    (target_profile.getAddress());
        addressList.addAll (target_profile.getAlternateAddresses());

        Iterator addressIterator = addressList.iterator();

        exception = null;
        socket = null;
        while (socket == null && addressIterator.hasNext())
        {
            try
            {
                IIOPAddress address = (IIOPAddress)addressIterator.next();

                final SocketFactory factory = 
                    (use_ssl) ? transportManager.getSSLSocketFactory() :
                    transportManager.getSocketFactory();

                final String ipAddress = address.getIP();
                final int port = (use_ssl) ? ssl_port : address.getPort();
                connection_info = ipAddress + ":" + port;

                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying to connect to " + connection_info + " with timeout=" + time_out);
                }
                exception = null;
                socket = null;

                if( time_out > 0 )
                {
                   //set up connect with an extra thread
                   //if thread returns within time_out it notifies current thread
                   //if not this thread will cancel the connect-thread
                   //this is necessary since earlier JDKs didnt support connect()
                   //with time_out
                   final ClientIIOPConnection self = this;
                   Thread thread = new Thread( new  Runnable()
                                                    {
                                                       public void run()
                                                       {
                                                          try
                                                          {
                                                             socket = factory.createSocket(ipAddress, port);
                                                          }
                                                          catch (Exception e)
                                                          {
                                                             exception = e;
                                                          }
                                                          finally
                                                          {
                                                             synchronized (self)
                                                             {
                                                                self.notify();
                                                             }
                                                          }
                                                       }
                                                    } );
                   thread.setDaemon(true);
                   try
                   {
                       synchronized (self)
                       {
                           thread.start();
                           self.wait(time_out);
                       }
                   }
                   catch (InterruptedException _ex) 
                   { }
                   
                   if (socket == null)
                   {
                       if (exception == null)
                       {
                           if (logger.isDebugEnabled())
                           {
                               logger.debug("connect to " + connection_info + 
                                            " with timeout=" + time_out + " timed out");
                           }
                           thread.interrupt();
                           exception = 
                               new TIMEOUT("connection timeout of " + time_out + " milliseconds expired");
                       }
                       else
                       {      
                           if (logger.isDebugEnabled())
                           {
                               logger.debug("connect to " + connection_info + " with timeout="
                                            + time_out + " raised exception: " + exception.toString());
                           }                                               
                       }
                   }
                }
                else
                {
                    //no timeout --> may hang forever!
                    socket = factory.createSocket(ipAddress, port);
                }
            }
            catch (Exception e)
            {
                exception = e;
            }
        }

        if (exception != null)
        {
            if( exception instanceof IOException )
            {
                throw (IOException)exception;
            }
            else if( exception instanceof org.omg.CORBA.TIMEOUT )
            {
                throw (org.omg.CORBA.TIMEOUT)exception;
            }
            else
            {
                //not expected, because all used methods just throw IOExceptions or TIMEOUT
                //but... never say never ;o)
                throw new IOException ( "Unexpected exception occured: " + exception.toString() );
            }
        }
    }


    public synchronized void close()
    {
        try
        {
            if (connected && socket != null)
            {
                socket.close ();

                //this will cause exceptions when trying to read from
                //the streams. Better than "nulling" them.
                if( in_stream != null )
                {
                    in_stream.close();
                }
                if( out_stream != null )
                {
                    out_stream.close();
                }

                //for testing purposes
                --openTransports;
            }

            connected = false;
        }
        catch (IOException ex)
        {
            throw to_COMM_FAILURE (ex);
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Client-side TCP transport to " +
                        connection_info + " closed.");
        }
    }

    public boolean isSSL()
    {
        return use_ssl;
    }

    public org.omg.ETF.Profile get_server_profile()
    {
        return target_profile;
    }

    /**
     * Check if this client should use SSL when connecting to
     * the server described by the target_profile.  The result
     * is stored in the private fields use_ssl and ssl_port.
     */
    private void checkSSL()
    {
        CompoundSecMechList sas
            = (CompoundSecMechList)target_profile.getComponent
                                           (TAG_CSI_SEC_MECH_LIST.value,
                                            CompoundSecMechListHelper.class);

        TLS_SEC_TRANS tls = null;
        if (sas != null && sas.mechanism_list[0].transport_mech.tag == TAG_TLS_SEC_TRANS.value) {
            try
            {
                byte[] tagData = sas.mechanism_list[0].transport_mech.component_data;
                CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null, tagData );
                in.openEncapsulatedArray();
                tls = TLS_SEC_TRANSHelper.read( in );
            }
            catch ( Exception ex )
            {
                logger.warn("Error parsing TLS_SEC_TRANS: "+ex);
            }
        }

        SSL ssl = (SSL)target_profile.getComponent
                                           (TAG_SSL_SEC_TRANS.value,
                                            SSLHelper.class);
        //if( sas != null &&
        //    ssl != null )
        //{
        //    ssl.target_requires |= sas.mechanism_list[0].target_requires;
        //}

        // SSL usage is decided the following way: At least one side
        // must require it. Therefore, we first check if it is
        // supported by both sides, and then if it is required by at
        // least one side. The distinction between
        // EstablishTrustInTarget and EstablishTrustInClient is
        // handled at the socket factory layer.

        //the following is used as a bit mask to check, if any of
        //these options are set
        int minimum_options =
            Integrity.value |
            Confidentiality.value |
            DetectReplay.value |
            DetectMisordering.value |
            EstablishTrustInTarget.value |
            EstablishTrustInClient.value;

        int client_required = 0;
        int client_supported = 0;

        //only read in the properties if ssl is really supported.
        if( doSupportSSL )
        {
            client_required =
                configuration.getAttributeAsInteger("jacorb.security.ssl.client.required_options", 16);
            client_supported =
                configuration.getAttributeAsInteger("jacorb.security.ssl.client.supported_options",16);
        }

        if( tls != null && // server knows about ssl...
            ((tls.target_supports & minimum_options) != 0) && //...and "really" supports it
            doSupportSSL && //client knows about ssl...
            ((client_supported & minimum_options) != 0 )&& //...and "really" supports it
            ( ((tls.target_requires & minimum_options) != 0) || //server ...
              ((client_required & minimum_options) != 0))) //...or client require it
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting TLS for connection");
            }

            use_ssl  = true;
            ssl_port = tls.addresses[0].port;
            if (ssl_port < 0) ssl_port += 65536;
        }
        else if( ssl != null && // server knows about ssl...
            ((ssl.target_supports & minimum_options) != 0) && //...and "really" supports it
            doSupportSSL && //client knows about ssl...
            ((client_supported & minimum_options) != 0 )&& //...and "really" supports it
            ( ((ssl.target_requires & minimum_options) != 0) || //server ...
              ((client_required & minimum_options) != 0))) //...or client require it
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting SSL for connection");
            }

            use_ssl  = true;
            ssl_port = ssl.port;
            if (ssl_port < 0)
                ssl_port += 65536;
        }
        //prevent client policy violation, i.e. opening plain TCP
        //connections when SSL is required
        else if( // server doesn't know ssl...
                 doSupportSSL && //client knows about ssl...
                 ((client_required & minimum_options) != 0)) //...and requires it
        {
            throw new org.omg.CORBA.NO_PERMISSION( "Client-side policy requires SSL/TLS, but server doesn't support it" );
        }
        else
        {
            use_ssl = false;
            ssl_port = -1;
        }
    }

}// Client_TCP_IP_Transport
