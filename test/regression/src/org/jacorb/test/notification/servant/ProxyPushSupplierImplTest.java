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
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.DirectExecutorPushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IAdmin;
import org.jacorb.notification.servant.ProxyPushSupplierImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyComm.PushConsumer;

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledFuture;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class ProxyPushSupplierImplTest extends NotificationTestCase
{
    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    private ProxyPushSupplierImpl objectUnderTest_;

    private MockControl controlScheduledFuture_;

    private ScheduledFuture mockScheduledFuture_;

    public ProxyPushSupplierImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        MockControl controlAdmin = MockControl.createControl(IAdmin.class);
        IAdmin mockAdmin = (IAdmin) controlAdmin.getMock();

        mockAdmin.isIDPublic();
        controlAdmin.setReturnValue(true);

        mockAdmin.getProxyID();
        controlAdmin.setReturnValue(10);

        mockAdmin.getContainer();
        controlAdmin.setReturnValue(null);

        mockAdmin.getAdminMBean();
        controlAdmin.setReturnValue("");
        
        controlAdmin.replay();

        MockControl controlConsumerAdmin = MockControl.createControl(ConsumerAdmin.class);
        ConsumerAdmin mockConsumerAdmin = (ConsumerAdmin) controlConsumerAdmin.getMock();

        controlConsumerAdmin.replay();

        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
       
        objectUnderTest_ = new ProxyPushSupplierImpl(mockAdmin, getORB(), getPOA(),
                getConfiguration(), mockTaskProcessor_, new DirectExecutorPushTaskExecutorFactory(), new OfferManager(),
                new SubscriptionManager(), mockConsumerAdmin);

        assertEquals(new Integer(10), objectUnderTest_.getID());
        
        controlScheduledFuture_ = MockControl.createControl(ScheduledFuture.class);
        mockScheduledFuture_ = (ScheduledFuture) controlScheduledFuture_.getMock();
    }

    public void testDeliverMessage_Error() throws Exception
    {
        Any any = getORB().create_any();

        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        mockMessage.toAny();
        controlMessage.setReturnValue(any);

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage, 2);

        mockMessage.dispose();

        controlMessage.replay();

        MockControl controlPushConsumer = MockControl.createControl(PushConsumer.class);
        PushConsumer mockPushConsumer = (PushConsumer) controlPushConsumer.getMock();

        mockPushConsumer.push(any);
        controlPushConsumer.setThrowable(new RuntimeException());

        controlPushConsumer.replay();

        mockTaskProcessor_.executeTaskAfterDelay(0, null);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        replayAll();

        objectUnderTest_.connect_any_push_consumer(mockPushConsumer);
        objectUnderTest_.queueMessage(mockMessage);
        
        verifyAll();
    }

    private void replayAll()
    {
        controlScheduledFuture_.replay();
        controlTaskProcessor_.replay();
    }
    
    private void verifyAll()
    {
        controlScheduledFuture_.verify();
        controlTaskProcessor_.verify();
    }
    
    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(ProxyPushSupplierImplTest.class);
    }
}