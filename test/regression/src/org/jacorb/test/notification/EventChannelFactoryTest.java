package org.jacorb.test.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.EventChannelFactoryImpl;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;

/**
 * @author Alphonse Bendt
 */

public class EventChannelFactoryTest
    extends NotificationTestCase {

    AbstractChannelFactory factory_;

    @Before
    public void setUp() throws Exception
    {
        factory_ = AbstractChannelFactory.newFactory(new Properties());

        factory_.activate();
    }


    public void tearDownTest() throws Exception {
        factory_.dispose();
    }


    @Test
    public void testGetCorbaLoc() throws Exception {
        String _corbaLoc = factory_.getCorbaLoc();

        assertNotNull(_corbaLoc);

        org.omg.CORBA.Object obj =
            getClientORB().string_to_object(_corbaLoc);

        assertNotNull(obj);

        EventChannelFactory factory =
            EventChannelFactoryHelper.narrow(obj);

        assertFalse(factory._non_existent());
    }


    @Test
    public void testGetIOR() throws Exception {
        String ior = factory_.getIOR();

        assertNotNull(ior);

        org.omg.CORBA.Object obj =
            getClientORB().string_to_object(ior);

        assertNotNull(obj);

        EventChannelFactory factory =
            EventChannelFactoryHelper.narrow(obj);

        assertFalse(factory._non_existent());
    }

    @Test
    public void testDestroy() throws Exception
    {
        EventChannel channel_ = ((EventChannelFactoryImpl)factory_).create_channel(new Property[0], new Property[0], new IntHolder());
        ConsumerAdmin _consumerAdmin = channel_.new_for_consumers(InterFilterGroupOperator.AND_OP,
                new IntHolder());
        SupplierAdmin _supplierAdmin = channel_.new_for_suppliers(InterFilterGroupOperator.AND_OP,
                new IntHolder());

        assertEquals(channel_, _consumerAdmin.MyChannel());
        assertEquals(channel_, _supplierAdmin.MyChannel());

        channel_.destroy();

        try
        {
            channel_.MyFactory();
            fail();
        } catch (OBJECT_NOT_EXIST e)
        {
            // expected
        }
    }
}
