package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.omg.ETF.Factories;

import org.jacorb.orb.*;
import org.jacorb.orb.factory.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.util.ObjectUtil;

/**
 * This class manages connections.<br>
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class ClientConnectionManager
    implements Configurable
{
    private org.jacorb.orb.ORB orb = null;

    /** connection mgmt. */
    private Map connections = new HashMap();

    private SocketFactory socket_factory = null;
    private SocketFactory ssl_socket_factory = null;

    private RequestListener request_listener = null;

    private MessageReceptorPool receptor_pool = null;

    private TransportManager transport_manager = null;
    private GIOPConnectionManager giop_connection_manager = null;

    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger = null;

    public ClientConnectionManager( ORB orb,
                                    TransportManager transport_manager,
                                    GIOPConnectionManager giop_connection_manager )
    {
        this.orb = orb;
        this.transport_manager = transport_manager;
        this.giop_connection_manager = giop_connection_manager;
    }

    /**
     * configure this connection manager
     */

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        // Moved from the constructor to facilitate logging.
        receptor_pool = MessageReceptorPool.getInstance(myConfiguration);

        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.orb.giop");

        request_listener = new NoBiDirClientRequestListener(logger);

        socket_factory = 
            transport_manager.getSocketFactoryManager().getSocketFactory();

        if (configuration.getAttribute("jacorb.security.support_ssl","off").equals("on") )
        {
            String s = configuration.getAttribute("jacorb.ssl.socket_factory","");
            if ( s.length() == 0)
            {
                throw new RuntimeException( "SSL support is on, but the property \"jacorb.ssl.socket_factory\" is not set!" );
            }

            try
            {
                Class ssl = ObjectUtil.classForName(s);

                Constructor constr = 
                    ssl.getConstructor( new Class[]{ ORB.class });

                ssl_socket_factory = 
                    (SocketFactory)constr.newInstance( new Object[]{ orb });
            }
            catch (Exception e)
            {
                throw new RuntimeException( "SSL support is on, but the ssl socket factory can't be instantiated ("+e.getMessage()+")!" );
            }
        }
    }


    public void setRequestListener( RequestListener listener )
    {
        request_listener = listener;
    }

    public synchronized ClientConnection getConnection
                                              (org.omg.ETF.Profile profile)
    {
        /* look for an existing connection */

        ClientConnection c =
            (ClientConnection)connections.get( profile );

        if (c == null)
        {
            int tag = profile.tag();
            Factories factories = transport_manager.getFactories (tag);
            if (factories == null)
            {
                throw new RuntimeException
                    ("No transport plugin for profile tag " + tag);
            }
            GIOPConnection connection =
                giop_connection_manager.createClientGIOPConnection(
                    profile,
                    factories.create_connection (null),
                    request_listener,
                    null );

            c = new ClientConnection( connection, orb, this,
                                      profile, true );

            if( logger.isInfoEnabled())
                logger.info("ClientConnectionManager: created new "
                            + c.getGIOPConnection().toString() );

            connections.put( profile, c );
            receptor_pool.connectionCreated( connection );
        }
        else
        {
            if( logger.isInfoEnabled())
                logger.info("ClientConnectionManager: found "
                            + c.getGIOPConnection().toString());

        }

        c.incClients();

        return c;
    }

    /**
     * Only used by Delegate for client-initiated connections.
     */
    public synchronized void releaseConnection( ClientConnection c )
    {
        if ( c.decClients() )
        {
            if (logger.isDebugEnabled())
                logger.debug ("ClientConnectionManager: releasing " 
                              + c.getGIOPConnection().toString());
            c.close();
            connections.remove(c.getRegisteredProfile());
        }
        else
        {
            // not sure if this should be a warning or even an error
            if (logger.isDebugEnabled())
                logger.debug ("ClientConnectionManager: cannot release "
                              + c.getGIOPConnection().toString()
                              + " (still has " + c.numClients() + " client(s))");
        }
    }

    /**
     * Only used by ClientConnection to unregister server-side of
     * BiDir connection.
     */
    public synchronized void removeConnection(ClientConnection c)
    {
        connections.remove( c.getRegisteredProfile() );
    }

    public synchronized void addConnection( GIOPConnection connection,
                                            org.omg.ETF.Profile profile )
    {
        if( !connections.containsKey( profile ))
        {
            ClientConnection c = new ClientConnection
            (
                connection, orb, this,
                profile,
                false
            );

            //this is a bit of a hack: the bidirectional client
            //connections have to persist until their underlying GIOP
            //connection is closed. Therefore, we set the initial
            //client count to 1, so the connection will be kept even
            //if there are currently no associated Delegates.

            c.incClients();

            connections.put( profile, c );
        }
    }

    public void shutdown()
    {
        /* release all open connections */

        for( Iterator i = connections.values().iterator(); i.hasNext(); )
        {
            ((ClientConnection) i.next()).close();
        }

        if( logger.isDebugEnabled())
        {
            logger.debug("ClientConnectionManager shut down (all connections released)");
        }

        connections.clear();
    }
}
