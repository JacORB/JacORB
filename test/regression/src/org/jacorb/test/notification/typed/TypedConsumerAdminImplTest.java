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

import org.jacorb.notification.servant.TypedConsumerAdminImpl;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdminHelper;

import junit.framework.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPullSupplier;
import org.omg.CosTypedNotifyChannelAdmin.TypedProxyPushSupplier;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TypedConsumerAdminImplTest extends NotificationTestCase {

    private TypedConsumerAdminImpl objectUnderTest_;

    private TypedConsumerAdmin consumerAdmin_;

    public TypedConsumerAdminImplTest(String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }

    public void setUp() throws Exception {
        objectUnderTest_ = new TypedConsumerAdminImpl();

        getChannelContext().setEventChannel(getDefaultChannel());

        getChannelContext().resolveDependencies(objectUnderTest_);

        objectUnderTest_.preActivate();

        consumerAdmin_ = TypedConsumerAdminHelper.narrow(objectUnderTest_.activate());
    }


    public void testMyChannel() throws Exception {
        assertEquals(getDefaultChannel(), consumerAdmin_.MyChannel());
    }


    public void testCreateTypedPullSupplier() throws Exception {
        IntHolder id = new IntHolder();

        TypedProxyPullSupplier supplier =
            consumerAdmin_.obtain_typed_notification_pull_supplier(PullCoffeeHelper.id(), id);

        assertEquals(supplier, consumerAdmin_.get_proxy_supplier(id.value));
    }


    public void testCreateTypedPushSupplier() throws Exception {
        IntHolder id = new IntHolder();

        TypedProxyPushSupplier supplier =
            consumerAdmin_.obtain_typed_notification_push_supplier(CoffeeHelper.id(), id);

        assertEquals(supplier, consumerAdmin_.get_proxy_supplier(id.value));
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(TypedConsumerAdminImplTest.class);
    }
}
