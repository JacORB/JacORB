package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2004 Nicolas Noffke, Gerald Brose.
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

import java.lang.reflect.Constructor;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.ORB;
import org.jacorb.orb.listener.NullSSLSessionListener;
import org.jacorb.orb.listener.NullTCPConnectionListener;
import org.jacorb.orb.listener.SSLSessionListener;
import org.jacorb.orb.listener.TCPConnectionListener;
import org.jacorb.util.ObjectUtil;

/**
 * @author Steve Osselton
 * @version $Id$
 */
public class SocketFactoryManager
    implements Configurable
{
    // Properties used to define custom socket and server socket factories
    public static final String SSL_SERVER_SOCKET_FACTORY = "jacorb.ssl.server_socket_factory";
    public static final String SSL_SOCKET_FACTORY = "jacorb.ssl.socket_factory";
    public static final String SOCKET_FACTORY = "jacorb.net.socket_factory";
    public static final String SERVER_SOCKET_FACTORY = "jacorb.net.server_socket_factory";

    /**
     * <code>TCP_LISTENER</code> should be a classname for the implementation of the
     * TCP listener interface.
     */
    public static final String TCP_LISTENER = "jacorb.net.tcp_listener";


    /**
     * <code>SSL_LISTENER</code> should be a classname for the implementation of the
     * SSL listener interface.
     */
    public static final String SSL_LISTENER = "jacorb.security.ssl.ssl_listener";


    /**
     * <code>listener</code> is a instantiated TCPConnectionListener.
     */
    private TCPConnectionListener tcpListener;


    /**
     * <code>sslListener</code> is a instantiated SSLSessionListener.
     */
    private SSLSessionListener sslListener;


    private SocketFactory socketFactory = null;
    private ServerSocketFactory serverFactory = null;
    private SSLServerSocketFactory sslServerSocketFactory;
    private ORB orb;

    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger = null;
    private String serverSocketFactoryClassName = null;
    private String socketFactoryClassName = null;
    private String portMin = null;
    boolean configured = false;
    private String sslServerSocketFactoryClazz;
    private SocketFactory sslSocketFactory;
    private String sslSocketFactoryClazz;

    public SocketFactoryManager(ORB orb)
    {
        this.orb = orb;
    }

    public void configure(Configuration config)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)config;
        logger = configuration.getNamedLogger("jacorb.orb.factory");
        serverSocketFactoryClassName = configuration.getAttribute(SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName());

        socketFactoryClassName = configuration.getAttribute(SOCKET_FACTORY, "");
        portMin = configuration.getAttribute(PortRangeSocketFactory.MIN_PROP, "");

        if ( socketFactoryClassName.length() == 0)
        {
            if ( portMin.length() > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("no SocketFactory class specified (" + SOCKET_FACTORY + "). assuming PortRangeSocketFactory as property " + PortRangeSocketFactory.MIN_PROP + " is specified.");
                }
                // If mimimum port number configured construct PortRangeSocketFactory
                socketFactoryClassName = PortRangeSocketFactory.class.getName();
            }
            else
            {
                logger.debug("defaulting to DefaultSocketFactory");
                // use default socket factory
                socketFactoryClassName = DefaultSocketFactory.class.getName();
            }
        }

        tcpListener = (TCPConnectionListener) configuration.getAttributeAsObject(TCP_LISTENER, NullTCPConnectionListener.class.getName());
        sslListener = (SSLSessionListener) configuration.getAttributeAsObject(SSL_LISTENER, NullSSLSessionListener.class.getName());

        if( configuration.getAttributeAsBoolean("jacorb.security.support_ssl", false))
        {
            sslServerSocketFactoryClazz = configuration.getAttribute(SSL_SERVER_SOCKET_FACTORY, "");

            if(  sslServerSocketFactoryClazz.length() == 0 )
            {
                throw new ConfigurationException( "SSL support is on, but the property \"" + SSL_SERVER_SOCKET_FACTORY + "\" is not set!" );
            }

            sslSocketFactoryClazz = configuration.getAttribute(SSL_SOCKET_FACTORY, "");

            if (sslSocketFactoryClazz.length() == 0)
            {
                throw new ConfigurationException("SSL support is on, but the property \"" + SSL_SOCKET_FACTORY + "\" is not set");
            }
        }

        configured = true;
    }


    public synchronized SocketFactory getSocketFactory()
    {
        if (socketFactory == null)
        {
            socketFactory = newSocketFactory(socketFactoryClassName);
        }

        return socketFactory;
    }

    public synchronized ServerSocketFactory getServerSocketFactory()
    {
        if (serverFactory == null)
        {
            serverFactory = newServerSocketFactory(serverSocketFactoryClassName);
        }

        return serverFactory;
    }

    public synchronized SSLServerSocketFactory getSSLServerSocketFactory()
    {
        if( sslServerSocketFactory == null )
        {
            sslServerSocketFactory = newSSLServerSocketFactory(sslServerSocketFactoryClazz);
        }
        return sslServerSocketFactory;
    }

    /**
     * <code>getSSLSocketFactory</code> returns a SSL socket factory.
     *
     * @return a <code>SocketFactory</code> value
     */
    public synchronized SocketFactory getSSLSocketFactory()
    {
        if (sslSocketFactory == null)
        {
            sslSocketFactory = newSSLSocketFactory(sslSocketFactoryClazz);
        }
        return sslSocketFactory;
    }



    /**
     * <code>getTCPListener</code> provides an accessor for the instantiated
     * TCPConnectionListener.
     */
    public TCPConnectionListener getTCPListener ()
    {
        return tcpListener;
    }

    /**
     * <code>getSSLListener</code> provides an accessor for the instantiated
     * SSLConnectionListener.
     */
    public SSLSessionListener getSSLListener ()
    {
        return sslListener;
    }

    private SocketFactory newSSLSocketFactory(String className)
    {
        SocketFactory result = (SocketFactory)newFactory(orb, className, SocketFactory.class);

        logger.debug("created SSLSocketFactory: " + className);

        return result;
    }


    private SSLServerSocketFactory newSSLServerSocketFactory(String className)
    {
        SSLServerSocketFactory result = (SSLServerSocketFactory)newFactory(orb, className, SSLServerSocketFactory.class);

        logger.debug("created SSLServerSocketFactory: " + result);

        return result;
    }

    private SocketFactory newSocketFactory(String className)
    {
        SocketFactory result = (SocketFactory)newFactory(orb, className, SocketFactory.class);

        logger.debug("created SocketFactory: " + className);

        return result;
    }

    private ServerSocketFactory newServerSocketFactory(String className)
    {
       ServerSocketFactory factory = (ServerSocketFactory) newFactory (orb, className, ServerSocketFactory.class);

       logger.debug("created ServerSocketFactory: " + className);

       return factory;
    }

    private Object newFactory(ORB orb, String className, Class expectedClazz)
    {
        try
        {
            Class factoryClazz = ObjectUtil.classForName(className);

            if (!expectedClazz.isAssignableFrom(factoryClazz))
            {
                throw new RuntimeException("Custom factory " + className + " does not implement " + expectedClazz.getName());
            }

            Constructor ctor = null;

            if (orb != null)
            {
                try
                {
                    // First try getting constructor with ORB parameter
                    ctor = factoryClazz.getConstructor (new Class[] { ORB.class });
                }
                catch (Exception ex)
                {
                    // Ignore
                }
            }

            final Object result;

            if (ctor != null)
            {
                // Construct passing ORB as parameter
                result = ctor.newInstance (new Object[] { orb });
            }
            else
            {
                // Default construction
                result = factoryClazz.newInstance();
            }

            if (result instanceof Configurable)
            {
                ((Configurable)result).configure(configuration);
            }

            return result;
        }
        catch (Exception ex)
        {
            logger.debug("Failed to create custom socket factory", ex);
            throw new RuntimeException("Failed to create custom socket factory: " +
                                       className);
        }
    }
}
