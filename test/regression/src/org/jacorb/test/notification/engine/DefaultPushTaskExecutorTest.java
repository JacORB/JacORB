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

package org.jacorb.test.notification.engine;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.jacorb.notification.engine.DefaultPushTaskExecutor;
import org.jacorb.notification.engine.PushTaskExecutor;

public class DefaultPushTaskExecutorTest extends TestCase
{
    private DefaultPushTaskExecutor objectUnderTest_;

    public DefaultPushTaskExecutorTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        objectUnderTest_ = new DefaultPushTaskExecutor(2);
    }

    public void testExecutePush()
    {
        MockControl controlPushTask = MockControl.createControl(PushTaskExecutor.PushTask.class);
        PushTaskExecutor.PushTask mockPushTask = (PushTaskExecutor.PushTask) controlPushTask
                .getMock();

        mockPushTask.doPush();

        controlPushTask.replay();

        objectUnderTest_.executePush(mockPushTask);

        controlPushTask.verify();
    }
}
