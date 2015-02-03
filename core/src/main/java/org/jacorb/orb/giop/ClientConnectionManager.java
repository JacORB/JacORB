package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CORBA.BAD_PARAM;
import org.omg.ETF.Factories;
import org.omg.ETF.Profile;
import org.slf4j.Logger;

/**
 * This class manages connections.
 *
 * @author Gerald Brose, FU Berlin
 */

public class ClientConnectionManager
    implements Configurable
{
    private final org.jacorb.orb.ORB orb;

    /** connection mgmt. */
    private final Map<Profile, ClientConnection> connections = new HashMap<Profile, ClientConnection>();

    private RequestListener request_listener;

    private MessageReceptorPool receptor_pool;

    private final TransportManager transport_manager;
    private final GIOPConnectionManager giop_connection_manager;

    /** the configuration object  */
    private Logger logger;

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
        receptor_pool = new MessageReceptorPool("client", "ClientMessageReceptor", myConfiguration);

        org.jacorb.config.Configuration configuration = myConfiguration;
        logger = configuration.getLogger("org.jacorb.orb.giop");

        request_listener = new NoBiDirClientRequestListener(orb, logger);
    }


    public void setRequestListener( RequestListener listener )
    {
        request_listener = listener;
    }

    public synchronized ClientConnection getConnection(org.omg.ETF.Profile profile)
    {
        /* look for an existing connection */

        ClientConnection clientConnection = connections.get( profile );

        if (clientConnection == null && profile instanceof IIOPProfile)
        {
            IIOPProfile iiopProfile = (IIOPProfile) profile;

            if (iiopProfile.getSSL() != null)
            {
                final IIOPProfile sslProfile = iiopProfile.toNonSSL();

                clientConnection = connections.get(sslProfile);
            }
        }

        // Don't return a closed connection.
        if (clientConnection != null && clientConnection.isClosed())
        {
            releaseConnection (clientConnection);
            clientConnection = null;
        }

        if (clientConnection == null)
        {
            int tag = profile.tag();
            Factories factories = transport_manager.getFactories (tag);
            if (factories == null)
            {
                throw new BAD_PARAM("No transport plugin for profile tag " + tag);
            }
            GIOPConnection connection =
                giop_connection_manager.createClientGIOPConnection(
                    profile,
                    factories.create_connection (null),
                    request_listener,
                    null );

            clientConnection = new ClientConnection( connection, orb, this,
                                      profile, true );

            if( logger.isInfoEnabled())
            {
                logger.info("ClientConnectionManager: created new "
                            + clientConnection.getGIOPConnection().toString() );
            }

            receptor_pool.connectionCreated( connection );
            connections.put( profile, clientConnection );
        }
        else
        {
            if( logger.isInfoEnabled())
            {
                logger.info("ClientConnectionManager: found "
                            + clientConnection.getGIOPConnection().toString());
            }
        }

        clientConnection.incClients();

        return clientConnection;
    }

    /**
     * Only used by Delegate for client-initiated connections.
     */
    public synchronized void releaseConnection( ClientConnection connection )
    {
        if ( connection.decClients() || connection.isClosed ())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug ("ClientConnectionManager: releasing " +
                              ( connection.isClosed () ? "closed connection " : "" ) +
                              connection.getGIOPConnection().toString());
            }
            connection.close();
            connections.remove(connection.getRegisteredProfile());
        }
        else
        {
            // not sure if this should be a warning or even an error
            if (logger.isDebugEnabled())
            {
                logger.debug ("ClientConnectionManager: cannot release "
                              + connection.getGIOPConnection().toString()
                              + " (still has " + connection.numClients() + " client(s))");
            }
        }
    }

    /**
     * Only used by ClientConnection to unregister server-side of
     * BiDir connection.
     */
    public synchronized void removeConnection(ClientConnection connection)
    {
        connections.remove( connection.getRegisteredProfile() );
    }

    public synchronized void addConnection( GIOPConnection connection,
                                            org.omg.ETF.Profile profile )
    {
        if( !connections.containsKey( profile ))
        {
            ClientConnection clientConnection = new ClientConnection
            (
                connection,
                orb,
                this,
                profile,
                false
            );

            //this is a bit of a hack: the bidirectional client
            //connections have to persist until their underlying GIOP
            //connection is closed. Therefore, we set the initial
            //client count to 1, so the connection will be kept even
            //if there are currently no associated Delegates.

            clientConnection.incClients();

            connections.put( profile, clientConnection );
        }
    }

    public synchronized void shutdown()
    {
        /* release all open connections */

        for( Iterator<ClientConnection> i = new HashSet<ClientConnection>(connections.values()).iterator(); i.hasNext(); )
        {
            i.next().close();
        }

        if( logger.isDebugEnabled())
        {
            logger.debug("ClientConnectionManager shut down (all connections released)");
        }

        connections.clear();
        receptor_pool.shutdown();
    }
}
