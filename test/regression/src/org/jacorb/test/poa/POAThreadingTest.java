package org.jacorb.test.poa;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;



public class POAThreadingTest extends ClientServerTestCase
{
    private MyServer server;

    public POAThreadingTest (String name, ClientServerSetup setup)
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

    public void testRequestThreading() throws Exception
    {
        Boolean result = null;
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
                    e.printStackTrace();
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
                    e.printStackTrace();
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
            boolean r = task.get(10000, TimeUnit.MILLISECONDS);
            // System.out.println ("### Result is " + r);
            fail ("Expected a timeout");
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
            fail ("Did not get a result from the testcall in the expected time.");
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof org.omg.CORBA.TIMEOUT)
            {
                System.out.println ("Got a timeout (" + e.getCause() + ")"); // pass
            }
            else
            {
                fail ("Unexpected exception " + e.getCause());
            }
        }

        thread1.join();
        thread2.join();
    }


    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.poa.thread_pool_min", "1");
        serverProps.setProperty("jacorb.poa.thread_pool_max", "2");
        serverProps.setProperty("jacorb.poa.threadtimeout", "5000");

        ClientServerSetup setup = new ClientServerSetup
            ( suite, ServerImpl.class.getName(), null, serverProps);

        TestUtils.addToSuite(suite, setup, POAThreadingTest.class);

        return setup;
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
