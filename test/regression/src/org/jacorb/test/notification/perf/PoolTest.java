/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.test.notification.perf;

import junit.framework.TestCase;

import org.jacorb.notification.AnyMessage;
import org.jacorb.notification.StructuredEventMessage;
import org.jacorb.notification.util.AbstractPoolablePool;

public class PoolTest extends TestCase
{
    private class Work implements Runnable
    {
        long time = -1;

        public void run()
        {
            long now = System.currentTimeMillis();

            for (int i = 0; i < 1000; ++i)
            {
                AnyMessage m = createMessage();
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e)
                {
                    // ignored
                }
                m.reset();
            }

            synchronized (this)
            {
                time = System.currentTimeMillis() - now;
                notifyAll();
            }
        }

        public AnyMessage createMessage()
        {
            return new AnyMessage();
        }

        public long getResult()
        {
            synchronized (this)
            {
                while (time == -1)
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        // ignored
                    }
                }

                return time;
            }
        }
    }

    public void testWithPool()
    {
        final AbstractPoolablePool pool =
            new AbstractPoolablePool("test")
            {
                public Object newInstance()
                {
                    return new AnyMessage();
                }
            };

            pool.configure(null);

        Work[] worker = new Work[4];

        for (int i = 0; i < worker.length; ++i)
        {
            worker[i] = new Work()
            {
                public AnyMessage createMessage()
                {
                    return (AnyMessage) pool.lendObject();
                }
            };
            new Thread(worker[i]).start();
        }

        long sum = 0;
        for (int i = 0; i < worker.length; ++i)
        {
            sum += worker[i].getResult();
        }

        System.out.println("pool: " + sum);
    }

    public void testNoPool()
    {
        Work[] worker = new Work[4];

        for (int i = 0; i < worker.length; ++i)
        {
            worker[i] = new Work();
            new Thread(worker[i]).start();
        }

        long sum = 0;
        for (int i = 0; i < worker.length; ++i)
        {
            sum += worker[i].getResult();
        }

        System.out.println("raw: " + sum);
    }
}
