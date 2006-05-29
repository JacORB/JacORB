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
import org.apache.avalon.framework.configuration.Configuration;

import org.jacorb.util.threadpool.*;

/**
 * MessageReceptorPool.java
 *
 * @author Nicolas Noffke
 * @version $Id$
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
     * TODO configuration of this class should be enhanced. config param does not feel nice.
     */
    public MessageReceptorPool(String config, String threadNamePrefix, Configuration myConfiguration)
    {
        if (!config.equals("server") || !config.equals("client"))
        {
            throw new IllegalArgumentException("must be client or server");
        }

        org.jacorb.config.Configuration configuration =
            (org.jacorb.config.Configuration) myConfiguration;

        final int maxConnectionThreads =
            configuration.getAttributeAsInteger("jacorb.connection." + config + ".max_threads", MAX_DEFAULT);

        final int maxIdleThreads = configuration.getAttributeAsInteger("jacorb.connection." + config + ".max_idle_receptor_threads", MAX_IDLE_DEFAULT);

        Logger logger = configuration.getNamedLogger("jacorb.orb.giop");

        if (logger.isDebugEnabled())
        {
            logger.debug("Maximum connection threads: " + maxConnectionThreads);
            logger.debug("Maximum idle threads: " + maxIdleThreads);
        }

        pool =
            new ThreadPool( threadNamePrefix,
                               new ConsumerFactory(){
                                    public Consumer create()
                                    {
                                        return new MessageReceptor();
                                    }
                                },
                                maxConnectionThreads,
                                maxIdleThreads ); //max idle threads
    }

    public void connectionCreated( GIOPConnection conn )
    {
        pool.putJob( conn );
    }
}// MessageReceptorPool
