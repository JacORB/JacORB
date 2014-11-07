/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannel;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactory;
import org.omg.CosTypedNotifyChannelAdmin.TypedEventChannelFactoryHelper;
import org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin;

/**
 * @author Alphonse Bendt
 */
public class TypedEventChannelTest extends ORBTestCase
{
    private TypedEventChannelFactory typedChannelFactory_;
    private TypedEventChannel objectUnderTest_;
    private AbstractChannelFactory servant_;

    @Before
    public void setUp() throws Exception
    {
        Properties props = new Properties();
        props.put(Attributes.ENABLE_TYPED_CHANNEL, "on");

        servant_ = AbstractChannelFactory.newFactory(props);
        org.omg.CORBA.Object obj = servant_.activate();

        typedChannelFactory_ = TypedEventChannelFactoryHelper.narrow(obj);

        objectUnderTest_ = typedChannelFactory_.create_typed_channel(new Property[0],
                new Property[0], new IntHolder());
    }


    @After
    public void tearDown() throws Exception
    {
        servant_.destroy();
    }

    @Test
    public void testCreateTypedEventChannel() throws Exception
    {
        IntHolder id = new IntHolder();
        TypedEventChannel channel = typedChannelFactory_.create_typed_channel(new Property[0],
                new Property[0], id);

        assertEquals(typedChannelFactory_, channel.MyFactory());
    }

    @Test
    public void testForConsumers() throws Exception
    {
        assertNotNull(objectUnderTest_.for_consumers());
    }

    @Test
    public void testForSuppliers() throws Exception
    {
        assertNotNull(objectUnderTest_.for_suppliers());
    }

    @Test
    public void testDefaults() throws Exception
    {
        assertNotNull(objectUnderTest_.default_consumer_admin());
        assertNotNull(objectUnderTest_.default_supplier_admin());
        assertNotNull(objectUnderTest_.default_filter_factory());
    }

    @Test
    public void testCreateConsumerAdmin() throws Exception
    {
        assertEquals(0, objectUnderTest_.get_all_consumeradmins().length);

        IntHolder id = new IntHolder();

        org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin admin = objectUnderTest_.new_for_typed_notification_consumers(InterFilterGroupOperator.OR_OP, id);

        int[] ids = objectUnderTest_.get_all_consumeradmins();

        assertEquals(1, ids.length);
        assertEquals(id.value, ids[0]);

        assertEquals(admin, objectUnderTest_.get_consumeradmin(id.value));
    }

    @Test
    public void testCreateSupplierAdmin() throws Exception
    {
        assertEquals(0, objectUnderTest_.get_all_supplieradmins().length);
        IntHolder id = new IntHolder();

        org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin admin = objectUnderTest_.new_for_typed_notification_suppliers(InterFilterGroupOperator.OR_OP, id);

        int[] ids = objectUnderTest_.get_all_supplieradmins();

        assertEquals(1, ids.length);
        assertEquals(id.value, ids[0]);

        assertEquals(admin, objectUnderTest_.get_supplieradmin(id.value));
    }

    @Test
    public void testInterFilterGroupOperator() throws Exception
    {
        IntHolder id = new IntHolder();
        org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin admin1 = objectUnderTest_.new_for_typed_notification_consumers(InterFilterGroupOperator.OR_OP, id);
        assertEquals(InterFilterGroupOperator.OR_OP, admin1.MyOperator());

        org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin admin2 = objectUnderTest_.new_for_typed_notification_suppliers(InterFilterGroupOperator.OR_OP, id);
        assertEquals(InterFilterGroupOperator.OR_OP, admin2.MyOperator());

        org.omg.CosTypedNotifyChannelAdmin.TypedConsumerAdmin admin3 = objectUnderTest_.new_for_typed_notification_consumers(InterFilterGroupOperator.AND_OP, id);
        assertEquals(InterFilterGroupOperator.AND_OP, admin3.MyOperator());

        org.omg.CosTypedNotifyChannelAdmin.TypedSupplierAdmin admin4 = objectUnderTest_.new_for_typed_notification_suppliers(InterFilterGroupOperator.AND_OP, id);
        assertEquals(InterFilterGroupOperator.AND_OP, admin4.MyOperator());
    }

    @Test
    public void testDefaultConsumerAdmin() throws Exception
    {
        TypedConsumerAdmin consumerAdmin = objectUnderTest_.default_consumer_admin();
        assertEquals(0, consumerAdmin.MyID());
        assertTrue(consumerAdmin._is_equivalent(objectUnderTest_.get_consumeradmin(0)));
    }

    @Test
    public void testDefaultSupplierAdmin() throws Exception
    {
        TypedSupplierAdmin admin = objectUnderTest_.default_supplier_admin();
        assertEquals(0, admin.MyID());
        assertTrue(admin._is_equivalent(objectUnderTest_.get_supplieradmin(0)));
    }
    }
