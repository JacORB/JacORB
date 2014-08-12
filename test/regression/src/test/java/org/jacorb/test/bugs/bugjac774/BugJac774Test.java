package org.jacorb.test.bugs.bugjac774;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static final int XMX = 1500;

    private MyServer server;


    @Before
    public void setUp() throws Exception
    {
        server = MyServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testSimpleBuffer()
    {
        for(int i = 1; i <= 10; i++)
        {
            int size = (Integer.MAX_VALUE/10) * i;

            switch (server.testBuffer(size))
            {
                case 0: // Pass
                {
                    TestUtils.getLogger().debug("Buffer allocated: " + size/1048576 + "Mb");
                    break;
                }
                case 1: // OutOfMemory
                {
                    TestUtils.getLogger().debug("Buffer unallocated: " + size/1048576 + "Mb");
                    return;
                }
                case -1: // Unexpected exception
                {
                    fail("Unexpected exception during buffer allocation. See the server output for the details.");
                }
            }
        }
    }

    @Test
    public void testExpandedBuffer()
    {
        // check small buffers
        for(int i = 1; i <= 10; i++)
        {
            int size = 1024 * i;

            int returnedSize = server.testExpandedBuffer(size);

            if (returnedSize < 0)
            {
                fail("Unexpected exception during buffer allocation. See the server output for the details.");
            }

            TestUtils.getLogger().debug("Buffer requested: " + size + " allocated: " + returnedSize + "B");
        }

        // check medium buffers
        for(int i = 1; i <= 100; i++)
        {
            int size = 1024 * 1024 * i;

            int returnedSize = server.testExpandedBuffer(size);

            if (returnedSize < 0)
            {
                fail("Unexpected exception during buffer allocation. See the server output for the details.");
            }

            TestUtils.getLogger().debug("Buffer requested: " + size / 1024 + " allocated: " + returnedSize / 1024 + "kB");
        }

        for(int i = 1; i <= 10; i++)
        {
            int size = 50 * 1024 * 1024 * i;

            int returnedSize = server.testExpandedBuffer(size);

            if (returnedSize < 0)
            {
                fail("Unexpected exception during buffer allocation. See the server output for the details.");
            }

            TestUtils.getLogger().debug("Buffer requested: " + size / 1024 / 1024  + " allocated: " + returnedSize / 1024 / 1024 + "MB");
        }
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.test.maxheapsize", XMX+"m");

    setup = new ClientServerSetup( ServerImpl.class.getName(), serverProps, serverProps);
    }
}
