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

import org.apache.avalon.framework.configuration.Configuration;
import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.DefaultPushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskExecutor;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IAdmin;
import org.jacorb.notification.servant.StructuredProxyPushSupplierImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushConsumer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class StructuredProxyPushSupplierImplTest extends NotificationTestCase
{
    private StructuredProxyPushSupplierImpl objectUnderTest_;

    private MockControl controlTaskExecutor_;

    private TaskExecutor mockTaskExecutor_;

    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUpTest() throws Exception
    {
        MockControl controlAdmin = MockControl.createControl(IAdmin.class);
        IAdmin mockAdmin = (IAdmin) controlAdmin.getMock();

        mockAdmin.isIDPublic();
        controlAdmin.setReturnValue(true);

        mockAdmin.getProxyID();
        controlAdmin.setReturnValue(10);

        mockAdmin.getContainer();
        controlAdmin.setReturnValue(null);

        controlAdmin.replay();

        MockControl controlConfig = MockControl.createControl(Configuration.class);
        Configuration mockConfig = (Configuration) controlConfig.getMock();

        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
        controlTaskExecutor_ = MockControl.createControl(TaskExecutor.class);
        mockTaskExecutor_ = (TaskExecutor) controlTaskExecutor_.getMock();
        objectUnderTest_ = new StructuredProxyPushSupplierImpl(mockAdmin, getORB(), getPOA(),
                getConfiguration(), mockTaskProcessor_, new DefaultPushTaskExecutorFactory(1), new OfferManager(),
                new SubscriptionManager(), null);

        assertEquals(new Integer(10), objectUnderTest_.getID());
    }

    /**
     * Constructor for StructuredProxyPushSupplierImplTest.
     * 
     * @param name
     */
    public StructuredProxyPushSupplierImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void testDeliveryToNotConnectedDoesNotAccessMessage()
    {
        MockControl controlMessage = MockControl.createStrictControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        controlMessage.replay();

        controlTaskExecutor_.replay();

        controlTaskProcessor_.replay();

        objectUnderTest_.deliverMessage(mockMessage);

        controlMessage.verify();

        controlTaskExecutor_.verify();

        controlTaskProcessor_.verify();
    }

    public void testDeliveryToDisabledConsumerEnqueues() throws Exception
    {
        MockControl controlMessage = MockControl.createStrictControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage);

        controlMessage.replay();

        controlTaskExecutor_.replay();

        controlTaskProcessor_.replay();

        MockControl controlStructuredPushConsumer = MockControl
                .createControl(StructuredPushConsumer.class);
        StructuredPushConsumer mockStructuredPushConsumer = (StructuredPushConsumer) controlStructuredPushConsumer
                .getMock();

        objectUnderTest_.connect_structured_push_consumer(mockStructuredPushConsumer);

        objectUnderTest_.disableDelivery();

        objectUnderTest_.deliverMessage(mockMessage);

        controlMessage.verify();

        controlTaskExecutor_.verify();

        controlTaskProcessor_.verify();
    }

    public void testDeliveryToConsumerDoesEnqueueAndDisposeMessage() throws Exception
    {
        StructuredEvent event = new StructuredEvent();

        MockControl controlMessage = MockControl.createStrictControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage);

        mockMessage.toStructuredEvent();
        controlMessage.setReturnValue(event);

        mockMessage.dispose();

        controlMessage.replay();

        controlTaskExecutor_.replay();

        MockControl controlStructuredPushConsumer = MockControl
                .createControl(StructuredPushConsumer.class);
        StructuredPushConsumer mockStructuredPushConsumer = (StructuredPushConsumer) controlStructuredPushConsumer
                .getMock();

        mockStructuredPushConsumer.push_structured_event(event);

        controlStructuredPushConsumer.replay();

        objectUnderTest_.connect_structured_push_consumer(mockStructuredPushConsumer);

        objectUnderTest_.deliverMessage(mockMessage);

        objectUnderTest_.pushPendingData();

        controlMessage.verify();

        controlTaskExecutor_.verify();
    }

    public void testFailedDeliveryToConsumerDoesNotDisposeMessage() throws Exception
    {
        StructuredEvent event = new StructuredEvent();

        MockControl controlMessage = MockControl.createControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        mockMessage.clone();
        controlMessage.setReturnValue(mockMessage, 2);

        mockMessage.toStructuredEvent();
        controlMessage.setReturnValue(event);

        mockMessage.dispose();

        controlMessage.replay();

        controlTaskExecutor_.replay();

        mockTaskProcessor_.executeTaskAfterDelay(0, null);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(null);

        controlTaskProcessor_.replay();

        MockControl controlStructuredPushConsumer = MockControl
                .createControl(StructuredPushConsumer.class);
        StructuredPushConsumer mockStructuredPushConsumer = (StructuredPushConsumer) controlStructuredPushConsumer
                .getMock();

        mockStructuredPushConsumer.push_structured_event(event);
        controlStructuredPushConsumer.setThrowable(new TRANSIENT());

        controlStructuredPushConsumer.replay();

        objectUnderTest_.connect_structured_push_consumer(mockStructuredPushConsumer);

        objectUnderTest_.deliverMessage(mockMessage);

        objectUnderTest_.pushPendingData();

        controlMessage.verify();

        controlTaskExecutor_.verify();

        controlTaskProcessor_.verify();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(StructuredProxyPushSupplierImplTest.class);
    }
}