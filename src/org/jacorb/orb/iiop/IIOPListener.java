package org.jacorb.orb.iiop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
import java.util.*;

import org.omg.ETF.*;
import org.omg.CSIIOP.*;
import org.omg.SSLIOP.*;

import org.jacorb.util.Debug;
import org.jacorb.util.Environment;
import org.jacorb.orb.factory.*;
import org.jacorb.orb.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPListener extends _ListenerLocalBase
{
    private ServerSocketFactory    serverSocketFactory    = null;
    private SSLServerSocketFactory sslServerSocketFactory = null;
    
    private Acceptor    acceptor    = null;
    private SSLAcceptor sslAcceptor = null;

    private IIOPProfile endpoint = null;

    /**
     * Reference to the ORB, for delivering
     * incoming connections via upcalls.
     */
    private org.omg.ETF.Handle up = null;
    
    /**
     * Queue of incoming connections, which will be
     * delivered via calls to the accept() method.
     * Connections will only be put into this list
     * if no Handle has been set.
     */
    private List incoming_connections = new ArrayList();
    
    private boolean terminated = false;
    
    public IIOPListener()
    {
        if (!isSSLRequired())
        {
            acceptor = new Acceptor();
            acceptor.init();
        }
            
        if (isSSLSupported())
        {
            sslAcceptor = new SSLAcceptor();
            sslAcceptor.init();
        }
            
        endpoint = createEndPointProfile();
    }
    
    /**
     * This call establishes the link between the ORB (i.e. the Handle
     * instance) and a server endpoint of the plugged-in transport.
     * All calls upwards into the ORB shall use the given instance.
     */
    public void set_handle (Handle up)
    {
        this.up = up;
    }

    /**
     * This call is an alternative to using set_handle() to initiate the
     * callback-style of accepting new connections. This call blocks until
     * a client connects to the server. Then a new Connection instance is
     * returned. The transport plug-in must ensure that a thread blocked
     * in accept() returns when destroy() is called with a null object
     * reference. The transport plug-in must raise the CORBA::BAD_INV_ORDER
     * with minor code {TBD} if the ORB calls this operation and set_handle()
     * has ever been called previously on the same listener instance.
     */
    public Connection accept()
    {
        if (up != null)
            throw new org.omg.CORBA.BAD_INV_ORDER 
                ("Must not call accept() when a Handle has been set");
        else
        {
            synchronized (incoming_connections)
            {
                while (!terminated && 
                       incoming_connections.isEmpty())
                {
                    try
                    {
                        incoming_connections.wait();
                    }
                    catch (InterruptedException ex)
                    {
                        // ignore
                    }
                }
                if (!terminated)
                    return (Connection)incoming_connections.remove (0);
                else
                    return null;
            }
        }       
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
        if (acceptor != null)
            acceptor.start();
            
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
        if (acceptor != null)
            acceptor.terminate();
            
        if (sslAcceptor != null)
            sslAcceptor.terminate();

        this.terminated = true;
        if (up == null)
            incoming_connections.notifyAll();
    }

    /**
     * The connection instance is returned to the Listener. It now shall
     * signal any incoming data to the Handle.
     */
    public void completed_data (Connection conn)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Returns a copy of the profile describing the endpoint
     * of this instance.
     */
    public Profile endpoint()
    {
        return endpoint.copy();
    }
    
    // internal methods below this line

    /**
     * Returns true if this Listener should support SSL connections.
     */
    private boolean isSSLSupported()
    {
        return Environment.isPropertyOn ("jacorb.security.support_ssl");    
    }

    /**
     * Returns true if this Listener should <i>require</i> SSL, and not
     * offer plain connections.
     */
    private boolean isSSLRequired()
    {
        if (isSSLSupported())
        {        
            //the following is used as a bit mask to check, if any of
            //these options are set
            int minimum_options =
                Integrity.value |
                Confidentiality.value |
                DetectReplay.value |
                DetectMisordering.value |
                EstablishTrustInTarget.value |
                EstablishTrustInClient.value;
        
            String prop = Environment.getProperty 
                               ("jacorb.ssl.server.required_options");
            if (prop != null)
            {
                try
                {
                    int server_requires = Integer.parseInt (prop, 16);
                    return ((server_requires & minimum_options) != 0);
                }
                catch (NumberFormatException e)
                {
                    throw new org.omg.CORBA.INITIALIZE
                      ("could not parse jacorb.ssl.server.required_options: "
                       + prop);
                }
            }
        }
        return false;
    }

    /**
     * Returns the server timeout that has been specified, or zero if none
     * has been set.
     */
    private int getServerTimeout()
    {
        String prop = Environment.getProperty ("jacorb.connection.server_timeout");
        if (prop != null)
            return Integer.parseInt (prop);
        else
            return 0;   
    }
    
    private ServerSocketFactory getServerSocketFactory()
    {
        if (serverSocketFactory == null)
        {
            serverSocketFactory = 
                SocketFactoryManager.getServerSocketFactory ((org.jacorb.orb.ORB)null);        
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
            // This is a hack: We need the ORB to create an SSL
            // server socket factory, but we don't have it here.
            // So we let the BasicAdapter create the factory, and
            // store it in a static variable (which is not specific
            // to a particular ORB again, but this is the way it
            // has always been in JacORB).
            
            // TODO: Check what is the right thing to do.

            sslServerSocketFactory 
                = org.jacorb.orb.BasicAdapter.ssl_socket_factory;
            if (sslServerSocketFactory == null)
                throw new org.omg.CORBA.INITIALIZE 
                                  ("No SSL server socket factory found");
        }
        return sslServerSocketFactory;
    }
    
    private IIOPProfile createEndPointProfile()
    {
        IIOPProfile result = null;
        if (acceptor != null)
        {
            result = new IIOPProfile (acceptor.getLocalAddress(), null);
        }
        else if (sslAcceptor != null)
        {
            // only an SSL acceptor exists: make a dummy primary address
            // (port number zero)
            String host = sslAcceptor.getLocalAddress().getHost();
            result = new IIOPProfile (new IIOPAddress (host, 0), null);
        }
        else
            throw new org.omg.CORBA.INITIALIZE
                ("failed to create endpoint profile");

        if (sslAcceptor != null)
        {
            result.addComponent (TAG_SSL_SEC_TRANS.value,
                                 createSSL(), SSLHelper.class);
        }
        return result;
    }

    private SSL createSSL()
    {
        int target_supports = Environment.getIntProperty
        (
            "jacorb.security.ssl.server.supported_options", 16
        );
        int target_requires = Environment.getIntProperty 
        (
            "jacorb.security.ssl.server.required_options", 16
        );
        
        return new SSL
        (
            (short)target_supports, (short)target_requires,
            (short)sslAcceptor.getLocalAddress().getPort()
        );
    }
    
    /**
     * Returns the IP address that this listener should listen on,
     * if one has been configured explicitly.  If none has been
     * specified, returns the IP address of the local host.
     */
    private InetAddress getConfiguredHost()
    {
        try
        {
            String oa_addr = Environment.getProperty ("OAIAddr");
            if (oa_addr == null)
                return InetAddress.getLocalHost();
            else 
            {
                InetAddress result = InetAddress.getByName(oa_addr);
                return result;
            }
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
        String prop = Environment.getProperty ("OAPort");
        if (prop != null)
            return Integer.parseInt (prop);
        else
            return 0;
    }
    
    private int getConfiguredSSLPort()
    {
        String prop = Environment.getProperty ("OASSLPort");
        if (prop != null)
            return Integer.parseInt (prop);
        else
            return 0;   
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
            result = new ServerIIOPConnection (socket, isSSL);
        }
        catch (IOException ex)
        {
            Debug.output (1, "Could not create connection from socket: " + ex);
            return;
        }

        if (up != null)
        {
              up.add_input (result);
        }
        else
        {
            synchronized (incoming_connections)
            {
                incoming_connections.add (result);
                incoming_connections.notifyAll();
            }
        }
    }
    
    // Acceptor classes below this line
    
    private class Acceptor extends Thread 
    {
        protected ServerSocket serverSocket;
        private   boolean      terminated = false;
        
        public Acceptor()
        {
            // initialization deferred to init() method due to JDK bug
        }
        
        public void init()
        {
            serverSocket = createServerSocket();
        }
        
        public void run()
        {
            while (!terminated)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    setup (socket);
                    deliverConnection (socket);
                }
                catch (Exception e)
                {
                    if (!terminated)
                        Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT, e );
                }
            }
            Debug.output( Debug.INFORMATION | Debug.ORB_CONNECT,
                          "Listener exited");
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
                Debug.output( Debug.INFORMATION | Debug.ORB_CONNECT, e );
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
                Debug.output (2, ex);
                throw new org.omg.CORBA.INITIALIZE ("Could not create server socket");
            }
        }
        
        /**
         * Template method that sets up the socket right after the
         * connection has been established.  Subclass implementations
         * must call super.setup() first.
         */
        protected void setup (Socket socket) throws IOException
        {
             socket.setSoTimeout (getServerTimeout());
        }

        protected void deliverConnection (Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, false);
        }
    }    

    private class SSLAcceptor extends Acceptor
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
                Debug.output (2, e);
                throw new org.omg.CORBA.INITIALIZE 
                                           ("Could not create SSL server socket");
            }                         
        }
        
        public void setup (Socket socket) throws IOException
        {
            super.setup (socket);
            getSSLServerSocketFactory().switchToClientMode (socket);
        }
        
        protected void deliverConnection (Socket socket)
        {
            IIOPListener.this.deliverConnection (socket, true);
        }
    }

}
