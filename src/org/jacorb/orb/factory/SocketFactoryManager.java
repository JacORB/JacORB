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

import java.net.*;
import java.lang.reflect.Constructor;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.*;
import org.jacorb.util.ObjectUtil;

public class SocketFactoryManager
    implements Configurable
{
    // Properties used to define custom socket and server socket factories
    private static final String FACTORY_PROP = "jacorb.net.socket_factory";
    private static final String SERVER_FACTORY_PROP = "jacorb.net.server_socket_factory";

    private SocketFactory socketFactory = null;
    private ServerSocketFactory serverFactory = null;
    private ORB orb;

    /** the configuration object  */
    private Configuration configuration = null;
    private Logger logger = null; 
    private String serverFactoryClassName = null;
    private String factoryClassName = null;
    private String portNo = null;

    public SocketFactoryManager(ORB orb)
    {
        this.orb = orb;
    }


    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = configuration;
        logger = 
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.orb.factory");
        serverFactoryClassName = configuration.getAttribute(SERVER_FACTORY_PROP, "");
        factoryClassName = configuration.getAttribute(FACTORY_PROP, "");
        portNo = configuration.getAttribute(PortRangeSocketFactory.MIN_PROP, "");
    }


    public synchronized SocketFactory getSocketFactory()
    {
        if (socketFactory != null)
        {
           return socketFactory;
        }

        if ( factoryClassName.length() == 0)
        {
            if ( portNo.length() > 0)
            {
                // If mimimum port number configured construct PortRangeSocketFactory
                socketFactory = new PortRangeSocketFactory();
            }
            else
            {
                // Construct default socket factory
                socketFactory = new DefaultSocketFactory();
            }
        }
        else
        {
            socketFactory = getFactory(factoryClassName);
        }

        return socketFactory;
    }

    public synchronized ServerSocketFactory getServerSocketFactory()
    {
        if (serverFactory != null)
        {
           return serverFactory;
        }

        if ((serverFactoryClassName == null) || (serverFactoryClassName.length() == 0))
        {
            // Construct default server socket factory
            serverFactory = new DefaultServerSocketFactory();
        }
        else
        {
            serverFactory = getServerFactory(serverFactoryClassName);
        }

        return serverFactory;
    }

    private SocketFactory getFactory(String className)
    {
       java.lang.Object factory = getFactoryObject(orb, className);

       if (factory instanceof SocketFactory)
       {
          return ((SocketFactory) factory);
       }
       else
       {
           throw new RuntimeException
               ("Custom factory " + className + " does not implement SocketFactory");
       }
    }

    private ServerSocketFactory getServerFactory(String className)
    {
       java.lang.Object factory = getFactoryObject (orb, className);

       if (factory instanceof ServerSocketFactory)
       {
          if (factory instanceof Configurable)
          {
             try
             {
                ((Configurable)factory).configure(configuration);
             }
             catch (ConfigurationException ce)
             {
               throw new RuntimeException("Configurable custom server socket factory " + 
                                          className + 
                                          " could not be configured !", ce);
             }   
          }
          return ((ServerSocketFactory) factory);
       }
       else
       {
           throw new RuntimeException("Custom factory " + 
                                      className + 
                                      " does not implement ServerSocketFactory");
       }
    }

    private java.lang.Object getFactoryObject(ORB orb, String className)
    {
        Constructor ctor = null;
        Class sfClass;
        java.lang.Object factory;

        try
        {
            sfClass = ObjectUtil.classForName(className);

            if (orb != null)
            {
                try
                {
                    // First try getting constructor with ORB parameter
                    ctor = sfClass.getConstructor (new Class[] { ORB.class });
                }
                catch (Exception ex)
                {
                    // Ignore
                }
            }

            if (ctor != null)
            {
                // Construct passing ORB as parameter
                factory = ctor.newInstance (new Object[] { orb });
            }
            else
            {
                // Default construction
                factory = sfClass.newInstance();
            }
        }
        catch (Exception ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(ex.getMessage());
            }
            throw new RuntimeException("Failed to create custom socket factory: " + 
                                       className);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("SocketFactoryManager: created " + 
                         factory.getClass().getName());
        }

        return factory;
    }
}
