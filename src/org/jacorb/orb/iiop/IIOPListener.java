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
import java.net.UnknownHostException;

import org.omg.ETF.*;
import org.omg.CSIIOP.*;
import org.omg.SSLIOP.*;

import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.BasicAdapter;
import org.jacorb.orb.factory.*;
import org.jacorb.orb.listener.AcceptorExceptionEvent;
import org.jacorb.orb.listener.AcceptorExceptionListener;
import org.jacorb.orb.listener.DefaultAcceptorExceptionListener;
import org.jacorb.orb.listener.NullAcceptorExceptionListener;
import org.jacorb.orb.listener.SSLListenerUtil;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.etf.ProtocolAddressBase;

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

    private SSLAcceptor sslAcceptor = null;
    private LoopbackAcceptor loopbackAcceptor ;

    private boolean supportSSL = false;
    private int serverTimeout = 0;
    private IIOPAddress address = null;
    private IIOPAddress sslAddress = null;
    private int target_supports = 0;
    private int target_requires = 0;
    private boolean generateSSLComponents = true;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);

        socketFactoryManager = orb.getTransportManager().getSocketFactoryManager();

        String address_str = configuration.getAttribute("OAAddress",null);
        if (address_str != null)
        {
            ProtocolAddressBase addr = orb.createAddress(address_str);
            if (addr instanceof IIOPAddress)
            {
                address = (IIOPAddress)addr;
            }
        }
        else
        {
            int oaPort = configuration.getAttributeAsInteger("OAPort",0);
            String oaHost = configuration.getAttribute("OAIAddr","");
            address = new IIOPAddress(oaHost,oaPort);
        }

        if (address != null)
        {
            address.configure (configuration);
        }

        address_str = configuration.getAttribute("OASSLAddress",null);
        if (address_str != null)
        {
            ProtocolAddressBase addr = orb.createAddress(address_str);
            if (addr instanceof IIOPAddress)
            {
                sslAddress = (IIOPAddress)addr;
            }
        }
        else
        {
            int sslPort = configuration.getAttributeAsInteger("OASSLPort",0);
            String sslHost = configuration.getAttribute("OAIAddr","");
            sslAddress = new IIOPAddress(sslHost,sslPort);
        }

        if (sslAddress != null)
        {
            sslAddress.configure (configuration);
        }

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
            configuration.getAttributeAsBoolean("jacorb.security.ssl.always_open_unsecured_address", false))
        {
            acceptor = new Acceptor("ServerSocketListener");
            ((Acceptor)acceptor).init();
        }

        if (supportSSL)
        {
            sslAcceptor = new SSLAcceptor();
            sslAcceptor.init();
        }

        loopbackAcceptor = new LoopbackAcceptor() ;

        profile = createAddressProfile();
    }


    /**
     * {@inheritDoc}
     */
    public void listen()
    {
        super.listen();

        if (sslAcceptor != null)
        {
            sslAcceptor.start();
        }

        loopbackAcceptor.start() ;
    }

    /**
     * {@inheritDoc}
     */
    public void destroy()
    {
        loopbackAcceptor.terminate() ;

        if (sslAcceptor != null)
        {
            sslAcceptor.terminate();
        }

        super.destroy();
    }

    public void renewSSLServerSocket()
    {
        if (sslAcceptor != null)
        {
            sslAcceptor.renewSSLServerSocket();
        }
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
     * Creates a new IIOPProfile that describes this transport address.
     */
    private IIOPProfile createAddressProfile()
        throws ConfigurationException
    {
        if (acceptor != null)
        {
            if (address.getPort() == 0)
            {
                address.setPort(((Acceptor)acceptor).getLocalAddress().getPort());
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug ("Using port " + address.getPort());
                }
            }
        }
        else if (sslAcceptor == null)
        {
            throw new org.omg.CORBA.INITIALIZE
                ("no acceptors found, cannot create address profile");
        }

        IIOPProfile result = new IIOPProfile(address,null);
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
        ServerIIOPConnection result = new ServerIIOPConnection(socket, is_ssl, socketFactoryManager.getTCPListener());
        try
        {
            result.configure(configuration);
        }
        catch( ConfigurationException ce )
        {
            throw new org.omg.CORBA.INTERNAL("ConfigurationException: " + ce.toString());
        }
        return result;
    }

    // Acceptor classes below this line

    private String getLocalhost()
    {
        String localhost;

         try
         {
             localhost =
             InetAddress.getLocalHost().getHostAddress();
         }
         catch (UnknownHostException uhe)
         {
             if (logger.isDebugEnabled())
             {
                 logger.debug
                     ("Unable to resolve local host - using default 127.0.0.1");
             }

             localhost = "127.0.0.1";
         }
        return localhost;
    }

    public class Acceptor
        extends org.jacorb.orb.etf.ListenerBase.Acceptor
    {
        private final Object runSync = new Object();
        private final boolean keepAlive;
        protected ServerSocket serverSocket;

        protected boolean terminated = false;

        /**
         * <code>acceptorExceptionListener</code> is listener to be notified
         * of terminal failures by the acceptor.
         */
        private final AcceptorExceptionListener acceptorExceptionListener;

        /**
         * <code>firstPass</code> stores whether we have already done
         * one pass in the run method i.e. have accepted one socket.
         */
        private boolean firstPass;

        protected Acceptor(String name)
        {
            super();
            // initialization deferred to init() method due to JDK bug
            setDaemon(true);
            setName(name);

            keepAlive = configuration.getAttributeAsBoolean("jacorb.connection.server.keepalive", false);

            try
            {
                acceptorExceptionListener = (AcceptorExceptionListener)configuration.getAttributeAsObject("jacorb.acceptor_exception_listener", DefaultAcceptorExceptionListener.class.getName());
            }
            catch (ConfigurationException e)
            {
                logger.error("couldn't create a AcceptorExceptionListener", e);
                throw new IllegalArgumentException("wrong configuration: " + e);
            }
        }

        public void init()
        {
            serverSocket = createServerSocket();

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Created socket listener on " +
                              serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort() );
            }
       }

        /**
         * template method that is invoked during the accept loop
         * before an incoming connection is accepted.
         */
        protected void beginAccept() throws InterruptedException
        {
            // empty to be overridden
        }

        /**
         * template method that is invoked during the accept loop
         * after an incoming connection was processed.
         */
        protected void endAccept()
        {
            // empty to be overridden
        }

        public final void run()
        {
            while(true)
            {
                try
                {
                    synchronized(runSync)
                    {
                        if (terminated)
                        {
                            if (logger.isInfoEnabled())
                            {
                                logger.info( "Listener exited");
                            }

                            return;
                        }
                    }

                    beginAccept();

                    try
                    {
                        Socket socket = serverSocket.accept();
                        setup(socket);
                        deliverConnection (socket);
                        firstPass = true;
                    }
                    finally
                    {
                        endAccept();
                    }
                }
                catch (Exception e)
                {
                    synchronized(runSync)
                    {
                        handleExceptionInRunLoop(e, terminated);
                    }
                }
            }
        }

        /**
         * template method that is invoked when
         * an exception occurs during the run loop.
         */
        private void handleExceptionInRunLoop(Exception exception, boolean isTerminated)
        {
            if (!isTerminated)
            {
                logger.warn("unexpected exception in runloop", exception);
            }

            acceptorExceptionListener.exceptionCaught
            (
                new AcceptorExceptionEvent
                (
                    this,
                    ((BasicAdapter) up).getORB(),
                    exception
                )
            );
        }

        protected void doHandleExceptionInRunLoop(Exception exception, boolean isTerminated)
        {
            // empty to be overridden
        }

        /**
         * Terminates this Acceptor by closing the ServerSocket and interrupting
         * the run loop.
         */
        public void terminate()
        {
            synchronized(runSync)
            {
                terminated = true;
            }

            try
            {
                serverSocket.close();
            }
            catch (java.io.IOException e)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("failed to close ServerSocket", e);
                }
            }

            interrupt();
        }

        public IIOPAddress getLocalAddress()
        {
            IIOPAddress addr = new IIOPAddress
            (
                serverSocket.getInetAddress().toString(),
                serverSocket.getLocalPort()
            );

            if (configuration != null)
            {
                try
                {
                    addr.configure(configuration);
                }
                catch( ConfigurationException ce)
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("ConfigurationException", ce );
                    }
                }
            }
            return addr;
        }

        /**
         * Template method that creates the server socket.
         */
        protected ServerSocket createServerSocket()
        {
            try
            {
                return socketFactoryManager.getServerSocketFactory()
                           .createServerSocket (address.getPort(),
                                                20,
                                                address.getConfiguredHost());
            }
            catch (IOException ex)
            {
                logger.warn("could not create server socket port: " + address.getPort() + " host: " + address.getConfiguredHost(), ex);
                throw new org.omg.CORBA.INITIALIZE ("Could not create server socket (" + address.getPort() + "): " + ex.toString());
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
             socket.setKeepAlive(keepAlive);

             SSLListenerUtil.addListener(orb, socket);

             String localhost = getLocalhost();

             socketFactoryManager.getTCPListener().connectionOpened
             (
                     new TCPConnectionEvent
                     (
                             this,
                             socket.getInetAddress().getHostAddress(),
                             socket.getPort(),
                             socket.getLocalPort(),
                             localhost
                     )
             );
        }

        protected void deliverConnection(Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, false);
        }

        /**
         * <code>getAcceptorSocketLoop</code> returns whether we have done
         * a socket accept. This is useful for the AcceptorExceptionListener
         * so it can determine for instance if the SSLException has been
         * thrown before any connections have been made or after x amount of
         * connections - this allows differentiation between initial
         * configuration failure and failure to connect to a single client.
         *
         * @return a <code>boolean</code> value
         */
        public boolean getAcceptorSocketLoop()
        {
            return firstPass;
        }
    }

    private class SSLAcceptor
        extends Acceptor
    {
        private final Object renewSocketSync = new Object();
        private boolean renewingSocket;
        private boolean blockedOnAccept;
        private final int soTimeout;

        private SSLAcceptor()
        {
            super("SSLServerSocketListener");

            soTimeout = configuration.getAttributeAsInteger("jacorb.listener.server_socket_timeout", 0);
        }

        protected ServerSocket createServerSocket()
        {
            try
            {
                int port = sslAddress.getPort();
                InetAddress configuredHost = sslAddress.getConfiguredHost();
                ServerSocket socket = newServerSocket(port, configuredHost);

                return socket;
            }
            catch (IOException e)
            {
                logger.warn("could not create SSL server socket", e);
                throw new org.omg.CORBA.INITIALIZE("Could not create SSL server socket");
            }
        }

        private ServerSocket newServerSocket(int port, InetAddress configuredHost) throws IOException
        {
            ServerSocket socket = socketFactoryManager.getSSLServerSocketFactory()
                        .createServerSocket (port,
                                             20,
                                             configuredHost);

            if (soTimeout > 0)
            {
                serverSocket.setSoTimeout(soTimeout);
            }

            return socket;
        }

        protected void deliverConnection(Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, true);
        }

        protected void beginAccept() throws InterruptedException
        {
            synchronized (renewSocketSync)
            {
                //wait 'til new socket has been created
                while (renewingSocket)
                {
                    renewSocketSync.wait();
                }

                //tell renewing thread we're busy
                blockedOnAccept = true;
            }
        }

        protected void endAccept()
        {
            synchronized (renewSocketSync)
            {
                //tell renewing thread we're done
                blockedOnAccept = false;
                renewSocketSync.notifyAll();
            }
        }

        protected void doHandleExceptionInRunLoop(Exception exception, boolean isTerminated)
        {
            // we are only interested in InterruptedExceptions here
            if (!(exception instanceof InterruptedException))
            {
                return;
            }

            // accept took too long
            if (soTimeout > 0)
            {
                return;
            }

            // Thread was interrupted
            if (isTerminated)
            {
                return;
            }

            logger.warn("InterruptedException should only occur if soTimeout > 0", exception);
        }

        /**
         * Replace an existing SSLServersocket by a new one (possibly using
         * new key material) that's opened on the same address/port
         * combination.
         */
        public void renewSSLServerSocket()
        {
            //remember old settings
            int oldPort = serverSocket.getLocalPort();
            InetAddress oldAddress = serverSocket.getInetAddress();

            try
            {
                synchronized (renewSocketSync)
                {
                    //tell listener we want to renew. Attention: this needs to
                    //be done prior to waiting, otherwise we may starve
                    renewingSocket = true;

                    //wait 'til listener isn't accepting anymore
                    while (blockedOnAccept)
                    {
                        try
                        {
                            renewSocketSync.wait();
                        }
                        catch(InterruptedException e)
                        {
                            // ignored
                        }
                    }
                }

                try
                {
                    //close old socket
                    serverSocket.close();
                }
                catch (Exception e)
                {
                    logger.warn("failed to close SSLServerSocket", e);
                }

                try
                {
                    //create new ServerSocket with old host/port
                    serverSocket = newServerSocket(oldPort, oldAddress);
                }
                catch (Exception e)
                {
                    logger.warn("Failed to create SSLServerSocket", e);
                    throw new org.omg.CORBA.INITIALIZE(
                        "Could not create SSL server socket: " + e);
                }
            }
            finally
            {
                // leave critical section
                synchronized(renewSocketSync)
                {
                    //tell listener we're done
                    renewingSocket = false;
                    renewSocketSync.notifyAll();
                }
            }
        }
    }

    private class LoopbackAcceptor implements IIOPLoopback
    {
        public void start()
        {
            IIOPLoopbackRegistry.getRegistry().register(getAddress(), this);
        }

        public void terminate()
        {
            IIOPLoopbackRegistry.getRegistry().unregister(getAddress());
        }

        public void initLoopback(final IIOPLoopbackInputStream lis,
                                 final IIOPLoopbackOutputStream los)
        {
            final IIOPLoopbackConnection connection =
                new IIOPLoopbackConnection(lis, los) ;
            deliverConnection(connection);
        }

        private IIOPAddress getAddress()
        {
            final IIOPProfile profile =
                (IIOPProfile)IIOPListener.this.profile;
            return (IIOPAddress)profile.getAddress();
        }
    }
}
