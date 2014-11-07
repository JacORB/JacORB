package org.jacorb.test.notification.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.fail;
import org.easymock.MockControl;
import org.jacorb.notification.engine.DefaultTaskExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class ThreadPoolTest
{
    private DefaultTaskExecutor objectUnderTest_;

    @Before
    public void setUp() throws Exception
    {
        objectUnderTest_ = new DefaultTaskExecutor("Testing", 2);
    }

    @After
    public void tearDown() throws Exception
    {
        objectUnderTest_.dispose();
    }

    @Test
    public void testExceute() throws Exception
    {
        MockControl controlTask = MockControl.createControl(Runnable.class);
        Runnable mockTask = (Runnable) controlTask.getMock();

        mockTask.run();
        controlTask.replay();

        objectUnderTest_.execute(mockTask);

        Thread.sleep(500);

        controlTask.verify();
    }

    @Test
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

    @Test
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

}