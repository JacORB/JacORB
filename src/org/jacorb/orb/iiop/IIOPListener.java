package org.jacorb.orb.iiop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.BasicAdapter;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.jacorb.orb.factory.ServerSocketFactory;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.orb.listener.AcceptorExceptionEvent;
import org.jacorb.orb.listener.AcceptorExceptionListener;
import org.jacorb.orb.listener.DefaultAcceptorExceptionListener;
import org.jacorb.orb.listener.SSLListenerUtil;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.listener.TCPConnectionListener;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.ETF.Connection;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author Andre Spiegel
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

    public void configure(Configuration config)
        throws ConfigurationException
    {
        super.configure(config);

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
            configuration.getAttributeAsBoolean("jacorb.security.support_ssl", false);

        target_supports =
           configuration.getAttributeAsInteger("jacorb.security.ssl.server.supported_options", 0x20, 16); // 16 is the base as we take the string value as hex!

        // make sure that the minimum options are always in the set of supported options
        target_supports |= MIN_SSL_OPTIONS;

        target_requires =
           configuration.getAttributeAsInteger("jacorb.security.ssl.server.required_options", 0, 16);


        generateSSLComponents =
            configuration.getAttributeAsBoolean("jacorb.security.ssl_components_added_by_ior_interceptor", true);


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

        profile = createAddressProfile();

        if (configuration.getAttributeAsBoolean("jacorb.iiop.enable_loopback", true))
        {
            loopbackAcceptor = new LoopbackAcceptor();
        }
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

        if (loopbackAcceptor != null)
        {
            loopbackAcceptor.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void destroy()
    {
        if (loopbackAcceptor != null)
        {
            loopbackAcceptor.terminate();
        }

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

        IIOPProfile result = new IIOPProfile(address, null, orb.getGIOPMinorVersion());
        result.configure(configuration);

        if (sslAcceptor != null && generateSSLComponents)
        {
             result.addComponent (TAG_SSL_SEC_TRANS.value,
                                  createSSL(), SSLHelper.class);
        }

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
                logger.error("Could not create connection from socket: ", ex);
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
        final TCPConnectionListener tcpListener = socketFactoryManager.getTCPListener();
        ServerIIOPConnection result = new ServerIIOPConnection(socket, is_ssl, tcpListener);

        if (tcpListener.isListenerEnabled())
        {
            tcpListener.connectionOpened
            (
                    new TCPConnectionEvent
                    (
                            result,
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            socket.getLocalPort(),
                            getLocalhost()
                    )
            );
        }

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
        return IIOPAddress.getLocalHostAddress (logger);
    }

    public class Acceptor
        extends org.jacorb.orb.etf.ListenerBase.Acceptor
    {
        private final Object runSync = new Object();
        private final boolean keepAlive;
        private final IIOPAddress addressToUse;
        protected final int soTimeout;
        protected final boolean reuseAddress;
        protected ServerSocket serverSocket;
        protected String info = "";

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

        protected Acceptor(String name, IIOPAddress target) throws ConfigurationException
        {
            super();
            // initialization deferred to init() method due to JDK bug
            setDaemon(true);
            setName(name);

            keepAlive = configuration.getAttributeAsBoolean("jacorb.connection.server.keepalive", false);
            soTimeout = configuration.getAttributeAsInteger("jacorb.listener.server_socket_timeout", 0);
            reuseAddress = configuration.getAttributeAsBoolean("jacorb.connection.server.reuse_address", false);

            try
            {
                acceptorExceptionListener = (AcceptorExceptionListener)configuration.getAttributeAsObject("jacorb.acceptor_exception_listener", DefaultAcceptorExceptionListener.class.getName());
            }
            catch (ConfigurationException e)
            {
                logger.error("couldn't create a AcceptorExceptionListener", e);
                throw new IllegalArgumentException("wrong configuration: " + e);
            }

            addressToUse = target;
        }

        protected Acceptor(String name) throws ConfigurationException
        {
            this(name, address);
        }

        public void init()
        {
            serverSocket = createServerSocket();

            if( logger.isDebugEnabled() )
            {
                logger.debug( "Created " + info + "socket listener on " +
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
            try
            {
                runLoop();
            }
            finally
            {
                if (!terminated)
                {
                    logger.error(info + "Listener is unexpectedly exiting. the ORB is in an non-functional state!");
                }
                else
                {
                    logger.info(info + "Listener exiting");
                }
            }
        }

        public final void runLoop()
        {
            while(true)
            {
                try
                {
                    synchronized(runSync)
                    {
                        if (terminated)
                        {
                            return;
                        }
                    }

                    beginAccept();

                    try
                    {
                        Socket socket = serverSocket.accept();

                        if (terminated)
                        {
                            // JAC#439
                            // serverSocket.close() and interruption of the
                            // Listener thread seem not to reliable prevent
                            // the serverSocket from accepting further connection requests.
                            // so we might come here although the IIOPListener was already
                            // shutdown.
                            // TODO: prohably it would be better to use
                            // SocketChannel socketChannel = serverSocket.getChannel().accept();
                            // Socket socket = socketChannel.socket();
                            // for more reliability

                            if ( ! (socket instanceof SSLSocket) && ! socket.isClosed())
                            {
                                socket.shutdownOutput();
                            }
                            socket.close();

                            if (logger.isInfoEnabled())
                            {
                                logger.info("closed Socket " + socket + " as " + info + "ServerSocket was closed.");
                            }
                            return;
                        }

                        setup(socket);

                        try
                        {
                            deliverConnection(socket);
                            firstPass = true;
                        }
                        catch(NO_RESOURCES e)
                        {
                            // as no ServerMessageReceptor
                            // was created for this socket
                            // we'll at least close
                            // the socket

                            if ( ! (socket instanceof SSLSocket) && ! socket.isClosed())
                            {
                                socket.shutdownOutput();
                            }
                            socket.close();
                            throw e;
                        }
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
                catch(OutOfMemoryError e)
                {
                    logger.error("OutOfMemory", e);
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
                logger.warn("unexpected exception in " + info + "Acceptor runloop", exception);
            }

            doHandleExceptionInRunLoop(exception, isTerminated);

            try
            {
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
            catch(Exception e)
            {
                logger.error("error in Acceptor Exception Listener: " + acceptorExceptionListener + " while handling exception: " + exception, e);
            }
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
                    logger.warn("failed to close " + info + "ServerSocket", e);
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

        protected ServerSocket createServerSocket()
        {
            final InetAddress configuredHost = addressToUse.getConfiguredHost();
            final int port = addressToUse.getPort();

            return createServerSocket(configuredHost, port);
        }

        protected ServerSocket createServerSocket(final InetAddress host, final int port)
        {
            try
            {
                final ServerSocket result =
                    getServerSocketFactory().createServerSocket(port, 20, host);

                if (soTimeout > 0)
                {
                    result.setSoTimeout(soTimeout);
                }

                if (reuseAddress)
                {
                    result.setReuseAddress(true);
                }

                return result;
            }
            catch (IOException ex)
            {
                logger.warn("could not create " + info + "ServerSocket port: " + port + " host: " + host, ex);
                throw new org.omg.CORBA.INITIALIZE ("Could not create " + info + "ServerSocket (" + port + "): " + ex.toString());
            }
        }

        protected ServerSocketFactory getServerSocketFactory()
        {
            return socketFactoryManager.getServerSocketFactory();
        }

        /**
         * Template method that sets up the socket right after the
         * connection has been established.  Subclass implementations
         * may implement their own logic by overriding doSetup
         */
        protected final void setup(Socket socket)
            throws IOException
        {
             socket.setSoTimeout(serverTimeout);
             socket.setKeepAlive(keepAlive);
             socket.setTcpNoDelay(true);

             try
             {
                 SSLListenerUtil.addListener(orb, socket);
             }
             catch(Throwable t)
             {
                 logger.warn("unexpected exception in ssl listener", t);
             }

             doSetup(socket);
        }

        protected void doSetup(Socket socket)
        {
            // empty to be overridden
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

        private SSLAcceptor() throws ConfigurationException
        {
            super("SSLServerSocketListener", sslAddress);
            info = "SSL";
        }

        protected ServerSocketFactory getServerSocketFactory()
        {
            return socketFactoryManager.getSSLServerSocketFactory();
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
            final InetAddress oldAddress = serverSocket.getInetAddress();
            final int oldPort = serverSocket.getLocalPort();

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

                //create new ServerSocket with old host/port
                serverSocket = createServerSocket(oldAddress, oldPort);
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
        private final IIOPAddress listenerAddress;
        private final IIOPAddress loopbackAddress;
        private final boolean isSSL;

        public LoopbackAcceptor()
        {
            final IIOPProfile iiopProfile = (IIOPProfile)IIOPListener.this.profile;
            listenerAddress = (IIOPAddress)iiopProfile.getAddress().copy();

            loopbackAddress = (IIOPAddress) listenerAddress.copy();
            loopbackAddress.setHostname("127.0.0.1");
            isSSL = iiopProfile.getSSL() != null;

            if (isSSL)
            {
                listenerAddress.setPort(iiopProfile.getSSLPort());
                loopbackAddress.setPort(iiopProfile.getSSLPort());
            }
       }

        public void start()
        {
            IIOPLoopbackRegistry.getRegistry().register(listenerAddress, this);
            IIOPLoopbackRegistry.getRegistry().register(loopbackAddress, this);
        }

        public void terminate()
        {
            IIOPLoopbackRegistry.getRegistry().unregister(listenerAddress);
            IIOPLoopbackRegistry.getRegistry().unregister(loopbackAddress);
        }

        public void initLoopback(final String connectionInfo,
                final IIOPLoopbackInputStream lis,
                final IIOPLoopbackOutputStream los)
        {
            final IIOPLoopbackConnection connection = new IIOPLoopbackConnection(lis, los)
            {
                {
                    connection_info = connectionInfo;
                }

                public boolean isSSL()
                {
                    return isSSL;
                }
            };

            try
            {
                connection.configure(configuration);
            }
            catch(ConfigurationException e)
            {
                throw new RuntimeException("should never happen", e);
            }
            deliverConnection(connection);
        }
    }
}
