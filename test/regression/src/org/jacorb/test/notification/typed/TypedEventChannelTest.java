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

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.conf.Attributes;
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
 * @version $Id$
 */
public class TypedEventChannelTest extends TestCase
{
    private TypedEventChannelFactory typedChannelFactory_;
    private TypedEventChannel objectUnderTest_;
    private AbstractChannelFactory servant_;
   
    protected void setUp() throws Exception
    {
        super.setUp();

        Properties props = new Properties();
        props.put(Attributes.ENABLE_TYPED_CHANNEL, "on");

        servant_ = AbstractChannelFactory.newFactory(props);
        org.omg.CORBA.Object obj = servant_.activate();

        typedChannelFactory_ = TypedEventChannelFactoryHelper.narrow(obj);
        
        objectUnderTest_ = typedChannelFactory_.create_typed_channel(new Property[0],
                new Property[0], new IntHolder());
    }

    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        servant_.destroy();
    }
    
   
    public TypedEventChannelTest(String name)
    {
        super(name);
    }

    public void testCreateTypedEventChannel() throws Exception
    {
        IntHolder id = new IntHolder();
        TypedEventChannel channel = typedChannelFactory_.create_typed_channel(new Property[0],
                new Property[0], id);

        assertEquals(typedChannelFactory_, channel.MyFactory());
    }

    public void testForConsumers() throws Exception
    {
        assertNotNull(objectUnderTest_.for_consumers());
    }

    public void testForSuppliers() throws Exception
    {
        assertNotNull(objectUnderTest_.for_suppliers());
    }

    public void testDefaults() throws Exception
    {
        assertNotNull(objectUnderTest_.default_consumer_admin());
        assertNotNull(objectUnderTest_.default_supplier_admin());
        assertNotNull(objectUnderTest_.default_filter_factory());
    }
    
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
    
    public void testDefaultConsumerAdmin() throws Exception
    {
        TypedConsumerAdmin consumerAdmin = objectUnderTest_.default_consumer_admin();
        assertEquals(0, consumerAdmin.MyID());
        assertTrue(consumerAdmin._is_equivalent(objectUnderTest_.get_consumeradmin(0)));
    }
    
    public void testDefaultSupplierAdmin() throws Exception
    {
        TypedSupplierAdmin admin = objectUnderTest_.default_supplier_admin();
        assertEquals(0, admin.MyID());
        assertTrue(admin._is_equivalent(objectUnderTest_.get_supplieradmin(0)));
    }
    
    public static Test suite()
    {
        return new TestSuite(TypedEventChannelTest.class);
    }
}
