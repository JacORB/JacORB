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
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.AbstractProxySupplier;
import org.jacorb.notification.servant.IAdmin;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Object;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class AbstractProxySupplierTest extends NotificationTestCase
{
    private AbstractProxySupplier objectUnderTest_;

    private MockControl controlMessage_;

    private Message mockMessage_;

    private MockControl controlClient_;

    private org.omg.CORBA.Object mockClient_;

    private MockControl controlPOA_;

    private POA mockPOA_;

    protected void setUpTest() throws Exception
    {
        MockControl controlIAdmin = MockControl.createControl(IAdmin.class);
        IAdmin mockIAdmin = (IAdmin) controlIAdmin.getMock();

        mockIAdmin.getProxyID();
        controlIAdmin.setReturnValue(0);

        mockIAdmin.isIDPublic();
        controlIAdmin.setReturnValue(false);

        mockIAdmin.getContainer();
        controlIAdmin.setReturnValue(null);

        mockIAdmin.getAdminMBean();
        controlIAdmin.setReturnValue("admin");

        controlIAdmin.replay();

        MockControl controlConsumerAdmin = MockControl.createControl(ConsumerAdmin.class);
        ConsumerAdmin mockConsumerAdmin = (ConsumerAdmin) controlConsumerAdmin.getMock();

        controlPOA_ = MockControl.createNiceControl(POA.class);
        mockPOA_ = (POA) controlPOA_.getMock();
        objectUnderTest_ = new AbstractProxySupplier(mockIAdmin, getORB(), mockPOA_,
                getConfiguration(), getTaskProcessor(), new OfferManager(),
                new SubscriptionManager(), mockConsumerAdmin)
        {
            protected long getCost()
            {
                return 0;
            }

            public ProxyType MyType()
            {
                return ProxyType.PULL_ANY;
            }

            protected void disconnectClient()
            {
                // ignored
            }

            public Servant newServant()
            {
                return null;
            }
        };

        controlMessage_ = MockControl.createControl(Message.class);
        mockMessage_ = (Message) controlMessage_.getMock();

        controlClient_ = MockControl.createNiceControl(org.omg.CORBA.Object.class);
        mockClient_ = (org.omg.CORBA.Object) controlClient_.getMock();
    }

    public AbstractProxySupplierTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void testNotConnectedSupplierDoesNotAccessMessage()
    {
        replayAll();

        objectUnderTest_.queueMessage(mockMessage_);

        verifyAll();
    }

    public void testConnectedSupplierDoesCloneMessage()
    {
        mockMessage_.clone();
        controlMessage_.setReturnValue(mockMessage_);

        mockClient_._is_a(null);
        controlClient_.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        controlClient_.setDefaultReturnValue(false);

        replayAll();

        objectUnderTest_.connectClient(mockClient_);
        objectUnderTest_.queueMessage(mockMessage_);

        verifyAll();
    }

    public void testDisposeDisposesPendingMessages() throws Exception
    {
        mockMessage_.clone();
        controlMessage_.setReturnValue(mockMessage_);

        mockMessage_.dispose();

        replayAll();

        objectUnderTest_.connectClient(mockClient_);
        objectUnderTest_.queueMessage(mockMessage_);
        objectUnderTest_.dispose();

        verifyAll();
    }
    
    public void testConnectedSupplierDoesQueueClonedMessage() throws Exception
    {
        MockControl controlClonedMessage = MockControl.createControl(Message.class);
        Message mockClonedMessage = (Message) controlClonedMessage.getMock();
        
        mockMessage_.clone();
        controlMessage_.setReturnValue(mockClonedMessage);
        mockMessage_.dispose();
        
        mockClient_._is_a(null);
        controlClient_.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        controlClient_.setDefaultReturnValue(false);

        controlClonedMessage.expectAndReturn(mockClonedMessage.getPriority(), 0, MockControl.ZERO_OR_MORE);
        mockClonedMessage.dispose();
        
        controlClonedMessage.replay();
        replayAll();

        objectUnderTest_.connectClient(mockClient_);
        objectUnderTest_.queueMessage(mockMessage_);
        mockMessage_.dispose();
        
        assertEquals(1, objectUnderTest_.getPendingMessagesCount());
        
        Message queuedMessage = objectUnderTest_.getMessageBlocking();
        assertSame(mockClonedMessage, queuedMessage);
        queuedMessage.dispose();

        assertEquals(0, objectUnderTest_.getPendingMessagesCount());
        
        controlClonedMessage.verify();
        verifyAll();
    }

    private void verifyAll()
    {
        controlClient_.verify();
        controlMessage_.verify();
        controlPOA_.verify();
    }

    private void replayAll()
    {
        controlClient_.replay();
        controlPOA_.replay();
        controlMessage_.replay();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(AbstractProxySupplierTest.class);
    }
}
