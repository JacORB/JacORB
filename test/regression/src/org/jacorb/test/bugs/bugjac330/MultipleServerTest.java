package org.jacorb.test.bugs.bugjac330;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.ORB;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class MultipleServerTest extends TestCase
{
    private ClientServerSetup setup1;
    private String server1IOR;
    private ClientServerSetup setup2;
    private String server2IOR;
    private ORB orb;

    protected void setUp() throws Exception
    {
        // this is a hack. we need two server objects for this test.
        // as ClientServerTestCase
        // does not support that, we create two ClientServerSetup's here and
        // invoke their lifecyle methods explicitely.
        TestSuite dummySuite = new TestSuite();

        Properties props = new Properties();
        props.put(ClientServerSetup.JACORB_REGRESSION_DISABLE_IMR, "true");
        props.put(ClientServerSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        setup1 = new ClientServerSetup(dummySuite, BasicServerImpl.class.getName(), props, null);
        setup1.setUp();
        server1IOR = setup1.getClientOrb().object_to_string(setup1.getServerObject());

        setup2 = new ClientServerSetup(dummySuite, BasicServerImpl.class.getName(), props, null);
        setup2.setUp();
        server2IOR = setup2.getClientOrb().object_to_string(setup2.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        setup2.tearDown();
        setup1.tearDown();
        if (orb != null)
        {
            orb.shutdown(true);
        }
    }

    private ORB newORB(Properties props)
    {
        orb = ORB.init(new String[0], props);

        return orb;
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
