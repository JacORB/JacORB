package org.jacorb.test.bugs.bugjac774;

import java.util.Properties;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This test checks base BufferManager functionality.
 * It is trying to get memory buffer with the maximum allowed
 * size and checks how BufferManager handles case when server
 * run out of memory. Successfully got memory buffers and size
 * that asked but is too big are printed into standard output
 * and could be checked during logs review.
 *
 * @author A.Birchenko
 *
 */

public class BugJac774Test extends ClientServerTestCase
{
    // -Xmx values below are experimentally found for the
    // mentioned platforms. Most *nix systems allows to use
    // -Xmx up to 2500 MB but for WIN23 is only 1500 MB
    private static final int WIN32_XMX = 1500;
    private static final int XMX = 1800;

    private MyServer server;

    public BugJac774Test (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = MyServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testBugjac774()
    {
        for(int i = 1; i <= 10; i++)
        {
            int size = (Integer.MAX_VALUE/10) * i;

            switch (server.testBuffer(size))
            {
                case 0: // Pass
                {
                    System.out.println("Buffer allocated: " + size/1048576 + "Mb");
                    break;
                }
                case 1: // OutOfMemory
                {
                    System.out.println("Buffer unallocated: " + size/1048576 + "Mb");
                    return;
                }
                case -1: // Unexpected exception
                {
                    fail("Unexpected exception during buffer allocation. See the server output for the details.");
                }
            }
        }
    }

    private static int getXmx()
    {
        if(System.getProperty("sun.arch.data.model").equals("32") &&
           System.getProperty("os.name").startsWith("Windows"))
        {
            return WIN32_XMX;
        }

        return XMX;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.test.maxheapsize", getXmx()+"m");

        ClientServerSetup setup =
        new ClientServerSetup( suite, ServerImpl.class.getName(), serverProps, serverProps);

        TestUtils.addToSuite(suite, setup, BugJac774Test.class);

        return setup;
    }
}
