/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.util.threadpool.Consumer;
import org.jacorb.util.threadpool.ConsumerFactory;
import org.jacorb.util.threadpool.ThreadPool;
import org.slf4j.Logger;

/**
 * @author Nicolas Noffke
 */
public class MessageReceptorPool
{
    private static final int MAX_DEFAULT = 1000;
    private static final int MAX_IDLE_DEFAULT = 5;

    private final ThreadPool pool;

    /**
     * @param config must be client or server to specify which configurationsubset should be used
     * @param threadNamePrefix prefix that's used to name all threads that are created by this pool
     * @param myConfiguration current configuration
     * TODO configuration of this class should be enhanced. config param does not feel right.
    * @throws ConfigurationException
     */
    public MessageReceptorPool(String config, String threadNamePrefix, Configuration myConfiguration) throws ConfigurationException
    {
        if (!"server".equals(config) && !"client".equals(config))
        {
            throw new IllegalArgumentException("must be client or server");
        }

        final org.jacorb.config.Configuration configuration =
            (org.jacorb.config.Configuration) myConfiguration;

        final int maxConnectionThreads =
            configuration.getAttributeAsInteger("jacorb.connection." + config + ".max_receptor_threads", MAX_DEFAULT);

        final int maxIdleThreads = configuration.getAttributeAsInteger("jacorb.connection." + config + ".max_idle_receptor_threads", MAX_IDLE_DEFAULT);

        Logger logger = configuration.getLogger("jacorb.orb.giop");

        if (logger.isDebugEnabled())
        {
            logger.debug("Maximum connection threads: " + maxConnectionThreads);
            logger.debug("Maximum idle threads: " + maxIdleThreads);
        }

        pool =
            new ThreadPool( configuration,
                            threadNamePrefix,
                            new ConsumerFactory(){
                                public Consumer create()
                                {
                                    return new MessageReceptor(configuration);
                                }
                            },
                            maxConnectionThreads,
                            maxIdleThreads ); //max idle threads
    }

    public void connectionCreated( GIOPConnection conn )
    {
        pool.putJob( conn );
    }

    /**
     * <code>shutdown</code> allows the ReceptorPool to be shutdown
     * so that new ORB.init's will use fresh pools
     */
    public void shutdown()
    {
        pool.shutdown();
    }
}
