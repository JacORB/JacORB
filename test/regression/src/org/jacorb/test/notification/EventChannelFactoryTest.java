package org.jacorb.test.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.EventChannelFactoryImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.jacorb.orb.util.CorbaLoc;

/**
 *  Unit Test for class EventChannelFactory
 *
 *
 * Created: Thu Jul 17 14:16:43 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EventChannelFactoryTest extends TestCase {

    EventChannelFactoryImpl factory_;
    ORB orb;

    public void setUp() throws Exception {
	factory_ = EventChannelFactoryImpl.newFactory();
	orb = ORB.init(new String[0], null);
    }

    public void tearDown() throws Exception {
	factory_.dispose();
	orb.shutdown(true);
    }

    public void testGetCorbaLoc() throws Exception {
	String _corbaLoc = factory_.getCorbaLoc();
	assertNotNull(_corbaLoc);

	org.omg.CORBA.Object obj = orb.string_to_object(_corbaLoc);
	assertNotNull(obj);

	EventChannelFactory factory = EventChannelFactoryHelper.narrow(obj);

	assertFalse(factory._non_existent());
    }

    public void testGetIOR() throws Exception {
	String ior = factory_.getIOR();

	assertNotNull(ior);

	org.omg.CORBA.Object obj = orb.string_to_object(ior);
	assertNotNull(obj);

	EventChannelFactory factory = EventChannelFactoryHelper.narrow(obj);

	assertFalse(factory._non_existent());
    }

    /** 
     * Creates a new <code>EventChannelFactoryTest</code> instance.
     *
     * @param name test name
     */
    public EventChannelFactoryTest (String name){
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite suite = new TestSuite(EventChannelFactoryTest.class);
	
	return suite;
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}
