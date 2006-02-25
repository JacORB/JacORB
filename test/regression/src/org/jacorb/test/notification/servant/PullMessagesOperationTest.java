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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.servant.MessageSupplierDelegate;
import org.jacorb.notification.servant.PullMessagesOperation;
import org.omg.CosEventComm.Disconnected;

public class PullMessagesOperationTest extends TestCase
{
    private PullMessagesOperation objectUnderTest_;

    private MockControl controlMessageSupplier_;

    private MessageSupplierDelegate mockMessageSupplier_;

    protected void setUp() throws Exception
    {
        super.setUp();

        controlMessageSupplier_ = MockControl.createControl(MessageSupplierDelegate.class);
        mockMessageSupplier_ = (MessageSupplierDelegate) controlMessageSupplier_.getMock();
        objectUnderTest_ = new PullMessagesOperation(mockMessageSupplier_);
    }

    public void testNotConnectedDoesNothing() throws Exception
    {
        mockMessageSupplier_.getConnected();
        controlMessageSupplier_.setReturnValue(false);
        controlMessageSupplier_.replay();
        try
        {
            objectUnderTest_.runPull();
        } catch (Disconnected e)
        {
            // expected
        }
        controlMessageSupplier_.verify();
    }

    public void testSuspendedDoesNothing() throws Exception
    {
        mockMessageSupplier_.getConnected();
        controlMessageSupplier_.setReturnValue(true);
        mockMessageSupplier_.isSuspended();
        controlMessageSupplier_.setReturnValue(true);
        controlMessageSupplier_.replay();
        objectUnderTest_.runPull();
        controlMessageSupplier_.verify();
    }
    
    public void testSuccessfulPullCausesQueue() throws Exception
    {
        MessageSupplierDelegate.PullResult _result =
            new MessageSupplierDelegate.PullResult(new Object(), true);
        
        mockMessageSupplier_.getConnected();
        controlMessageSupplier_.setReturnValue(true);
        mockMessageSupplier_.isSuspended();
        controlMessageSupplier_.setReturnValue(false);
        
        mockMessageSupplier_.pullMessages();
        controlMessageSupplier_.setReturnValue(_result);
        
        mockMessageSupplier_.queueMessages(_result);
        
        controlMessageSupplier_.replay();
        objectUnderTest_.runPull();
        controlMessageSupplier_.verify();
    }

    public void testNonSuccessfulPullIsNotQueued() throws Exception
    {
        MessageSupplierDelegate.PullResult _result =
            new MessageSupplierDelegate.PullResult(new Object(), false);
        
        mockMessageSupplier_.getConnected();
        controlMessageSupplier_.setReturnValue(true);
        mockMessageSupplier_.isSuspended();
        controlMessageSupplier_.setReturnValue(false);
        
        mockMessageSupplier_.pullMessages();
        controlMessageSupplier_.setReturnValue(_result);
        
        controlMessageSupplier_.replay();
        objectUnderTest_.runPull();
        controlMessageSupplier_.verify();
    }
    
    public void testInterruptedThreadDoesNothing() throws Exception
    {
        mockMessageSupplier_.getConnected();
        controlMessageSupplier_.setReturnValue(true);
        mockMessageSupplier_.isSuspended();
        controlMessageSupplier_.setReturnValue(false);
        
        controlMessageSupplier_.replay();
        
        Thread.currentThread().interrupt();
        objectUnderTest_.runPull();
        
        controlMessageSupplier_.verify();
    }
    
    public static Test suite()
    {
        return new TestSuite(PullMessagesOperationTest.class);
    }
}
