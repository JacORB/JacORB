package org.jacorb.test.bugs.bug940;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jacorb.test.TimingServer;
import org.jacorb.test.TimingServerHelper;
import org.jacorb.test.harness.CallbackTestCase;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.util.Time;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.omg.CORBA.InvalidPolicies;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.PolicyManagerHelper;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA.TIMEOUT;
import org.omg.Messaging.RELATIVE_REQ_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import static org.junit.Assert.assertTrue;

/**
 * Try to validate whether calling another target within an interceptor messes up the Delegate timing
 * policies.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Bug940Test extends CallbackTestCase
{
    private TimingServer server;
    private ExecutorService pool;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties props = new Properties();

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                + "ORBInit", Initializer.class.getName());

        setup = new ClientServerSetup
                (
                        "org.jacorb.test.orb.policies.TimingServerImpl",
                        props, props
                );
    }

    private static class ServerCallable implements Callable
    {
        TimingServer ts;
        int delay;

        private ServerCallable(TimingServer ts, int delay)
        {
            this.ts = ts;
            this.delay = delay;
        }

        public Long call()
        {
            return ts.server_time(delay);
        }
    }

    @Before
    public void setUp() throws Exception
    {
        server = TimingServerHelper.narrow(setup.getServerObject());

        pool = Executors.newFixedThreadPool(3);
    }

    @After
    public void tearDown() throws Exception
    {
        pool.shutdown();
    }

    /**
     * Sets a ReplyEndTime and use an interceptor calling a different object
     */
    @Test
    public void test_reply_end_time_sync_ok()
    {
        server = clearPolicies(server);

        Initializer.ci.setTimingServer(server);

        server = setReplyEndTime(server, System.currentTimeMillis() + 2000);

        server.operation(434, 500);
    }

    /**
     * Set a ReplyTime and have an interceptor call the same object
     */
    @Test
    public void test_reply_end_time_sync_ok_interceptor_same_object() throws Exception
    {
        server = clearPolicies(server);
        server = setReplyEndTime(server, System.currentTimeMillis() + 2000);

        Initializer.ci.setTimingServer(server);

        server.operation(434, 500);
    }

    /**
     * Set a ReplyTime and have an interceptor call a different object with its own timeout
     * on two different threads at once.
     */
    @Test
    public void test_reply_end_time_sync_ok_interceptor_thread() throws Exception
    {
        server = clearPolicies(server);

        setGlobalRelativeRequestTimeout(900);

        Initializer.ci.setTimingServer(server);

        Future<Long> t1 = pool.submit(new ServerCallable(server, 100));
        Future<Long> t2 = pool.submit(new ServerCallable(server, 2000));
        Future<Long> t3 = pool.submit(new ServerCallable(server, 200));

        t1.get();
        t3.get();
        try
        {
            t2.get();
        }
        catch (ExecutionException e)
        {
            // Pass
            assertTrue(e.getCause() instanceof TIMEOUT);
        }
    }

    /**
     * Set a global Request timeout and have an interceptor call the same object
     */
    @Test
    public void test_x_timeout_with_interceptor() throws Exception
    {
        server = clearPolicies(server);

        setGlobalRelativeRequestTimeout(900);

        Initializer.ci.setTimingServer(server);

        server.operation(434, 500);

        TestUtils.getLogger().debug("Sleeping before invoking next call");
        Thread.sleep(5000);

        Initializer.ci.setInInterceptor(true);
        server.server_time(500);
    }

    private TimingServer clearPolicies(TimingServer server)
    {
        org.omg.CORBA.Object r = server._set_policy_override(new Policy[] { }, SetOverrideType.SET_OVERRIDE);
        server._release();
        return TimingServerHelper.narrow(r);
    }

    static TimingServer setReplyEndTime(TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime(unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert(any, corbaTime);
        try
        {
            Policy policy =
                    orb.create_policy(REPLY_END_TIME_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override(new Policy[] { policy },
                    SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow(r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException("policy error: " + e);
        }
    }

    private void setGlobalRelativeRequestTimeout(long millis)
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        any.insert_ulonglong(millis * 10000);

        PolicyManager orbManager;
        try
        {
            orbManager = PolicyManagerHelper.narrow
                    (orb.resolve_initial_references("ORBPolicyManager"));
            Policy policy1 =
                    orb.create_policy(RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value, any);
            Policy policy2 =
                    orb.create_policy(RELATIVE_RT_TIMEOUT_POLICY_TYPE.value, any);

            orbManager.set_policy_overrides(new Policy[] { policy1, policy2 }, SetOverrideType.SET_OVERRIDE);

        }
        catch (InvalidName e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (PolicyError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidPolicies e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
