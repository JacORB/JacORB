package org.jacorb.test.transport;

import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.ORB;


public interface AbstractTester {

    public abstract void test_transport_current(ORB orb, Logger logger);

}
