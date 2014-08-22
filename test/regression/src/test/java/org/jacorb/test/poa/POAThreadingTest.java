package org.jacorb.test.poa;

import static org.junit.Assert.fail;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class POAThreadingTest extends ClientServerTestCase
{
    private MyServer server;

    @Before
    public void setUp() throws Exception
    {
        server = MyServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testRequestThreading() throws Exception
    {
        Thread thread1 = new Thread("Block1")
        {
            public void run()
            {
                try
                {
                    server.block();
                }
                catch (Exception e)
                {
                }
            }
        };
        Thread thread2 = new Thread("Block2")
        {
            public void run()
            {
                try
                {
                    server.block();
                }
                catch (Exception e)
                {
                }
            }
        };

        thread1.start();
        thread2.start();
        Thread.sleep(1000);

        Callable<Boolean> myTestCaller = new TestCaller (server);
        FutureTask<Boolean> task = new FutureTask<Boolean>(myTestCaller);
        Thread t3 = new Thread (task);
        t3.start();

        try
        {
            task.get(10000, TimeUnit.MILLISECONDS);
            fail ("Expected a timeout");
        }
        catch (TimeoutException e)
        {
            fail ("Did not get a result from the testcall in the expected time.");
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof org.omg.CORBA.TIMEOUT)
            {
                TestUtils.getLogger().debug ("Got a timeout (" + e.getCause() + ")"); // pass
            }
            else
            {
                fail ("Unexpected exception " + e.getCause());
            }
        }

        thread1.join();
        thread2.join();
    }


    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.poa.thread_pool_min", "1");
        serverProps.setProperty("jacorb.poa.thread_pool_max", "2");
        serverProps.setProperty("jacorb.poa.threadtimeout", "5000");

        setup = new ClientServerSetup( ServerImpl.class.getName(), null, serverProps);

    }


    public class TestCaller implements Callable<Boolean>
    {
        private MyServer server;

        TestCaller (MyServer server)
        {
            this.server = server;
        }


        public Boolean call() throws Exception
        {
            return server.testCall();
        }
    }
}
