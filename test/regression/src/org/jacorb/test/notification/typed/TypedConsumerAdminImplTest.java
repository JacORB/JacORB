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

import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.servant.IEventChannel;
import org.jacorb.notification.servant.TypedConsumerAdminImpl;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 */

public class TypedConsumerAdminImplTest extends NotificationTestCase
{
    private TypedConsumerAdminImpl objectUnderTest_;

    private TypedConsumerAdmin consumerAdmin_;

    private MutablePicoContainer container_;
    
    public TypedConsumerAdminImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        container_ = getPicoContainer();

        container_.registerComponentInstance(new OfferManager());
        container_.registerComponentInstance(new SubscriptionManager());
        
        MockControl controlChannel = MockControl.createControl(IEventChannel.class);
        IEventChannel mockChannel = (IEventChannel) controlChannel.getMock();

        mockChannel.getEventChannel();
        controlChannel.setReturnValue(null);

        mockChannel.getContainer();
        controlChannel.setReturnValue(container_);

        mockChannel.getAdminID();
        controlChannel.setReturnValue(10);

        mockChannel.getChannelID();
        controlChannel.setReturnValue(20);

        mockChannel.getChannelMBean();
        controlChannel.setReturnValue("channel");
        
        controlChannel.replay();

        objectUnderTest_ = new TypedConsumerAdminImpl(getORB(), getPOA(), getConfiguration(),
                getMessageFactory(), (OfferManager) container_
                        .getComponentInstance(OfferManager.class), (SubscriptionManager) container_
                        .getComponentInstance(SubscriptionManager.class), mockChannel);
        objectUnderTest_.setInterFilterGroupOperator(InterFilterGroupOperator.AND_OP);
        consumerAdmin_ = TypedConsumerAdminHelper.narrow(objectUnderTest_.activate());
    }

    public void testContainer()
    {
        assertNotNull(container_.getComponentInstance(ConsumerAdmin.class));
    }
    
    public void testIDs()
    {
        assertEquals(10, consumerAdmin_.MyID());
    }

    public void testCreateTypedPullSupplier() throws Exception
    {
        IntHolder id = new IntHolder();

        TypedProxyPullSupplier supplier = consumerAdmin_.obtain_typed_notification_pull_supplier(
                PullCoffeeHelper.id(), id);

        assertEquals(supplier, consumerAdmin_.get_proxy_supplier(id.value));
        
        assertEquals(supplier, supplier.MyAdmin().get_proxy_supplier(id.value));
    }

    public void testCreateTypedPushSupplier() throws Exception
    {
        IntHolder id = new IntHolder();

        TypedProxyPushSupplier supplier = consumerAdmin_.obtain_typed_notification_push_supplier(
                CoffeeHelper.id(), id);

        assertEquals(supplier, consumerAdmin_.get_proxy_supplier(id.value));
        
        assertEquals(supplier, supplier.MyAdmin().get_proxy_supplier(id.value));
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite("TypedConsumerAdminImpl Tests",
                TypedConsumerAdminImplTest.class);
    }
}