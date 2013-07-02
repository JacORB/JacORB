package org.jacorb.test.orb.policies;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.AMI_TimingServerHandler;
import org.jacorb.test.AMI_TimingServerHandlerOperations;
import org.jacorb.test.AMI_TimingServerHandlerPOATie;
import org.jacorb.test.EmptyException;
import org.jacorb.test.TimingServer;
import org.jacorb.test.TimingServerHelper;
import org.jacorb.test._TimingServerStub;
import org.jacorb.test.common.CallbackTestCase;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.util.Time;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.SetOverrideType;
import org.omg.Messaging.ExceptionHolder;
import org.omg.Messaging.RELATIVE_REQ_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REPLY_START_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * @author Andre Spiegel spiegel@gnu.org
 */
public class TimingTest extends CallbackTestCase
{
    private TimingServer server = null;

    public TimingTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = TimingServerHelper.narrow (setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    private class ReplyHandler
        extends CallbackTestCase.ReplyHandler
        implements AMI_TimingServerHandlerOperations
    {

        public void ex_op_excep(ExceptionHolder excep_holder)
        {
            wrong_exception ("ex_op_excep", excep_holder);
        }

        public void ex_op(char ami_return_val)
        {
            wrong_reply ("ex_op");
        }

        public void operation_excep(ExceptionHolder excep_holder)
        {
            wrong_exception ("operation_excep", excep_holder);
        }

        public void operation(int ami_return_val)
        {
            wrong_reply ("operation");
        }

        public void server_time_excep(ExceptionHolder excep_holder)
        {
            wrong_exception ("server_time_excep", excep_holder);
        }

        public void server_time(long ami_return_val)
        {
            wrong_reply ("server_time");
        }

    }

    private AMI_TimingServerHandler ref ( ReplyHandler handler )
    {
        AMI_TimingServerHandlerPOATie tie =
            new AMI_TimingServerHandlerPOATie( handler )
            {
                public org.omg.CORBA.portable.OutputStream
                    _invoke( String method,
                             org.omg.CORBA.portable.InputStream _input,
                             org.omg.CORBA.portable.ResponseHandler handler )
                    throws org.omg.CORBA.SystemException
                {
                    try
                    {
                        return super._invoke( method, _input, handler );
                    }
                    catch( AssertionFailedError e )
                    {
                        return null;
                    }
                }
            };
        return tie._this( setup.getClientOrb() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("Timing Policies");
        ClientServerSetup setup =
            new ClientServerSetup
                (suite,
                 "org.jacorb.test.orb.policies.TimingServerImpl");

        // some tests are disabled below (no test prefix)
        // because it is impossible
        // to make them succeed on a fast machine where the Java
        // clock has only millisecond resolution

        TestUtils.addToSuite(suite, setup, TimingTest.class);

        return setup;
    }

    /**
     * Do a few synchronous invocations as a sanity check
     * and to get all the necessary classes loaded.
     */
    public void test_sync_no_timing() throws Exception
    {
        int result = server.operation (1, 0);
        assertEquals (1, result);

        result = server.operation (2, 10);
        assertEquals (2, result);

        result = server.operation (3, 100);
        assertEquals (3, result);

        try
        {
            server.ex_op('e', 50);
            fail ("should have raised EmptyException");
        }
        catch (EmptyException ex)
        {
            // ok
        }

        try
        {
            server.ex_op('$', 50);
            fail ("should have raised DATA_CONVERSION");
        }
        catch (org.omg.CORBA.DATA_CONVERSION ex)
        {
            // ok
        }
    }

    /**
     * Do a few asynchronous invocations as a sanity check
     * and to get all the necessary classes loaded.
     */
    public void test_async_no_timing()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (7, ami_return_val);
                pass();
            }
        };
        ((_TimingServerStub)server).sendc_operation (ref (handler), 7, 50);
        handler.wait_for_reply(400);

