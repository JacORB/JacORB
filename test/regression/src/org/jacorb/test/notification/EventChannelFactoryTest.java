package org.jacorb.test.notification;

import org.jacorb.notification.EventChannelFactoryImpl;

import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelFactoryTest
    extends TestCase {

    EventChannelFactoryImpl factory_;

    ORB orb;

    ////////////////////////////////////////

    public EventChannelFactoryTest (String name){
        super(name);
    }

    ////////////////////////////////////////

    public void setUp() throws Exception {
        super.setUp();

        factory_ = EventChannelFactoryImpl.newFactory();
        factory_.getEventChannelFactory();
        orb = ORB.init(new String[0], null);
    }


    public void tearDown() throws Exception {
        super.tearDown();

        factory_.dispose();
        orb.shutdown(true);
    }


    public void testGetCorbaLoc() throws Exception {
        String _corbaLoc = factory_.getCorbaLoc();

        assertNotNull(_corbaLoc);

        org.omg.CORBA.Object obj =
            orb.string_to_object(_corbaLoc);

        assertNotNull(obj);

        EventChannelFactory factory =
            EventChannelFactoryHelper.narrow(obj);

        assertFalse(factory._non_existent());
    }


    public void testGetIOR() throws Exception {
        String ior = factory_.getIOR();

        assertNotNull(ior);

        org.omg.CORBA.Object obj =
            orb.string_to_object(ior);

        assertNotNull(obj);

        EventChannelFactory factory =
            EventChannelFactoryHelper.narrow(obj);

        assertFalse(factory._non_existent());
    }


    public static TestSuite suite(){
        TestSuite suite =
            new TestSuite(EventChannelFactoryTest.class);

        return suite;
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
