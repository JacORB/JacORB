package org.jacorb.test.notification;

import java.util.Properties;

import junit.framework.Test;

import org.jacorb.notification.AbstractChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

/**
 * @author Alphonse Bendt
 */

public class EventChannelFactoryTest
    extends NotificationTestCase {

    AbstractChannelFactory factory_;

    ////////////////////////////////////////

    public EventChannelFactoryTest (String name, NotificationTestCaseSetup setup){
        super(name, setup);
    }

    ////////////////////////////////////////

    public void setUpTest() throws Exception {
        factory_ = AbstractChannelFactory.newFactory(new Properties());

        factory_.activate();
    }


    public void tearDownTest() throws Exception {
        factory_.dispose();
    }


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


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(EventChannelFactoryTest.class);
    }
}
