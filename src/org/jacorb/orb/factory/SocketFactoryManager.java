package org.jacorb.orb.factory;

/*
 *        Written for JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2003 Nicolas Noffke, Gerald Brose.
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

import org.jacorb.orb.*;
import org.jacorb.util.*;

public class SocketFactoryManager
{
    // Properties used to define custom socket and server socket factories

    private static final String FACTORY_PROP = "jacorb.net.socket_factory";
    private static final String SERVER_FACTORY_PROP = "jacorb.net.server_socket_factory";

    private static SocketFactory socketFactory = null;
    private static ServerSocketFactory serverFactory = null;

    private static Logger logger = null; 

    public static synchronized SocketFactory getSocketFactory (ORB orb)
    {
        if (socketFactory != null)
        {
           return socketFactory;
        }

        if (logger==null)
        {
            logger = Debug.getNamedLogger("jacorb.orb.factory");
        }

        String className = Environment.getProperty (FACTORY_PROP);

        if ((className == null) || (className.length() == 0))
        {
            String portNo = Environment.getProperty (PortRangeSocketFactory.MIN_PROP);
            if ((portNo != null) && (portNo.length() > 0))
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
           socketFactory = getFactory (orb, className);
        }

        return socketFactory;
    }

    public static synchronized ServerSocketFactory getServerSocketFactory(ORB orb)
    {
        if (serverFactory != null)
        {
           return serverFactory;
        }

        String className = Environment.getProperty(SERVER_FACTORY_PROP);

        if ((className == null) || (className.length() == 0))
        {
            // Construct default server socket factory

            serverFactory = new DefaultServerSocketFactory();
        }
        else
        {
           serverFactory = getServerFactory (orb, className);
        }

        return serverFactory;
    }

    private static SocketFactory getFactory(ORB orb, String className)
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

    private static ServerSocketFactory getServerFactory(ORB orb, String className)
    {
       java.lang.Object factory = getFactoryObject (orb, className);

       if (factory instanceof ServerSocketFactory)
       {
          return ((ServerSocketFactory) factory);
       }
       else
       {
           throw new RuntimeException("Custom factory " + 
                                      className + 
                                      " does not implement ServerSocketFactory");
       }
    }

    private static java.lang.Object getFactoryObject(ORB orb, String className)
    {
        Constructor ctor = null;
        Class sfClass;
        java.lang.Object factory;

        try
        {
            sfClass = Environment.classForName(className);

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
            throw new RuntimeException("Failed to create custom socket factory: " + className);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("SocketFactoryManager: created " + 
                         factory.getClass().getName());
        }

        return factory;
    }
}
