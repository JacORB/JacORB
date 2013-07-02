package org.jacorb.test.transport;

import org.omg.CORBA.ORB;
import org.slf4j.Logger;


public interface AbstractTester 
{
    void test_transport_current(ORB orb, Logger logger);
}