        handler = new ReplyHandler()
        {
            public void ex_op_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (EmptyException.class,
                              getException (excep_holder).getClass());
                pass();
            }
        };
        ((_TimingServerStub)server).sendc_ex_op (ref (handler), 'e', 50);
        handler.wait_for_reply(400);

        handler = new ReplyHandler()
        {
            public void ex_op_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (org.omg.CORBA.DATA_CONVERSION.class,
                              getException (excep_holder).getClass());
                pass();
            }
        };
        ((_TimingServerStub)server).sendc_ex_op (ref (handler), '$', 50);
        handler.wait_for_reply(400);
    }

    /**
     * Set all timing policies to values that will be met by the invocation.
     */
    public void test_all_policies_sync_ok() throws Exception
    {
        server = clearPolicies (server);
        server = setRequestStartTime (server, System.currentTimeMillis());
        server = setRequestEndTime (server, System.currentTimeMillis() + 2000);
        server = setRelativeRequestTimeout (server, System.currentTimeMillis() + 3000);
        server = setReplyStartTime (server, System.currentTimeMillis());
        server = setReplyEndTime (server, System.currentTimeMillis() + 5000);
        server = setRelativeRoundtripTimeout(server, System.currentTimeMillis() + 6000);

        assertEquals(434, server.operation (434, 500));
    }


    /**
     * Sets a RequestStartTime which will already have expired
     * when the request arrives.
     */
    public void test_request_start_time_sync_expired()
    {
        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setRequestStartTime (server, start);
        server.server_time (10);

        long delta = System.currentTimeMillis() - start;
        if (delta > 200)
        {
            fail ("reply too late (" + delta + "ms)");
        }
    }

    /**
     * Sets a RequestStartTime which will not have been reached
     * when the request arrives.
     */
    public void test_request_start_time_sync_wait()
    {
        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setRequestStartTime (server, start + 200);
        long time = server.server_time (100);

        long delta = time - start;
        if (delta < 200)
        {
            fail ("request started too early (" + delta + "ms)");
        }
        else if (delta > 250)
        {
            fail ("request started too late (" + delta + "ms)");
        }
    }

    /**
     * Sets a RequestEndTime which will
     * be met by the invocation.
     */
    public void test_request_end_time_sync_ok() throws Exception
    {
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() + 400);
        server.operation (434, 500);
    }

    /**
     * Sets a RequestEndTime which will have expired prior
     * to the invocation.
     */
    public void test_request_end_time_sync_pre_expired()
    {
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() - 200);
        try
        {
            server.operation (121, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }

    /**
     * Sets a RequestEndTime which will have expired prior to the invocation.
     */
    public void test_request_end_time_async_pre_expired()
    {
        ReplyHandler handler = new ReplyHandler();

        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() - 5);
        try
        {
            ((_TimingServerStub)server).sendc_operation (ref (handler), 765, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT e)
        {
            // ok
        }
    }

    /**
     * Sets a RequestEndTime which will expire during the invocation.
     */
    public void test_request_end_time_sync_expired() throws Exception
    {
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() - 200);
        try
        {
            server.operation (121, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }


    /**
     * Sets a RelativeRequestTimeout which will
     * be met by the invocation.
     */
    public void test_request_timeout_sync_ok()
    {
        server = clearPolicies (server);
        server = setRelativeRequestTimeout (server, 200);
        try
        {
            server.operation (434, 300);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("should not have been a TIMEOUT");
        }
    }

    /**
     * Sets a RelativeRequestTimeout which will expire during the invocation.
     */
    public void _test_request_timeout_sync_expired()
    {
        server = clearPolicies (server);
        server = setRelativeRequestTimeout (server, 1);

        try
        {
            server.operation (121, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }

    /**
     * Sets a RelativeRequestTimeout which will
     * expire during the invocation.
     */
    public void _test_request_timeout_async_expired()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.fail ("should have raised TIMEOUT");
            }

            public void operation_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (org.omg.CORBA.TIMEOUT.class,
                              getException (excep_holder).getClass());
                pass();
            }
        };

        server = clearPolicies (server);
        server = setRelativeRequestTimeout (server, -100);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 767, 200);
        handler.wait_for_reply (400);
    }

    /**
     * Sets a ReplyStartTime which will already have expired
     * when the reply arrives.
     */
    public void test_reply_start_time_sync_expired()
    {
        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setReplyStartTime (server, start);
        server.operation (18, 10);

        long delta = System.currentTimeMillis() - start;
        if (delta > 200)
        {
            fail ("reply too late (" + delta + "ms)");
        }
    }

    /**
     * Sets a ReplyStartTime which will not have been reached
     * when the reply arrives.
     */
    public void test_reply_start_time_sync_wait()
    {
        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setReplyStartTime (server, start + 2000);
        int result = server.operation (18, 1000);
        assertEquals (18, result);

        long delta = System.currentTimeMillis() - start;
        if (delta < 2000)
        {
            fail ("reply too early (" + delta + "ms)");
        }
        else if (delta > 2500)
        {
            fail ("reply too late (" + delta + "ms)");
        }
    }

    /**
     * Sets a ReplyStartTime which will already have expired
     * when the reply arrives.
     */
    public void test_reply_start_time_async_expired()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (19, ami_return_val);
                pass();
            }
        };

        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setReplyStartTime (server, start);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 19, 500);
        handler.wait_for_reply (1000);
        long delta = System.currentTimeMillis() - start;
        if (delta > 2000)
        {
            fail ("reply too late (" + delta + "ms)");
        }
    }

    /**
     * Sets a ReplyStartTime which will not have been reached
     * when the reply arrives.
     */
    public void test_reply_start_time_async_wait()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (19, ami_return_val);
                pass();
            }
        };

        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setReplyStartTime (server, start + 200);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 19, 100);
        handler.wait_for_reply (250);

        long delta = System.currentTimeMillis() - start;
        if (delta < 200)
        {
            fail ("reply too early (" + delta + "ms)");
        }
    }

    /**
     * Sets a ReplyEndTime which will
     * be met by the invocation.
     */
    public void test_reply_end_time_async_ok()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (765, ami_return_val);
                pass();
            }
        };

        server = clearPolicies (server);
        server = setReplyEndTime (server, System.currentTimeMillis() + 500);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 765, 50);
        handler.wait_for_reply (450);
    }


    /**
     * Sets a ReplyEndTime which will
     * expire during the invocation.
     */
    public void test_reply_end_time_async_expired()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.fail ("should have raised TIMEOUT");
            }

            public void operation_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (org.omg.CORBA.TIMEOUT.class,
                              getException (excep_holder).getClass());
                pass();
            }
        };

        server = clearPolicies (server);
        server = setReplyEndTime (server, System.currentTimeMillis() + 1000);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 767, 2000);
        handler.wait_for_reply (4000);
    }

    /**
     * Sets a ReplyEndTime which will
     * be met by the invocation.
     */
    public void test_reply_end_time_sync_ok()
    {
        server = clearPolicies (server);
        server = setReplyEndTime (server, System.currentTimeMillis() + 2000);
        server.operation (434, 500);
    }

    /**
     * Sets a ReplyEndTime which has expired prior to invocation.
     */
    public void test_reply_end_time_sync_pre_expired()
    {
        server = clearPolicies (server);
        server = setReplyEndTime (server, System.currentTimeMillis() - 100);
        try
        {
            server.operation (44, 100);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }

    /**
     * Sets a ReplyEndTime which will
     * expire during invocation.
     */
    public void test_reply_end_time_sync_expired()
    {
        server = clearPolicies (server);
        server = setReplyEndTime (server, System.currentTimeMillis() + 200);
        try
        {
            server.operation (343, 300);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }


    /**
     * Sets a RelativeRoundtripTimeout which will
     * be met by the invocation.
     */
    public void test_relative_roundtrip_sync_ok()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);
        server.operation (434, 50);
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * expire during invocation.
     */
    public void test_relative_roundtrip_sync_expired()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);
        try
        {
            server.operation (343, 300);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * be met by the invocation.
     */
    public void test_relative_roundtrip_async_ok()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (765, ami_return_val);
                pass();
            }
        };

        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 765, 50);
        handler.wait_for_reply (150);
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * expire during the invocation.
     */
    public void test_relative_roundtrip_async_expired()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.fail ("should have raised TIMEOUT");
            }

            public void operation_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (org.omg.CORBA.TIMEOUT.class,
                              getException (excep_holder).getClass());
                pass();
            }
        };

        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 50);
        ((_TimingServerStub)server).sendc_operation (ref (handler), 767, 100);
        handler.wait_for_reply (400);
    }

    /**
     * Test multiple combined policies.
     * Sets a Request- and ReplyStartTime which will not have been
     * reached. Server needs to wait for both policies.
     */
    public void test_request_reply_start_time_sync_wait()
    {
        server = clearPolicies (server);
        long start = System.currentTimeMillis();
        server = setRequestStartTime(server, start + 2000);
        server = setReplyStartTime(server, start + 6000);
        long serverStart = server.server_time(2000);
        long rtTime = System.currentTimeMillis() - start;

        //System.err.println("Server started after: " + (serverStart-start));
        //System.err.println("Relpy returned after: " + (rtTime));

        // check server starts immediately
        assertTrue(serverStart >= start + 2000);
        assertTrue(serverStart <= start  + 2000 + 500); // 500 ms latency

        // check roundtrip time takes 1000 ms
        assertTrue(rtTime >= 6000);
        assertTrue(rtTime <= 6000 + 500); // 500 ms latency
    }


    // convenience methods for policy manipulation

    // These methods create policies in the really cumbersome way
    // via the ORB, so that the mechanism gets tested.  Each of the
    // policy types in org.jacorb.orb.policies also has a convenience
    // constructor that makes it much easier.

    private TimingServer clearPolicies (TimingServer server)
    {
        org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{}, SetOverrideType.SET_OVERRIDE);
        server._release();
        return TimingServerHelper.narrow (r);
    }

    private TimingServer setRequestStartTime (TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime (unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert (any, corbaTime);
        try
        {
            Policy policy =
                orb.create_policy (REQUEST_START_TIME_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private TimingServer setRequestEndTime (TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime (unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert (any, corbaTime);
        try
        {
            Policy policy =
                orb.create_policy (REQUEST_END_TIME_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private TimingServer setRelativeRequestTimeout (TimingServer server,
                                              long millis)
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        any.insert_ulonglong (millis * 10000);
        try
        {
            Policy policy =
                orb.create_policy (RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private TimingServer setReplyStartTime (TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime (unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert (any, corbaTime);
        try
        {
            Policy policy =
                orb.create_policy (REPLY_START_TIME_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private TimingServer setReplyEndTime (TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime (unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert (any, corbaTime);
        try
        {
            Policy policy =
                orb.create_policy (REPLY_END_TIME_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private TimingServer setRelativeRoundtripTimeout (TimingServer server,
                                              long millis)
    {
        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        any.insert_ulonglong (millis * 10000);
        try
        {
            Policy policy =
                orb.create_policy (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value, any);
            org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{ policy },
                                         SetOverrideType.ADD_OVERRIDE);
            server._release();
            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }
}
