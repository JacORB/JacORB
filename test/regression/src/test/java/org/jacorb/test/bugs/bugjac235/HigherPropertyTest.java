package org.jacorb.test.bugs.bugjac235;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

public class HigherPropertyTest extends AbstractTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        final Properties serverProps = new Properties();
        serverProps.setProperty(PROP_PENDING_REPLY_TIMEOUT, "8000");
        setup = new ClientServerSetup(JAC235Impl.class.getName(), serverProps, serverProps);

    }

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
            server.hello( 4000 );
            fail ("testTimeout : TIMEOUT exception expected");
        }
        catch ( org.omg.CORBA.TIMEOUT t )
        {
        }

        server.hello(1000);
    }
}
