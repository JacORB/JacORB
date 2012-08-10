package org.jacorb.test.notification.typed;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.Properties;

import junit.framework.Test;

import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.servant.ITypedAdmin;
import org.jacorb.notification.servant.TypedProxyPullConsumerImpl;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StringHolder;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventTypeHelper;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumerHelper;
import org.omg.CosTypedNotifyComm.TypedPullSupplier;
import org.omg.CosTypedNotifyComm.TypedPullSupplierHelper;
import org.omg.CosTypedNotifyComm.TypedPullSupplierPOATie;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedProxyPullConsumerImplTest extends NotificationTestCase
{
    private TypedProxyPullConsumerImpl objectUnderTest_;

    private TypedProxyPullConsumer proxyPullConsumer_;

    private MockControl controlTypedPullSupplier_;

    private TypedPullSupplier mockTypedPullSupplier_;

    private MockControl controlAdmin_;

    private ITypedAdmin mockAdmin_;

    private MockControl controlSupplierAdmin_;

    private SupplierAdmin mockSupplierAdmin_;

    private MockControl controlTaskProcessor_;

    private TaskProcessor mockTaskProcessor_;

    private MockControl controlPullCoffeeOperations_;

    private PullCoffeeOperations mockPullCoffee_;

    private PullCoffee pullCoffee_;

    private MockControl controlScheduledFuture_;

    private ScheduledFuture mockScheduledFuture_;

    private ORBSetup myORB;

    public void setUpTest() throws Exception
    {
    	Properties props = new Properties();
    	props.setProperty("ORBInitRef.InterfaceRepository", getRepository().toString());
    	// FIXME: bugzilla #820 - disabled security for some regression tests
    	props.setProperty (CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
    	myORB = new ORBSetup(this, props);
    	myORB.setUp();

        controlScheduledFuture_ = MockControl.createControl(ScheduledFuture.class);
        mockScheduledFuture_ = (ScheduledFuture) controlScheduledFuture_.getMock();
        controlAdmin_ = MockControl.createNiceControl(ITypedAdmin.class);
        mockAdmin_ = (ITypedAdmin) controlAdmin_.getMock();
        mockAdmin_.getProxyID();
        controlAdmin_.setReturnValue(10);

        mockAdmin_.isIDPublic();
        controlAdmin_.setReturnValue(true);

        mockAdmin_.getContainer();
        controlAdmin_.setReturnValue(getPicoContainer());

        mockAdmin_.getSupportedInterface();
        controlAdmin_.setDefaultReturnValue(PullCoffeeHelper.id());

        controlAdmin_.replay();

        controlSupplierAdmin_ = MockControl.createControl(SupplierAdmin.class);
        mockSupplierAdmin_ = (SupplierAdmin) controlSupplierAdmin_.getMock();

        controlSupplierAdmin_.replay();

        controlTaskProcessor_ = MockControl.createControl(TaskProcessor.class);
        mockTaskProcessor_ = (TaskProcessor) controlTaskProcessor_.getMock();

        objectUnderTest_ = new TypedProxyPullConsumerImpl(mockAdmin_, mockSupplierAdmin_, getORB(),
                getPOA(), getConfiguration(), mockTaskProcessor_, getMessageFactory(),
                new OfferManager(), new SubscriptionManager());

        String string = getORB().object_to_string(
                TypedProxyPullConsumerHelper.narrow(objectUnderTest_.activate()));
        proxyPullConsumer_ = TypedProxyPullConsumerHelper.narrow(getClientORB().string_to_object(
                string));

        controlTypedPullSupplier_ = MockControl.createControl(TypedPullSupplier.class);
        mockTypedPullSupplier_ = (TypedPullSupplier) controlTypedPullSupplier_.getMock();

        controlPullCoffeeOperations_ = MockControl.createControl(PullCoffeeOperations.class);

        mockPullCoffee_ = (PullCoffeeOperations) controlPullCoffeeOperations_.getMock();

        pullCoffee_ = new PullCoffeePOATie(mockPullCoffee_)._this(getClientORB());
    }

    protected void tearDownTest() throws Exception
    {
    	myORB.tearDown();
    }

    public TypedProxyPullConsumerImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public ORB getClientORB()
    {
    	return myORB.getORB();
    }


    public void testId()
    {
        assertTrue(objectUnderTest_.isIDPublic());
        assertEquals(new Integer(10), objectUnderTest_.getID());
    }

    public void testMyType()
    {
        assertEquals(ProxyType.PULL_TYPED, proxyPullConsumer_.MyType());
    }

    public void testConnect() throws Exception
    {
        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(pullCoffee_);

        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        replayAll();

        proxyPullConsumer_.connect_typed_pull_supplier(TypedPullSupplierHelper
                .narrow(new TypedPullSupplierPOATie(mockTypedPullSupplier_)._this(getClientORB())));

        verifyAll();
    }

    public void testConnectInvalidType() throws Exception
    {
        TypedPullSupplier _typedPullSupplier = TypedPullSupplierHelper
                .narrow(new TypedPullSupplierPOATie(mockTypedPullSupplier_)._this(getClientORB()));

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(_typedPullSupplier);

        replayAll();

        try
        {
            proxyPullConsumer_.connect_typed_pull_supplier(_typedPullSupplier);
            fail();
        } catch (TypeError e)
        {
            // expected
        }
        verifyAll();
    }

    public void testTryOperationsAreInvoked() throws Exception
    {
        controlPullCoffeeOperations_.setDefaultMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] arg0, Object[] arg1)
            {
                StringHolder name = (StringHolder) arg0[0];

                if (name != null)
                {
                    // need to set the out param.
                    name.value = "";
                }

                return true;
            }
        });

        mockPullCoffee_.try_drinking_coffee(null, null);

        controlPullCoffeeOperations_.setReturnValue(false);

        mockPullCoffee_.try_cancel_coffee(null);

        controlPullCoffeeOperations_.setReturnValue(false);

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(pullCoffee_);

        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        replayAll();

        proxyPullConsumer_.connect_typed_pull_supplier(new TypedPullSupplierPOATie(
                mockTypedPullSupplier_)._this(getClientORB()));

        objectUnderTest_.runPullMessage();

        verifyAll();
    }

    public void testTryOperationsThrowsException() throws Exception
    {
        mockPullCoffee_.try_drinking_coffee(null, null);
        controlPullCoffeeOperations_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlPullCoffeeOperations_.setThrowable(new TRANSIENT());

        mockPullCoffee_.try_cancel_coffee(null);
        controlPullCoffeeOperations_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] arg0, Object[] arg1)
            {
                StringHolder name = (StringHolder) arg0[0];

                if (name != null)
                {
                    // need to set the out param
                    name.value = "jacorb";
                }

                return true;
            }
        });
        controlPullCoffeeOperations_.setReturnValue(true);

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(pullCoffee_);

        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        mockTaskProcessor_.processMessage(null);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);

        replayAll();

        proxyPullConsumer_.connect_typed_pull_supplier(new TypedPullSupplierPOATie(
                mockTypedPullSupplier_)._this(getClientORB()));

        objectUnderTest_.runPullMessage();

        verifyAll();
    }

    public void testFormat() throws Exception
    {
        mockTaskProcessor_.processMessage(null);

        controlTaskProcessor_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                if (expected[0] != null)
                {
                    try
                    {
                        Property[] _props = ((Message) expected[0]).toTypedEvent();

                        assertEquals(3, _props.length);

                        assertEquals("event_type", _props[0].name);
                        EventType et = EventTypeHelper.extract(_props[0].value);
                        assertEquals(PullCoffeeHelper.id(), et.domain_name);

                        assertEquals(
                                "::org::jacorb::test::notification::typed::PullCoffee::drinking_coffee",
                                et.type_name);

                        assertEquals("jacorb", _props[1].value.extract_string());
                        assertEquals(20, _props[2].value.extract_long());

                        return true;
                    } catch (Exception e)
                    {
                        return false;
                    }
                }
                return true;
            }
        });

        mockPullCoffee_.try_drinking_coffee(null, null);
        controlPullCoffeeOperations_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                StringHolder name = (StringHolder) expected[0];
                IntHolder minutes = (IntHolder) expected[1];

                // need to set the out params
                if (name != null)
                {
                    name.value = "jacorb";
                }

                if (minutes != null)
                {
                    minutes.value = 20;
                }

                return true;
            }
        });
        controlPullCoffeeOperations_.setReturnValue(true);

        mockPullCoffee_.try_cancel_coffee(null);
        controlPullCoffeeOperations_.setMatcher(new AbstractMatcher()
        {
            public boolean matches(Object[] expected, Object[] actual)
            {
                StringHolder name = (StringHolder) expected[0];

                if (name != null)
                {
                    // need to set the out param
                    name.value = "";
                }

                return true;
            }
        });
        controlPullCoffeeOperations_.setReturnValue(false);

        mockTypedPullSupplier_.get_typed_supplier();
        controlTypedPullSupplier_.setReturnValue(pullCoffee_);

        mockTaskProcessor_.executeTaskPeriodically(0, null, false);
        controlTaskProcessor_.setMatcher(MockControl.ALWAYS_MATCHER);
        controlTaskProcessor_.setReturnValue(mockScheduledFuture_);

        replayAll();

        proxyPullConsumer_.connect_typed_pull_supplier(new TypedPullSupplierPOATie(
                mockTypedPullSupplier_)._this(getClientORB()));

        objectUnderTest_.runPullMessage();

        verifyAll();
    }

    private void verifyAll()
    {
        controlScheduledFuture_.verify();
        controlTypedPullSupplier_.verify();
        controlAdmin_.verify();
        controlSupplierAdmin_.verify();
        controlTaskProcessor_.verify();
        controlPullCoffeeOperations_.verify();
    }

    private void replayAll()
    {
        controlScheduledFuture_.replay();
        controlPullCoffeeOperations_.replay();
        controlTypedPullSupplier_.replay();
        controlTaskProcessor_.replay();
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(TypedProxyPullConsumerImplTest.class);
    }
}