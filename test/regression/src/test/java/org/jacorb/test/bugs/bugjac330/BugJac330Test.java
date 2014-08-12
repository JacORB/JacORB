package org.jacorb.test.bugs.bugjac330;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.NO_RESOURCES;

/**
 * @author Alphonse Bendt
 */
public class BugJac330Test extends ClientServerTestCase
{
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        final Properties clientProps = new Properties();
        final Properties serverProps = new Properties();

        clientProps.put("jacorb.connection.client.max_receptor_threads", "0");

        setup = new ClientServerSetup(BasicServerImpl.class.getName(), clientProps, serverProps);
    }

    @Test
    public void testConnectClient() throws Exception
    {
        final BasicServer server = BasicServerHelper.narrow(setup.getServerObject());
        final Exception[] exception = new Exception[1];
        final boolean[] success = new boolean[1];
        final int timeout = 60000;

        Runnable pingCommand = new Runnable()
        {
            public void run()
            {
                try
                {
                    server.ping();
                    success[0] = true;
                }
                catch (Exception e)
                {
                    exception[0] = e;
                }
            }
        };

        Thread thread = new Thread(pingCommand);

        thread.start();
        thread.join(timeout);

        assertTrue("ping command did not terminate within " + timeout + " ms", success[0] || exception[0] != null);
        assertNotNull("request should have failed", exception[0]);
        assertEquals(NO_RESOURCES.class, exception[0].getClass());
    }
}
