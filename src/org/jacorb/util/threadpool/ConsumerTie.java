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


package org.jacorb.util.threadpool;

/**
 * ConsumerTie.java
 *
 *
 * Created: Fri Jun  9 15:44:26 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

import org.jacorb.util.Debug;

public  class ConsumerTie
    implements Runnable
{
    private boolean run = true;
    private ThreadPool pool = null;
    private Consumer delegate = null;

    public ConsumerTie( ThreadPool pool,
                        Consumer delegate )
    {
        this.pool = pool;
        this.delegate = delegate;
    }
	
    public void run()
    {
        try
        {
            while( run )
            {
                Object job = pool.getJob();
      
                if( job == null )
                {
                    /*
                     * job == null is sent by the pool, if there are
                     * too much idle threads. Therefore we exit.
                     */
                    return;
                }
                else
                {
                    delegate.doWork( job );
                }
            }
        }
        catch( Exception e )
        {
            Debug.output( Debug.IMPORTANT | Debug.TOOLS, e );
            return;
        }
    }
} // ConsumerTie



