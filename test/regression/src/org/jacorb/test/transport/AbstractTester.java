package org.jacorb.test.transport;

import org.slf4j.Logger;
import org.omg.CORBA.ORB;


public interface AbstractTester 
{
    void test_transport_current(ORB orb, Logger logger);
}
