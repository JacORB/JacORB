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
package org.jacorb.orb.giop;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import java.util.*;

import org.jacorb.util.ObjectUtil;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class GIOPConnectionManager 
    implements Configurable
{
    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;

    /** configuration properties */
    private Logger logger = null;

    //private List client_giop_connections = null;
    private List server_giop_connections = null;
    private int max_server_giop_connections = 0;
    private Class statistics_provider_class = null;
    private SelectionStrategy selection_strategy = null;
    private int wait_for_idle_interval = 0;



    public GIOPConnectionManager()
    {
        server_giop_connections = new LinkedList();
    }

    /**
     * configures the GIOPConnectionManager
     */

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = 
            configuration.getNamedLogger("jacorb.orb.giop.conn");
        
        max_server_giop_connections = 
            configuration.getAttributeAsInteger("jacorb.connection.max_server_connections",
                                                Integer.MAX_VALUE );
        
        selection_strategy = (SelectionStrategy)
            configuration.getAttributeAsObject( 
                "jacorb.connection.selection_strategy_class" );
        
        wait_for_idle_interval =
            configuration.getAttributeAsInteger( 
                "jacorb.connection.wait_for_idle_interval", 500 );                

        String s = 
            configuration.getAttribute( "jacorb.connection.statistics_provider_class","" );

        if( s.length() > 0 )
        {
            try
            {
                statistics_provider_class = ObjectUtil.classForName( s );
            }
            catch( Exception e )
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Unable to create class from property >jacorb.connection.statistics_provider_class<: " + e.getMessage() );
                }                        
            }
        }

    }
    

    public ServerGIOPConnection createServerGIOPConnection(org.omg.ETF.Profile profile,
                                                           org.omg.ETF.Connection transport,
                                                           RequestListener request_listener,
                                                           ReplyListener reply_listener )
    {
        //if too many open connections, shut one down
        if( server_giop_connections.size() >= max_server_giop_connections )
        {
            if( selection_strategy != null )
            {
                while( server_giop_connections.size() >= max_server_giop_connections )
                {
                    ServerGIOPConnection to_close = null;

                    synchronized( server_giop_connections )
                    {
                        to_close = 
                            selection_strategy.selectForClose( server_giop_connections );
                    }
                    
                    if( to_close != null &&
                        to_close.tryClose() )
                    {
                        break;
                    }
                    else
                    {
                        try
                        {
                            Thread.sleep( wait_for_idle_interval );
                        }
                        catch( Exception e )
                        {
                            if (logger.isWarnEnabled())
                            {
                                logger.warn("During thread.sleep: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            else
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "No. of max server giop connections set, but no SelectionStrategy present" );
                }
            }
        }

        ServerGIOPConnection connection =
            new ServerGIOPConnection( profile,
                                      transport,
                                      request_listener,
                                      reply_listener,
                                      getStatisticsProvider(),
                                      this);

        try
        {
            connection.configure( configuration );
        }
        catch( ConfigurationException ce )
        {
            if (logger.isWarnEnabled())
                logger.warn("ConfigurationException", ce);
        }

        synchronized( server_giop_connections )
        {
            server_giop_connections.add( connection );
        }
        
        return connection;
    }

    public void unregisterServerGIOPConnection( 
        ServerGIOPConnection connection )
    {
        synchronized( server_giop_connections )
        {
            server_giop_connections.remove( connection );
        }
    } 

    public GIOPConnection createClientGIOPConnection( 
        org.omg.ETF.Profile profile,
        org.omg.ETF.Connection transport,
        RequestListener request_listener,
        ReplyListener reply_listener )
    {
        ClientGIOPConnection connection = 
            new ClientGIOPConnection( profile,
                                      transport,
                                      request_listener,
                                      reply_listener,
                                      null );
        
        try
        {
            connection.configure( configuration );
        }
        catch( ConfigurationException ce )
        {
            if (logger.isWarnEnabled())
                logger.warn("ConfigurationException", ce);
        }
        return connection;
    }

    private StatisticsProvider getStatisticsProvider()
    {
        StatisticsProvider result = null;
        if( statistics_provider_class != null )
        {
            try
            {
                result = (StatisticsProvider) 
                    statistics_provider_class.newInstance();
            }
            catch( Exception e )
            {
                if (logger.isErrorEnabled())
                    logger.error( "Unable to create instance from Class >" +
                                  statistics_provider_class + '<');
            }
        }
        return result;       
    }

}// GIOPConnectionManager



