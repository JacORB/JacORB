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

import org.easymock.MockControl;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.container.RepositoryComponentAdapter;
import org.jacorb.notification.servant.IEventChannel;
import org.jacorb.notification.servant.TypedSupplierAdminImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdminHelper;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class TypedSupplierAdminImplTest extends NotificationTestCase
{
    private TypedSupplierAdminImpl objectUnderTest_;

    private TypedSupplierAdmin supplierAdmin_;

    private MutablePicoContainer container_;

    public TypedSupplierAdminImplTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    public void setUpTest() throws Exception
    {
        container_ = getPicoContainer();

        container_.registerComponentInstance(new OfferManager());

        container_.registerComponentInstance(new SubscriptionManager());

        container_.registerComponent(new RepositoryComponentAdapter());
        
        MockControl controlChannel = MockControl.createControl(IEventChannel.class);
        IEventChannel mockChannel = (IEventChannel) controlChannel.getMock();

        mockChannel.getEventChannel();
        controlChannel.setReturnValue(getDefaultChannel());

        mockChannel.getContainer();
        controlChannel.setReturnValue(container_);

        mockChannel.getAdminID();
        controlChannel.setReturnValue(10);

        mockChannel.getChannelID();
        controlChannel.setReturnValue(20);

        controlChannel.replay();

        objectUnderTest_ = new TypedSupplierAdminImpl(mockChannel, getORB(), getPOA(),
                getConfiguration(), getMessageFactory(), (OfferManager) container_
                        .getComponentInstance(OfferManager.class), (SubscriptionManager) container_
                        .getComponentInstance(SubscriptionManager.class));

        supplierAdmin_ = TypedSupplierAdminHelper.narrow(objectUnderTest_.activate());
    }

    public void testMyChannel() throws Exception
    {
        assertEquals(getDefaultChannel(), supplierAdmin_.MyChannel());
    }

    public void testCreatePushConsumer() throws Exception
    {
        IntHolder id = new IntHolder();

        TypedProxyPushConsumer consumer = supplierAdmin_.obtain_typed_notification_push_consumer(
                CoffeeHelper.id(), id);

        assertEquals(consumer, supplierAdmin_.get_proxy_consumer(id.value));
    }

    public void testCreatePullConsumer() throws Exception
    {
        IntHolder id = new IntHolder();

        TypedProxyPullConsumer consumer = supplierAdmin_.obtain_typed_notification_pull_consumer(
                PullCoffeeHelper.id(), id);

        assertEquals(consumer, supplierAdmin_.get_proxy_consumer(id.value));
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite("TypedSupplierAdminImpl Tests",
                TypedSupplierAdminImplTest.class);
    }
}