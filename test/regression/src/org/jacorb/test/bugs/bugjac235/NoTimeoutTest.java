package org.jacorb.test.bugs.bugjac235;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;

public class NoTimeoutTest extends AbstractTestCase
{
    public NoTimeoutTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("JAC235 Reply Timeout test");

        ClientServerSetup setup = new ClientServerSetup(suite, JAC235Impl.class.getName());

        TestUtils.addToSuite(suite, setup, NoTimeoutTest.class);

        return setup;
    }

    public void testNormalInvocation()
    {
        assertNotNull(server.hello(500));
    }
}
