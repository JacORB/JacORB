package org.jacorb.test.bugs.bugjac235;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;

public class OnlyPolicyTest extends AbstractTestCase
{
    public OnlyPolicyTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void testTimeout0() throws Exception
    {
        setTimeout(2000);

        setServerPolicy();

        // try to invoke the operation this
        // should result in a timeout exception because the server
        // will sleep longer than the timeout (sleep is int msecs.)
        try
        {
            server.hello( 4000 );
            fail ("testTimeout : TIMEOUT exception expected");
        }
        catch ( org.omg.CORBA.TIMEOUT t )
        {
            // expected
        }

        server.hello(1000);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("JAC235 Reply Timeout test");

        ClientServerSetup setup = new ClientServerSetup(suite, JAC235Impl.class.getName());

        TestUtils.addToSuite(suite, setup, OnlyPolicyTest.class);

        return setup;
    }
}
