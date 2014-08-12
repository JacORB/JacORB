package org.jacorb.test.bugs.bugjac235;

import static org.junit.Assert.assertNotNull;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

public class NoTimeoutTest extends AbstractTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup(JAC235Impl.class.getName());

    }

    @Test
    public void testNormalInvocation()
    {
        assertNotNull(server.hello(500));
    }
}
