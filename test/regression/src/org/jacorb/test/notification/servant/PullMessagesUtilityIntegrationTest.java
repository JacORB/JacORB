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

package org.jacorb.test.notification.servant;

import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.interfaces.MessageSupplier;
import org.jacorb.notification.servant.PullMessagesUtility;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

public class PullMessagesUtilityIntegrationTest extends NotificationTestCase
{
    private MockControl controlMessageSupplier_;
    private MessageSupplier mockMessageSupplier_;
    private PullMessagesUtility objectUnderTest_;
    private DefaultTaskProcessor taskProcessor_;

    public PullMessagesUtilityIntegrationTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    protected void setUpTest() throws Exception
    {
        controlMessageSupplier_ = MockControl.createControl(MessageSupplier.class);
        mockMessageSupplier_ = (MessageSupplier) controlMessageSupplier_.getMock();
        taskProcessor_ = new DefaultTaskProcessor(getConfiguration());
        objectUnderTest_ = new PullMessagesUtility(taskProcessor_ , mockMessageSupplier_);    
    }
    
    protected void tearDownTest() throws Exception
    {
        taskProcessor_.dispose();
    }
    
    public void testStartTask() throws Exception
    {
        mockMessageSupplier_.runPullMessage();
        
        controlMessageSupplier_.replay();
        objectUnderTest_.startTask(4000);
        
        Thread.sleep(2000);
        
        objectUnderTest_.stopTask();
        
        controlMessageSupplier_.verify();
    }
    
    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(PullMessagesUtilityIntegrationTest.class);
    }
}
