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
 *
 * Created: Sat Aug 18 10:40:25 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class MessageReceptorPool
{
    private static final int MAX_DEFAULT = 1000;

    private static MessageReceptorPool singleton = null;

    private int maxConnectionThreads = 1000;
    private ThreadPool pool = null;

    private MessageReceptorPool(Configuration myConfiguration)
    {
        org.jacorb.config.Configuration configuration =
            (org.jacorb.config.Configuration) myConfiguration;

        int maxConnectionThreads = MAX_DEFAULT;

        final String attribute = "jacorb.connection.max_threads";

        maxConnectionThreads =
            configuration.getAttributeAsInteger(attribute, MAX_DEFAULT);

        Logger logger = configuration.getNamedLogger("jacorb.orb.giop");

        if (logger.isDebugEnabled())
        {
            logger.debug("Maximum connection threads: " + maxConnectionThreads);
        }

        pool = 
            new ThreadPool( new ConsumerFactory(){
                    public Consumer create()
                    {
                        return new MessageReceptor();
                    }
                },
                               maxConnectionThreads, //maximum number of connections
                               5 ); //max idle threads
    }

    public static synchronized MessageReceptorPool getInstance(Configuration myConfiguration)
    {
        if( singleton == null )
        {
            singleton = new MessageReceptorPool(myConfiguration);
        }

        return singleton;
    }

    public void connectionCreated( GIOPConnection conn )
    {
        pool.putJob( conn );
    }
}// MessageReceptorPool
