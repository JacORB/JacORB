package org.jacorb.test.bugs.bugjac235;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

public class LowerPropertyTest extends AbstractTestCase
{
    @Test
    public void testTimeout() throws Exception
    {
        setTimeout(2000);

        setServerPolicy();

        // try to invoke the operation this
        // should result in a timeout exception because the server
        // will sleep longer than the timeout (sleep is int msecs.)
        try
        {
            server.hello(4000);
            fail ("testTimeout : TIMEOUT exception expected");
        }
        catch ( org.omg.CORBA.TIMEOUT t )
        {
            // expected
        }

        server.hello(500);

        /* If property timeout value is lower than policy timeout value
         * there should be no timeout if the server responds in between
         * the 2 values e.g. property = 10 seconds, policy = 20 seconds
         * server responds in 15 seconds - this tests the problem raised
         * by Jac#235
         */
        server.hello (1500);
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        final Properties serverProps = new Properties();
        serverProps.setProperty(PROP_PENDING_REPLY_TIMEOUT, "1000");
        setup = new ClientServerSetup(JAC235Impl.class.getName(), serverProps, serverProps);
    }
}
