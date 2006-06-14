package org.jacorb.test.dii;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTest
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("DII/DSI Tests");

        suite.addTest(DiiTest.suite());

        return suite;
    }
}
