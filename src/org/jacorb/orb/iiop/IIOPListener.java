package org.jacorb.orb.iiop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

import org.omg.ETF.*;
import org.omg.CSIIOP.*;
import org.omg.SSLIOP.*;

import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.factory.*;
import org.jacorb.orb.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPListener 
    extends org.jacorb.orb.etf.ListenerBase
{
    /** the maximum set of security options supported by the SSL mechanism */
    private static final int MAX_SSL_OPTIONS = Integrity.value |         
                                               Confidentiality.value |
                                               DetectReplay.value |
                                               DetectMisordering.value |
                                               EstablishTrustInTarget.value |
                                               EstablishTrustInClient.value;

    /**  the minimum set of security options supported by the SSL mechanism
     *   which cannot be turned off, so they are always supported
     */
    private static final int MIN_SSL_OPTIONS = Integrity.value |
                                               DetectReplay.value |
                                               DetectMisordering.value;

    
    private SocketFactoryManager socketFactoryManager = null;
    private ServerSocketFactory    serverSocketFactory    = null;
    private SSLServerSocketFactory sslServerSocketFactory = null;

    private SSLAcceptor sslAcceptor = null;

    private boolean supportSSL = false;
    private boolean dnsEnabled = false;
    private int serverTimeout = 0;
    private int oaPort = 0;
    private int sslPort = 0;
    private int target_supports = 0;
    private int target_requires = 0;
    private boolean generateSSLComponents = true;

    public IIOPListener()
    {
    }
    
    /**
    * @deprecated Use no-args version and then configure(). 
    */
    public IIOPListener(ORB orb)
    {
        super(orb);
        socketFactoryManager = new SocketFactoryManager(orb);
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        
        if (orb == null)
        {
            // c.f. with the constructor taking an ORB param.
            this.orb = this.configuration.getORB();
            socketFactoryManager = new SocketFactoryManager(this.orb);
        }
        
        logger = this.configuration.getNamedLogger("jacorb.iiop.listener");
 
        socketFactoryManager.configure(configuration);

        oaPort = configuration.getAttributeAsInteger("OAPort",0);
        sslPort = configuration.getAttributeAsInteger("OASSLPort",0);
 
        dnsEnabled = 
            configuration.getAttribute("jacorb.dns.enable","off").equals("on");

        serverTimeout = 
            configuration.getAttributeAsInteger("jacorb.connection.server.timeout",0);

        supportSSL =
            configuration.getAttribute("jacorb.security.support_ssl","off").equals("on");

        target_supports = 
            Integer.parseInt(
                configuration.getAttribute("jacorb.security.ssl.server.supported_options","20"),
                16); // 16 is the base as we take the string value as hex!
        
        // make sure that the minimum options are always in the set of supported options
        target_supports |= MIN_SSL_OPTIONS;

        target_requires = 
            Integer.parseInt(
                configuration.getAttribute("jacorb.security.ssl.server.required_options","0"),
                16);


        generateSSLComponents =
            configuration.getAttribute("jacorb.security.ssl_components_added_by_ior_interceptor","off").equals("off");


        if (!isSSLRequired() || 
            configuration.getAttributeAsBoolean("jacorb.security.ssl.always_open_unsecured_endpoint"))
        {
            acceptor = new Acceptor();
            ((Acceptor)acceptor).init();
        }

        if (supportSSL)
        {
            sslAcceptor = new SSLAcceptor();
            sslAcceptor.init();
        }

        endpoint = createEndPointProfile();

    }

    
    /**
     * It is possible that connection requests arrive <i>after</i> the
     * initial creation of the Listener instance but <i>before</i> the
     * conclusion of the configuration of the specific endpoint in this
     * plugin. In order to provide a clear end of this configuration state,
     * we added the listen() method. It is called by the ORB when it ready
     * for incoming connection and thus signals the Listener instance to
     * start processing the incoming connection requests. Therefore,
     * a Listener instance shall not deliver incoming connections to the
     * ORB before this method was called.
     */
    public void listen()
    {
        super.listen();
        
        if (sslAcceptor != null)
            sslAcceptor.start();
    }

    /**
     * The Listener is instructed to close its endpoint. It shall no
     * longer accept any connection requests and shall close all
     * connections opened by it.
     */
    public void destroy()
    {        
        if (sslAcceptor != null)
            sslAcceptor.terminate();

        super.destroy();
    }

    // internal methods below this line

    /**
     * Returns true if this Listener should support SSL connections.
     */
    private boolean isSSLSupported()
    {
        return  supportSSL;
    }

    /**
     * Returns true if this Listener should <i>require</i> SSL, and not
     * offer plain connections.
     */
    private boolean isSSLRequired()
        throws ConfigurationException
    {
        if (isSSLSupported())
        {
            // the following is used as a bit mask to check if any SSL
            // options are required
            return ((target_requires & MAX_SSL_OPTIONS ) != 0);
        }
        return false;
    }

    /**
     * Returns the server timeout that has been specified, or zero if none
     * has been set.
     */
    private int getServerTimeout()
    {
        return serverTimeout;
    }

    private ServerSocketFactory getServerSocketFactory()
    {
        if (serverSocketFactory == null)
        {            
            serverSocketFactory =
                socketFactoryManager.getServerSocketFactory();
        }
        return serverSocketFactory;
    }

    /**
     * Returns the SSLServerSocketFactory that has been configured.
     * If no such factory is available, org.omg.CORBA.INITIALIZE() is thrown.
     */
    private SSLServerSocketFactory getSSLServerSocketFactory()
    {
        if (sslServerSocketFactory == null)
        {
            sslServerSocketFactory = 
                orb.getBasicAdapter().getSSLSocketFactory();

            if (sslServerSocketFactory == null)
                throw new org.omg.CORBA.INITIALIZE("No SSL server socket factory found");
        }
        return sslServerSocketFactory;
    }

    /**
     * Creates a new IIOPProfile that describes this transport endpoint.
     */
    private IIOPProfile createEndPointProfile()
        throws ConfigurationException
    {
        int port=0;
        if (acceptor != null)
        {
            port = getConfiguredPort();
            if (port == 0)
                port = ((Acceptor)acceptor).getLocalAddress().getPort();
        } 
        else if (sslAcceptor == null)
            throw new org.omg.CORBA.INITIALIZE
                ("no acceptors found, cannot create endpoint profile");

        IIOPProfile result = new IIOPProfile
        (
            new IIOPAddress 
            (
                // Construct the address either from the symbolic or
                // numeric host address, depending on what we need later.
                // Otherwise, we might get an unnecessary DNS lookup
                // at IOR creation time, which might break the setting
                // of OAIAddr on a multi-homed host.
                dnsEnabled ? getHost().getHostName() : getHost().getHostAddress(),
                port // will be 0 if there is only an SSLAcceptor
            ), 
            null
        );
        if (sslAcceptor != null && generateSSLComponents)
        {
             result.addComponent (TAG_SSL_SEC_TRANS.value,
                                  createSSL(), SSLHelper.class);
        }
        
        result.configure(configuration);
        return result;
    }

    private SSL createSSL()
    {
        return new SSL
        (
            (short)target_supports, 
            (short)target_requires,
            (short)sslAcceptor.getLocalAddress().getPort()
        );
    }

    /**
     * Returns the IP address that this listener should listen on, if one has 
     * been configured explicitly. In the absence of an explicit configuration 
     * (through the <code>"OAIAddr"</code> property), returns null. Eventually 
     * the returned address will be passed as the `bindAddr' argument to a 
     * <code>java.net.ServerSocket</code> constructor, so a null address (no 
     * explicit configuration) means that this listener will accept connections
     * on any/all local addresses. On the other hand, a non-null address means
     * that this listener will only accept connect requests to the configured 
     * IP address.
     *
     * @return an <code>InetAddress</code>object with the IP address 
     *         specified by the <code>"OAIAddr"</code> property, or 
     *         <code>null</code> if this property has not been defined.
     */
    private InetAddress getConfiguredHost()
    {
        try
        {
            String oa_addr = configuration.getAttribute("OAIAddr","");
            return (oa_addr.length() == 0) ? null : InetAddress.getByName(oa_addr);
        }
//         catch (ConfigurationException e)
//         {
//             throw new org.omg.CORBA.INITIALIZE("Could not resolve configured listener host" + 
//                                                e.getMessage());
//         }
        catch (java.net.UnknownHostException e)
        {
            throw new org.omg.CORBA.INITIALIZE
                             ("Could not resolve configured listener host");
        }
    }

    /**
     * Returns the IP address to be placed in this listener's endpoint
     * profile. The returned value is either an explicitly configured IP 
     * address or (in the absence of an explicit configuration) is the IP
     * address of the local host.
     *
     * @return an <code>InetAddress</code>object with the IP address 
     *         specified by the <code>"OAIAddr"</code> property, or 
     *         (if this property has not been defined) an 
     *         <code>InetAddress</code>object for the local host.
     */
    private InetAddress getHost()
    {
        try
        {
            InetAddress configuredHost = getConfiguredHost();
            return (configuredHost == null) ? InetAddress.getLocalHost()
                                            : configuredHost;
        }
        catch (java.net.UnknownHostException e)
        {
            throw new org.omg.CORBA.INITIALIZE
                             ("Could not resolve configured listener host");
        }
    }

    /**
     * Returns the number of the port that this Listener should listen on.
     * If no port has been specified, returns zero.
     */
    private int getConfiguredPort()
    {
        return oaPort;

    }

    private int getConfiguredSSLPort()
    {
        return sslPort;
    }

    /**
     * Creates an ETF connection around a live socket and passes it
     * up to the ORB, using either the upcall mechanism or the
     * polling mechanism.
     */
    private void deliverConnection (Socket socket, boolean isSSL)
    {
        Connection result = null;
        try
        {
            result = createServerConnection(socket, isSSL);
        }
        catch (IOException ex)
        {
            if (logger.isErrorEnabled())
            { 
                logger.error("Could not create connection from socket: " + ex);
            }
            return;
        }

        deliverConnection(result);
    }

    /**
     * Template method to create a server-side ETF Connection.
     * This can be overridden by subclasses to pass a different
     * kind of Connection up to the ORB.
     */
    protected Connection createServerConnection (Socket socket,
                                                 boolean is_ssl)
        throws IOException
    {
        ServerIIOPConnection result = new ServerIIOPConnection(socket, is_ssl);
        try
        {
            result.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.getMessage());
        }
        return result;
    }

    // Acceptor classes below this line

    protected class Acceptor 
        extends org.jacorb.orb.etf.ListenerBase.Acceptor
    {
        protected ServerSocket serverSocket;
        
        protected boolean terminated = false;
        
        public Acceptor()
        {
            // initialization deferred to init() method due to JDK bug
            setDaemon(true);
        }

        public void init()
        {
            serverSocket = createServerSocket();

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Created socket listener on " + serverSocket.getInetAddress() );
            }
       }

        public void run()
        {
            while (!terminated)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    setup (socket);
                    System.out.println("Socket port: " + socket.getLocalPort());
                    deliverConnection (socket);
                }
                catch (Exception e)
                {
                    if (!terminated)
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn(e.getMessage());
                        }
                    }
                }
            }
            if (logger.isInfoEnabled())
            {
                logger.info( "Listener exited");
            }
        }

        /**
         * Terminates this Acceptor by closing the ServerSocket.
         */
        public void terminate()
        {
            terminated = true;
            try
            {
                serverSocket.close();
            }
            catch (java.io.IOException e)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn(e.getMessage());
                }
            }
        }

        public IIOPAddress getLocalAddress()
        {
            return new IIOPAddress
            (
                serverSocket.getInetAddress().getHostAddress(),
                serverSocket.getLocalPort()
            );
        }

        /**
         * Template method that creates the server socket.
         */
        protected ServerSocket createServerSocket()
        {
            try
            {
                return getServerSocketFactory()
                           .createServerSocket (getConfiguredPort(),
                                                20,
                                                getConfiguredHost());
            }
            catch (IOException ex)
            {                
                logger.warn(ex.getMessage());
                throw new org.omg.CORBA.INITIALIZE ("Could not create server socket");
            }
        }

        /**
         * Template method that sets up the socket right after the
         * connection has been established.  Subclass implementations
         * must call super.setup() first.
         */
        protected void setup(Socket socket) 
            throws IOException
        {
             socket.setSoTimeout(serverTimeout);
        }

        protected void deliverConnection(Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, false);
        }
    }

    private class SSLAcceptor 
        extends Acceptor
    {
        protected ServerSocket createServerSocket()
        {
            try
            {
                return getSSLServerSocketFactory()
                            .createServerSocket (getConfiguredSSLPort(),
                                                 20,
                                                 getConfiguredHost());
            }
            catch (IOException e)
            {
                logger.warn(e.getMessage());
                throw new org.omg.CORBA.INITIALIZE
                                           ("Could not create SSL server socket");
            }
        }

        protected void deliverConnection(Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, true);
        }
    }

}
