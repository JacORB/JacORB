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

package org.jacorb.test.notification.typed;

import junit.framework.Test;

import org.easymock.MockControl;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.test.notification.common.TypedServerTestCase;
import org.jacorb.test.notification.common.TypedServerTestSetup;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushConsumer;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;
import org.omg.CosTypedNotifyComm.TypedPushConsumer;
import org.omg.CosTypedNotifyComm.TypedPushConsumerHelper;
import org.omg.CosTypedNotifyComm.TypedPushConsumerOperations;
import org.omg.CosTypedNotifyComm.TypedPushConsumerPOATie;

public class TypedEventChannelIntegrationTest extends TypedServerTestCase
{
    private TypedEventChannel objectUnderTest_;

    public TypedEventChannelIntegrationTest(String name, TypedServerTestSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        objectUnderTest_ = getChannelFactory().create_typed_channel(new Property[0], new Property[0], new IntHolder());
    }

    protected void tearDown() throws Exception
    {
        objectUnderTest_.destroy();
    }

    public void testMyFactory()
    {
        assertTrue(getChannelFactory()._is_equivalent(objectUnderTest_.MyFactory()));
    }

    public void testCreateFilter() throws Exception
    {
        FilterFactory filterFactory = objectUnderTest_.default_filter_factory();
        Filter filter = filterFactory.create_filter(ETCLFilter.CONSTRAINT_GRAMMAR);
        assertEquals(ETCLFilter.CONSTRAINT_GRAMMAR, filter.constraint_grammar());
    }

    public void testSendPushPush() throws Exception
    {
        MockControl coffeeOperationsControl = MockControl.createControl(CoffeeOperations.class);
        CoffeeOperations coffeeOperationsMock = (CoffeeOperations) coffeeOperationsControl.getMock();

        MockControl typedPushConsumerControl = MockControl.createNiceControl(TypedPushConsumerOperations.class);
        TypedPushConsumerOperations typedPushConsumerMock = (TypedPushConsumerOperations) typedPushConsumerControl.getMock();

        CoffeePOATie consumerTie = new CoffeePOATie(coffeeOperationsMock);
        Coffee consumer = CoffeeHelper.narrow(consumerTie._this(getClientORB()));

        TypedPushConsumerPOATie typedPushConsumerTie = new TypedPushConsumerPOATie(typedPushConsumerMock);
        TypedPushConsumer typedPushConsumer = TypedPushConsumerHelper.narrow(typedPushConsumerTie._this(getClientORB()));

        typedPushConsumerControl.expectAndReturn(typedPushConsumerMock.get_typed_consumer(), consumer);

        coffeeOperationsMock.drinking_coffee("jacorb", 10);

        coffeeOperationsControl.replay();
        typedPushConsumerControl.replay();

        TypedSupplierAdmin supplierAdmin = objectUnderTest_.new_for_typed_notification_suppliers(InterFilterGroupOperator.AND_OP, new IntHolder());
        TypedProxyPushConsumer proxyPushConsumer = supplierAdmin.obtain_typed_notification_push_consumer(CoffeeHelper.id(), new IntHolder());
        CoffeeOperations typedProxyPushConsumer = CoffeeHelper.narrow(proxyPushConsumer.get_typed_consumer());

        TypedConsumerAdmin consumerAdmin = objectUnderTest_.new_for_typed_notification_consumers(InterFilterGroupOperator.AND_OP, new IntHolder());
        TypedProxyPushSupplier proxyPushSupplier = consumerAdmin.obtain_typed_notification_push_supplier(CoffeeHelper.id(), new IntHolder());
        proxyPushSupplier.connect_typed_push_consumer(typedPushConsumer);

        typedProxyPushConsumer.drinking_coffee("jacorb", 10);

        Thread.sleep(1000);

        coffeeOperationsControl.verify();
        typedPushConsumerControl.verify();
    }

    public static Test suite() throws Exception
    {
        return TypedServerTestCase.suite(TypedEventChannelIntegrationTest.class);
    }
}
