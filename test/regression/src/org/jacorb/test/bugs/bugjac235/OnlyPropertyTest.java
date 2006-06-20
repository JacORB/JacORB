package org.jacorb.test.bugs.bugjac235;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;

public class OnlyPropertyTest extends AbstractTestCase
{
    public OnlyPropertyTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("JAC235 Reply Timeout test");

        final Properties serverProps = new Properties();
        serverProps.setProperty(PROP_PENDING_REPLY_TIMEOUT, "2000");
        ClientServerSetup setup = new ClientServerSetup(suite, JAC235Impl.class.getName(), serverProps, serverProps);

        TestUtils.addToSuite(suite, setup, OnlyPropertyTest.class);

        return setup;
    }

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
