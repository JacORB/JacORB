/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

package org.jacorb.orb.connection;

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
    private static MessageReceptorPool singleton = null;
    private ThreadPool pool = null;

    private MessageReceptorPool()
    {
        pool = new ThreadPool( new ConsumerFactory(){
                public Consumer create()
                {
                    return new MessageReceptor();
                }
            },
                               1000, //maximum number of connections
                               5 ); //max idle threads
    }

    public static synchronized MessageReceptorPool getInstance()
    {
        if( singleton == null )
        {
            singleton = new MessageReceptorPool();
        }

        return singleton;
    }

    public void connectionCreated( GIOPConnection conn )
    {
        pool.putJob( conn );
    }
}// MessageReceptorPool
