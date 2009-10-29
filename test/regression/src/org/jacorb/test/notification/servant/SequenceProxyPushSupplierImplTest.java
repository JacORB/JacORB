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

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.PushTaskExecutor;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.IAdmin;
import org.jacorb.notification.servant.SequenceProxyPushSupplierImpl;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotification.MaximumBatchSize;
import org.omg.CosNotification.PacingInterval;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyComm.SequencePushConsumer;
import org.omg.TimeBase.TimeTHelper;

import java.util.concurrent.ScheduledFuture;

public class SequenceProxyPushSupplierImplTest extends NotificationTestCase
{
    private SequenceProxyPushSupplierImpl objectUnderTest_;

    private MockControl controlAdmin_;

    private IAdmin mockAdmin_;

    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    private MockControl controlConsumerAdmin_;

    private ConsumerAdmin mockConsumerAdmin_;

    private MockControl controlPushConsumer_;

    private SequencePushConsumer mockPushConsumer_;

    private AbstractMatcher TASKPROCESSOR_MATCHER = new AbstractMatcher()
    {
        public boolean matches(Object[] expected, Object[] actual)
        {
            return expected[0].equals(actual[0]) && expected[2].equals(actual[2]);
        }
    };

    private MockControl controlPushTaskExecutorFactory_;

    private PushTaskExecutorFactory mockPushTaskExecutorFactory_;

    private ScheduledFuture mockScheduledFuture_;

    private MockControl controlScheduledFuture_;

    public SequenceProxyPushSupplierImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    protected void setUpTest() throws Exception
    {
        controlScheduledFuture_ = MockControl.createControl(ScheduledFuture.class);
        mockScheduledFuture_ = (ScheduledFuture) controlScheduledFuture_.getMock();
        
        controlAdmin_ = MockControl.createControl(IAdmin.class);
        mockAdmin_ = (IAdmin) controlAdmin_.getMock();

        controlAdmin_.expectAndReturn(mockAdmin_.getContainer(), null);

        controlAdmin_.expectAndReturn(mockAdmin_.isIDPublic(), true);

        controlAdmin_.expectAndReturn(mockAdmin_.getProxyID(), 1);

        controlAdmin_.expectAndReturn(mockAdmin_.getAdminMBean(), "");
        controlAdmin_.replay();

        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();
        controlConsumerAdmin_ = MockControl.createControl(ConsumerAdmin.class);
        mockConsumerAdmin_ = (ConsumerAdmin) controlConsumerAdmin_.getMock();

        controlPushConsumer_ = MockControl.createControl(SequencePushConsumer.class);
        mockPushConsumer_ = (SequencePushConsumer) controlPushConsumer_.getMock();

        controlPushTaskExecutorFactory_ = MockControl.createControl(PushTaskExecutorFactory.class);
        mockPushTaskExecutorFactory_ = (PushTaskExecutorFactory) controlPushTaskExecutorFactory_
                .getMock();

        mockPushTaskExecutorFactory_.newExecutor(null);
        controlPushTaskExecutorFactory_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlPushTaskExecutorFactory_.setReturnValue(new PushTaskExecutor()
        {
            public void executePush(PushTask task)
            {
                task.doPush();
            }

            public void dispose()
            {
                // ignored
            }
        });

        controlPushTaskExecutorFactory_.replay();

        objectUnderTest_ = new SequenceProxyPushSupplierImpl(mockAdmin_, getORB(), getPOA(),
                getConfiguration(), mockTaskProcessor_, mockPushTaskExecutorFactory_,
                new OfferManager(), new SubscriptionManager(), mockConsumerAdmin_);
    }

    private void verifyAll()
    {
        controlScheduledFuture_.verify();
        controlPushTaskExecutorFactory_.verify();
        controlAdmin_.verify();
        controlTaskProcessor_.verify();
        controlConsumerAdmin_.verify();
        controlPushConsumer_.verify();
    }

    private void replayAll()
    {
        controlScheduledFuture_.replay();
        controlTaskProcessor_.replay();
        controlConsumerAdmin_.replay();
        controlPushConsumer_.replay();
    }

    public void testCreation()
    {
        replayAll();

        assertEquals(1, objectUnderTest_.getID().intValue());
        assertEquals(mockConsumerAdmin_, objectUnderTest_.MyAdmin());
        assertEquals(ProxyType.PUSH_SEQUENCE, objectUnderTest_.MyType());

        verifyAll();
    }

