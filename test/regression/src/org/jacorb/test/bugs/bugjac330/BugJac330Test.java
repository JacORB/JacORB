package org.jacorb.test.bugs.bugjac330;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.NO_RESOURCES;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class BugJac330Test extends ClientServerTestCase
{
    public BugJac330Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        final Properties clientProps = new Properties();
        final Properties serverProps = new Properties();

        clientProps.put("jacorb.connection.client.max_receptor_threads", "0");

        ClientServerSetup setup = new ClientServerSetup(suite, BasicServerImpl.class.getName(), clientProps, serverProps);

        TestUtils.addToSuite(suite, setup, BugJac330Test.class);

        return setup;
    }

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
        assertEquals(NO_RESOURCES.class, exception[0].getClass());
    }
}
