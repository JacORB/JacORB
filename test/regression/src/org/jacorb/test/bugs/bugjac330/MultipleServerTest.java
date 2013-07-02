package org.jacorb.test.bugs.bugjac330;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.test.common.ServerSetup;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 */
public class MultipleServerTest extends TestCase
{
    private ServerSetup setup1;
    private String server1IOR;
    private ServerSetup setup2;
    private String server2IOR;
    private final List orbs = new ArrayList();

    protected void setUp() throws Exception
    {
        Properties props = new Properties();
        props.put(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");
        props.put(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        setup1 = new ServerSetup(this, BasicServerImpl.class.getName());
        setup1.setUp();
        server1IOR = setup1.getServerIOR();

        setup2 = new ServerSetup(this, BasicServerImpl.class.getName());
        setup2.setUp();
        server2IOR = setup2.getServerIOR();
    }

    protected void tearDown() throws Exception
    {
        setup2.tearDown();
        setup1.tearDown();

        Iterator i = orbs.iterator();
        while(i.hasNext())
        {
            ORBSetup setup = (ORBSetup) i.next();
            setup.tearDown();
        }

        orbs.clear();
    }

    private ORB newORB(Properties props) throws Exception
    {
    	ORBSetup setup = new ORBSetup(this, props);

    	setup.setUp();

        orbs.add(setup);

        return setup.getORB();
    }

    public void testAccessTwoServersAtOnceShouldFail() throws Exception
    {
        Properties props = new Properties();

        props.put("jacorb.connection.client.max_receptor_threads", "1");

        ORB orb = newORB(props);

        BasicServer server1 = BasicServerHelper.narrow(orb.string_to_object(server1IOR));
        assertEquals(10, server1.bounce_long(10));

        BasicServer server2 = BasicServerHelper.narrow(orb.string_to_object(server2IOR));

        try
        {
            server2.bounce_long(10);
            fail();
        }
        catch (NO_RESOURCES e)
        {
            // expected
        }
    }

    public void testAccessTwoServersOneByOne() throws Exception
    {
        Properties props = new Properties();

        props.put("jacorb.connection.client.max_receptor_threads", "1");

        ORB orb = newORB(props);

        BasicServer server1 = BasicServerHelper.narrow(orb.string_to_object(server1IOR));
        assertEquals(10, server1.bounce_long(10));
        server1._release();

        // give the ConsumerReceptorThread some time to finish its work
        Thread.sleep(1000);

        BasicServer server2 = BasicServerHelper.narrow(orb.string_to_object(server2IOR));
        assertEquals(10, server2.bounce_long(10));
        server2._release();
    }

    public void testAccessTwoServersAtOnceReleaseTryAgain() throws Exception
    {
        Properties props = new Properties();

        props.put("jacorb.connection.client.max_receptor_threads", "1");

        ORB orb = newORB(props);

        BasicServer server1 = BasicServerHelper.narrow(orb.string_to_object(server1IOR));
        assertEquals(10, server1.bounce_long(10));

        BasicServer server2 = BasicServerHelper.narrow(orb.string_to_object(server2IOR));

        try
        {
            server2.bounce_long(10);
            fail("should fail as there may not be more than 1 ClientReceptorThreads");
        }
        catch (NO_RESOURCES e)
        {
            // expected
        }

        server1._release();

        // give the ConsumerReceptorThread some time to finish its work
        Thread.sleep(1000);

        // retry bind
        assertEquals(10, server2.bounce_long(10));
    }

    public void testNoIdleThreads() throws Exception
    {
        Properties props = new Properties();

        props.put("jacorb.connection.client.max_receptor_threads", "1");
        props.put("jacorb.connection.client.max_idle_receptor_threads", "0");

        ORB orb = newORB(props);

        BasicServer server1 = BasicServerHelper.narrow(orb.string_to_object(server1IOR));
        assertEquals(10, server1.bounce_long(10));

        final String threadName = "ClientMessageReceptor";
        assertTrue(isThereAThreadNamed(threadName));

        server1._release();

        int retry = 0;
        final int maxRetry = 30;

        while( (retry++ < maxRetry) && isThereAThreadNamed(threadName))
        {
            // wait some time to allow the ClientMessageReceptor Thread to exit
            Thread.sleep(1000);
            System.gc();
        }

        dumpThread(threadName);

        assertFalse("there should be no idle thread", isThereAThreadNamed(threadName));
    }

    private void dumpThread(final String threadName) throws Exception
    {
        Method method;

        try
        {
            method = Thread.class.getMethod("getAllStackTraces", new Class[0]);
        }
        catch (NoSuchMethodException e)
        {
            // not a JDK 1.5
            return;
        }

        Map map = (Map) method.invoke(null, new Object[0]);

        Iterator i = map.keySet().iterator();

        while(i.hasNext())
        {
            Thread key = (Thread) i.next();

            if (!key.getName().startsWith(threadName))
            {
                continue;
            }

            StackTraceElement[] stack = (StackTraceElement[]) map.get(key);

            System.out.println(key.getName());
            for (int j = 0; j < stack.length; j++)
            {
                System.out.println("\t" + stack[j]);
            }
            System.out.println();
        }
    }

    private boolean isThereAThreadNamed(String name)
    {
        // begin hack.
        // fetch the names of all active threads and see if
        // the name matches.
        int threadCount = Thread.activeCount();
        Thread[] threads = new Thread[threadCount];

        Thread.enumerate(threads);

        for (int i = 0; i < threads.length; i++)
        {
            if (threads[i] == null)
            {
                continue;
            }

            if (threads[i].getName().indexOf(name) >= 0)
            {
                return true;
            }
        }
        return false;
    }
}