    public void testDefaultConfigurationDoesNotStartFlushThread() throws Exception
    {
        replayAll();

        objectUnderTest_.connect_sequence_push_consumer(mockPushConsumer_);

        verifyAll();
    }

    public void testSetQoSDoesNotStartThread() throws Exception
    {
        replayAll();

        final int interval = 1000;

        Property prop = newPacingInterval(interval);

        objectUnderTest_.set_qos(new Property[] { prop });

        verifyAll();
    }

    public void testSetQoSBeforeConnect() throws Exception
    {
        final int interval = 10000000;

        mockTaskProcessor_.executeTaskPeriodically(1000, null, true);
        controlTaskProcessor_.setMatcher(TASKPROCESSOR_MATCHER);
        controlTaskProcessor_.setReturnValue(null);

        replayAll();

        Property prop = newPacingInterval(interval);

        objectUnderTest_.set_qos(new Property[] { prop });
        objectUnderTest_.connect_sequence_push_consumer(mockPushConsumer_);

        verifyAll();
    }

    public void testSetQoSAfterConnect() throws Exception
    {
        final int interval = 10000000;

        mockTaskProcessor_.executeTaskPeriodically(1000, null, true);
        controlTaskProcessor_.setMatcher(TASKPROCESSOR_MATCHER);
        controlTaskProcessor_.setReturnValue(null);

        replayAll();

        Property prop = newPacingInterval(interval);

        objectUnderTest_.connect_sequence_push_consumer(mockPushConsumer_);
        objectUnderTest_.set_qos(new Property[] { prop });

        verifyAll();
    }

    public void testSetQoSTwoTimes() throws Exception
    {
        final int interval1 = 10000000;
        final int interval2 = 20000000;

        mockTaskProcessor_.executeTaskPeriodically(1000, null, true);
        controlTaskProcessor_.setMatcher(TASKPROCESSOR_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        mockScheduledFuture_.cancel(true);
        controlScheduledFuture_.setReturnValue(true);
        
        mockTaskProcessor_.executeTaskPeriodically(2000, null, true);
        controlTaskProcessor_.setReturnValue(null);

        replayAll();

        Property prop1 = newPacingInterval(interval1);
        Property prop2 = newPacingInterval(interval2);

        objectUnderTest_.connect_sequence_push_consumer(mockPushConsumer_);
        objectUnderTest_.set_qos(new Property[] { prop1 });
        objectUnderTest_.set_qos(new Property[] { prop2 });

        verifyAll();
    }

    public void testMaximumBatchSize() throws Exception
    {
        StructuredEvent event = new StructuredEvent();

        MockControl controlMessage = MockControl.createNiceControl(Message.class);
        Message mockMessage = (Message) controlMessage.getMock();

        controlMessage.expectAndReturn(mockMessage.clone(), mockMessage, MockControl.ONE_OR_MORE);
        
        mockMessage.toStructuredEvent();
        controlMessage.setReturnValue(event);

        controlMessage.replay();

        replayAll();

        Any any = getORB().create_any();
        any.insert_long(3);
        objectUnderTest_.set_qos(new Property[] { new Property(MaximumBatchSize.value, any) });
        objectUnderTest_.connect_sequence_push_consumer(mockPushConsumer_);

        objectUnderTest_.getMessageConsumer().queueMessage(mockMessage);
        objectUnderTest_.getMessageConsumer().queueMessage(mockMessage);

        controlPushConsumer_.verify();

        controlPushConsumer_.reset();

        mockPushConsumer_.push_structured_events(new StructuredEvent[] { event, event });
        controlPushConsumer_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                return ((StructuredEvent[]) expected[0]).length == ((StructuredEvent[]) actual[0]).length;
            }
        });

        controlPushConsumer_.replay();
        objectUnderTest_.flushPendingEvents();

        verifyAll();
    }

    private Property newPacingInterval(final int timeT)
    {
        Any any = getORB().create_any();
        TimeTHelper.insert(any, timeT);

        Property prop = new Property(PacingInterval.value, any);
        return prop;
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(SequenceProxyPushSupplierImplTest.class);
    }
}
