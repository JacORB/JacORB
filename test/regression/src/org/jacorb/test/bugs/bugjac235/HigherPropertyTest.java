package org.jacorb.test.bugs.bugjac235;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;

public class HigherPropertyTest extends AbstractTestCase
{
    public HigherPropertyTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("JAC235 Reply Timeout test");

        final Properties serverProps = new Properties();
        serverProps.setProperty(PROP_PENDING_REPLY_TIMEOUT, "8000");
        ClientServerSetup setup = new ClientServerSetup(suite, JAC235Impl.class.getName(), serverProps, serverProps);

        TestUtils.addToSuite(suite, setup, HigherPropertyTest.class);

        return setup;
    }

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
