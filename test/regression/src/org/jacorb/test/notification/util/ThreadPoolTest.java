package org.jacorb.test.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.notification.engine.TaskExecutor;

import java.util.HashSet;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 */

public class ThreadPoolTest extends TestCase
{
    TaskExecutor threadPool_;

    public void setUp() throws Exception
    {
        threadPool_ = new TaskExecutor("Testing", 2);
    }

    public void tearDown() throws Exception
    {
        threadPool_.dispose();
    }

    public void testExceute() throws Exception
    {
        final HashSet s = new HashSet();

        threadPool_.execute(new Runnable()
                            {
                                public void run()
                                {
                                    s.add("passed");
                                }
                            }
                           );

        Thread.sleep(100);

        assertTrue(s.contains("passed"));
    }

    public void testDirectExceute() throws Exception
    {
        threadPool_.dispose();
        threadPool_ = new TaskExecutor("Testing", 0);

        final HashSet s = new HashSet();

        threadPool_.execute(new Runnable()
                            {
                                public void run()
                                {
                                    s.add("passed");
                                }
                            }
                           );

        assertTrue(s.contains("passed"));
    }

    public void testNegativeNumberOfThreadsIsInvalid() throws Exception
    {
        try
        {
            new TaskExecutor("Testing", -1);
            fail();
        }
        catch (IllegalArgumentException e)
        {}
    }

    public void testIsTaskQueued() throws Exception
    {
        assertTrue(!threadPool_.isTaskQueued());

        Latch _l1 = new Latch();
        Latch _l2 = new Latch();

        BlockingRunnable r1, r2, r3;

        threadPool_.execute(r1 = new BlockingRunnable(_l1));
        threadPool_.execute(r2 = new BlockingRunnable(_l2));
        threadPool_.execute(r3 = new BlockingRunnable(_l2));

        assertTrue(threadPool_.isTaskQueued());
        _l1.release();
        Thread.sleep(100);
        assertTrue(!threadPool_.isTaskQueued());
    }


    public ThreadPoolTest (String name)
    {
        super(name);
    }


    public static Test suite()
    {
        TestSuite suite =
            new TestSuite(ThreadPoolTest.class, "Tests for Class ThreadPool");

        return suite;
    }
}


class BlockingRunnable implements Runnable
{
    private final Latch signal_;

    BlockingRunnable(Latch l)
    {
        signal_ = l;
    }

    public void run()
    {
        try
        {
            signal_.acquire();
        }
        catch (InterruptedException e)
        {}
    }
}
