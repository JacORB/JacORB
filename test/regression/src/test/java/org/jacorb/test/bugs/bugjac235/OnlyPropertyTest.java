package org.jacorb.test.bugs.bugjac235;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.junit.BeforeClass;
import org.junit.Test;

public class OnlyPropertyTest extends AbstractTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        final Properties serverProps = new Properties();
        serverProps.setProperty(PROP_PENDING_REPLY_TIMEOUT, "2000");
        setup = new ClientServerSetup(JAC235Impl.class.getName(), serverProps, serverProps);

    }

    @Test
    public void testTimeout()
    {
        JAC235 srv = server;

        try
        {
            srv.hello( 4000 );

            fail ("testNoPolicy : TIMEOUT exception expected");
        }
        catch ( org.omg.CORBA.TIMEOUT t )
        {
            // expected
        }

        srv.hello(1000);
    }
}
