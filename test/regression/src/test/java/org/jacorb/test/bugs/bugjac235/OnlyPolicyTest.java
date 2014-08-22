package org.jacorb.test.bugs.bugjac235;

import static org.junit.Assert.fail;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

public class OnlyPolicyTest extends AbstractTestCase
{
    @Test
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

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup(JAC235Impl.class.getName());

    }
}
