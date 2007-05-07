package org.jacorb.test.bugs.bugjac235;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BugJac235Suite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(HigherPropertyTest.suite());
        suite.addTest(LowerPropertyTest.suite());
        suite.addTest(NoTimeoutTest.suite());
        suite.addTest(OnlyPolicyTest.suite());
        suite.addTest(OnlyPropertyTest.suite());

        return suite;
    }
}
