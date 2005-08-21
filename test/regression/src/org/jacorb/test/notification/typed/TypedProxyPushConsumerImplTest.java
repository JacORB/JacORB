package org.jacorb.test.notification.typed;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */

import junit.framework.Test;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.ITypedAdmin;
import org.jacorb.notification.servant.TypedProxyPushConsumerImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.PushSupplierOperations;
import org.omg.CosNotifyComm.PushSupplierPOATie;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumerHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPushConsumerImplTest extends NotificationTestCase
{
    private TypedProxyPushConsumerImpl objectUnderTest_;

    private TypedProxyPushConsumer proxyPushConsumer_;

    private final static String DRINKING_COFFEE_ID = "::org::jacorb::test::notification::typed::Coffee::drinking_coffee";

    private MockControl controlAdmin_;

    private ITypedAdmin mockAdmin_;

    private MockControl controlSupplierAdmin_;

    private SupplierAdmin mockSupplierAdmin_;

    public void setUpTest() throws Exception
    {
        controlAdmin_ = MockControl.createNiceControl(ITypedAdmin.class);
        mockAdmin_ = (ITypedAdmin) controlAdmin_.getMock();
        mockAdmin_.getProxyID();
        controlAdmin_.setReturnValue(10);

        mockAdmin_.isIDPublic();
        controlAdmin_.setReturnValue(true);

        mockAdmin_.getContainer();
        controlAdmin_.setReturnValue(getPicoContainer());

        mockAdmin_.getSupportedInterface();
        controlAdmin_.setDefaultReturnValue(CoffeeHelper.id());

        controlAdmin_.replay();

        controlSupplierAdmin_ = MockControl.createControl(SupplierAdmin.class);
        mockSupplierAdmin_ = (SupplierAdmin) controlSupplierAdmin_.getMock();

        objectUnderTest_ = new TypedProxyPushConsumerImpl(mockAdmin_, mockSupplierAdmin_, getORB(),
                getPOA(), getConfiguration(), getTaskProcessor(), getMessageFactory(),
                new OfferManager(), new SubscriptionManager(), getRepository());

        proxyPushConsumer_ = TypedProxyPushConsumerHelper.narrow(objectUnderTest_.activate());
    }

    public TypedProxyPushConsumerImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void testID()
    {
        assertEquals(new Integer(10), objectUnderTest_.getID());
        assertTrue(objectUnderTest_.isIDPublic());
    }

    public void testGetTypedConsumer() throws Exception
    {
        Coffee coffee = CoffeeHelper.narrow(proxyPushConsumer_.get_typed_consumer());

        assertNotNull(coffee);

        assertTrue(coffee._is_a(CoffeeHelper.id()));
    }

    public void testInvokeDrinkingCoffee() throws Exception
    {
        MockControl controlTaskProcessor = MockControl.createControl(TaskProcessor.class);
        TaskProcessor mockTaskProcessor = (TaskProcessor) controlTaskProcessor.getMock();

        TypedEventMessage message = new TypedEventMessage();
        Message handle = message.getHandle();

        mockTaskProcessor.processMessage(handle);
        controlTaskProcessor.setMatcher(new AbstractMatcher()
        {
            protected boolean argumentMatches(Object exp, Object act)
            {
                Message mesg = (Message) exp;

                assertEquals(Message.TYPE_TYPED, mesg.getType());

                try
                {
                    Property[] _props = mesg.toTypedEvent();

                    assertEquals("event_type", _props[0].name);
                    EventType et = EventTypeHelper.extract(_props[0].value);

                    assertEquals(CoffeeHelper.id(), et.domain_name);
                    assertEquals(DRINKING_COFFEE_ID, et.type_name);

                    assertEquals("name", _props[1].name);
                    assertEquals("jacorb", _props[1].value.extract_string());

                    assertEquals("minutes", _props[2].name);
                    assertEquals(10, _props[2].value.extract_long());
                } catch (Exception e)
                {
                    fail();
                }
                return true;
            }
        });

        controlTaskProcessor.replay();

        objectUnderTest_ = new TypedProxyPushConsumerImpl(mockAdmin_, mockSupplierAdmin_, getORB(),
                getPOA(), getConfiguration(), mockTaskProcessor, getMessageFactory(),
                new OfferManager(), new SubscriptionManager(), getRepository());

        proxyPushConsumer_ = TypedProxyPushConsumerHelper.narrow(objectUnderTest_.activate());

        org.omg.CORBA.Object obj = CoffeeHelper.narrow(proxyPushConsumer_.get_typed_consumer());

        // some extra steps involved as local invocations are not
        // supported on dsi servants.

        String coffString = obj.toString();

        Coffee coffee = CoffeeHelper.narrow(getClientORB().string_to_object(coffString));

        coffee.drinking_coffee("jacorb", 10);

        controlTaskProcessor.verify();
    }

    public void testMyType() throws Exception
    {
        assertEquals(ProxyType.PUSH_TYPED, proxyPushConsumer_.MyType());
    }

    public void testPushAny() throws Exception
    {
        MockControl controlPushSupplier = MockControl.createControl(PushSupplierOperations.class);
        PushSupplierOperations mockPushSupplier = (PushSupplierOperations) controlPushSupplier
                .getMock();

        controlPushSupplier.replay();
        
        PushSupplierPOATie tie = new PushSupplierPOATie(mockPushSupplier);
        
        proxyPushConsumer_.connect_typed_push_supplier(tie._this(getClientORB()));

        Any any = getORB().create_any();

        any.insert_string("push");

        try
        {
            proxyPushConsumer_.push(any);

            fail("TypedProxyPushConsumer shouldn't support untyped push");
        } catch (NO_IMPLEMENT e)
        {
            // expected
        }
        
        controlPushSupplier.verify();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite("TypedProxyPushConsumer Tests",
                TypedProxyPushConsumerImplTest.class);
    }
}