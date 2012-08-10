package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
import java.lang.reflect.InvocationTargetException;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.listener.NullSSLSessionListener;
import org.jacorb.orb.listener.NullTCPConnectionListener;
import org.jacorb.orb.listener.SSLSessionListener;
import org.jacorb.orb.listener.TCPConnectionListener;
import org.jacorb.util.ObjectUtil;
import org.slf4j.Logger;

/**
 * @author Steve Osselton
 * @version $Id$
 */
public class SocketFactoryManager
    implements Configurable
{
    public static final String SUPPORT_SSL = "jacorb.security.support_ssl";
    // Properties used to define custom socket and server socket factories
    public static final String SOCKET_FACTORY            = "jacorb.net.socket_factory";
    public static final String SERVER_SOCKET_FACTORY     = "jacorb.net.server_socket_factory";
    public static final String SSL_SOCKET_FACTORY        = "jacorb.ssl.socket_factory";
    public static final String SSL_SERVER_SOCKET_FACTORY = "jacorb.ssl.server_socket_factory";

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

    private final ORB orb;

    private SocketFactory socketFactory;
    private ServerSocketFactory serverFactory;
    private ServerSocketFactory sslServerSocketFactory;
    private SocketFactory sslSocketFactory;

    private org.jacorb.config.Configuration configuration;
    private Logger logger;
    private String serverSocketFactoryClassName;
    private String socketFactoryClassName;
    private String sslServerSocketFactoryClazz;
    private String sslSocketFactoryClazz;

    public SocketFactoryManager(ORB orb)
    {
        this.orb = orb;
    }

    public void configure(Configuration config)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)config;
        logger = configuration.getLogger("jacorb.orb.factory");
        serverSocketFactoryClassName = configuration.getAttribute(SERVER_SOCKET_FACTORY, DefaultServerSocketFactory.class.getName());

        socketFactoryClassName = configuration.getAttribute(SOCKET_FACTORY, "");

        if ( socketFactoryClassName.length() == 0)
        {
            String portMin = configuration.getAttribute(PortRangeSocketFactory.MIN_PROP, "");
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

        if( configuration.getAttributeAsBoolean(SUPPORT_SSL, false))
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
            sslListener = (SSLSessionListener) configuration.getAttributeAsObject(SSL_LISTENER, NullSSLSessionListener.class.getName());
        }
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

    public synchronized ServerSocketFactory getSSLServerSocketFactory()
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
        SocketFactory result = (SocketFactory)newFactory(className, SocketFactory.class);

        logger.debug("created SSLSocketFactory: " + className);

        return result;
    }

    private ServerSocketFactory newSSLServerSocketFactory(String className)
    {
        ServerSocketFactory result = (ServerSocketFactory)newFactory(className, ServerSocketFactory.class);

        logger.debug("created SSLServerSocketFactory: " + result);

        return result;
    }

    private SocketFactory newSocketFactory(String className)
    {
        SocketFactory result = (SocketFactory)newFactory(className, SocketFactory.class);

        logger.debug("created SocketFactory: " + className);

        return result;
    }

    private ServerSocketFactory newServerSocketFactory(String className)
    {
       ServerSocketFactory factory = (ServerSocketFactory) newFactory (className, ServerSocketFactory.class);

       logger.debug("created ServerSocketFactory: " + className);

       return factory;
    }

    private Object newFactory(String className, Class expectedClazz)
    {
        try
        {
            final Class factoryClazz = ObjectUtil.classForName(className);

            if (!expectedClazz.isAssignableFrom(factoryClazz))
            {
                throw new IllegalArgumentException("Custom factory " + className + " does not implement " + expectedClazz.getName());
            }

            Constructor ctor = null;

            try
            {
                // First try getting constructor with ORB parameter
                ctor = factoryClazz.getConstructor(new Class[] { org.jacorb.orb.ORB.class });
            }
            catch (NoSuchMethodException e) // NOPMD
            {
                // ignore
            }
            catch (SecurityException e) // NOPMD
            {
                // Ignore
            }

            if (ctor == null)
            {
                try
                {
                    // First try getting constructor with ORB parameter
                    ctor = factoryClazz.getConstructor(new Class[] { org.omg.CORBA.ORB.class });
                }
                catch (NoSuchMethodException e) // NOPMD
                {
                    // ignore
                }
                catch (SecurityException e) // NOPMD
                {   
                    // Ignore
                }
            }
            
            final Object result;

            if (ctor == null)
            {
                // Default construction
                result = factoryClazz.newInstance();
            }
            else
            {
                // Construct passing ORB as parameter
                result = ctor.newInstance (new Object[] { orb });
            }

            if (result instanceof Configurable)
            {
                ((Configurable)result).configure(configuration);
            }

            return result;
        }
        catch (InvocationTargetException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("there was an invocation failure with the socket factory " + className, e.getCause());
            }

            throw new org.omg.CORBA.INITIALIZE(
                "there was an invocation failure with the " +
                "socket factory " + className + ": " + e.getCause());
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Failed to create custom socket factory " + className, e);
            }

            throw new org.omg.CORBA.INITIALIZE("Failed to create custom socket factory " +
                    className + ": " + e.toString());
        }
    }
}
