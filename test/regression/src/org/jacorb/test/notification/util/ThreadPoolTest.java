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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.engine.DefaultTaskExecutor;

/**
 * @author Alphonse Bendt
 */
public class ThreadPoolTest extends TestCase
{
    private DefaultTaskExecutor objectUnderTest_;

    public void setUp() throws Exception
    {
        objectUnderTest_ = new DefaultTaskExecutor("Testing", 2);
    }

    public void tearDown() throws Exception
    {
        objectUnderTest_.dispose();
    }

    public void testExceute() throws Exception
    {
        MockControl controlTask = MockControl.createControl(Runnable.class);
        Runnable mockTask = (Runnable) controlTask.getMock();

        mockTask.run();
        controlTask.replay();

        objectUnderTest_.execute(mockTask);

        Thread.sleep(100);

        controlTask.verify();
    }

    public void testDirectExceute() throws Exception
    {
        objectUnderTest_.dispose();
        objectUnderTest_ = new DefaultTaskExecutor("Testing", 0);

        MockControl controlTask = MockControl.createControl(Runnable.class);
        Runnable mockTask = (Runnable) controlTask.getMock();

        mockTask.run();
        controlTask.replay();

        objectUnderTest_.execute(mockTask);

        controlTask.verify();
    }

    public void testNegativeNumberOfThreadsIsInvalid() throws Exception
    {
        try
        {
            new DefaultTaskExecutor("Testing", -1);
            fail();
        } catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public ThreadPoolTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(ThreadPoolTest.class, "Tests for Class ThreadPool");

        return suite;
    }
}