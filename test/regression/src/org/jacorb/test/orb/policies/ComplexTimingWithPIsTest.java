package org.jacorb.test.orb.policies;

import java.util.Properties;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.AMI_ComplexTimingServerHandler;
import org.jacorb.test.AMI_ComplexTimingServerHandlerOperations;
import org.jacorb.test.AMI_ComplexTimingServerHandlerPOATie;
import org.jacorb.test.ComplexTimingServer;
import org.jacorb.test.ComplexTimingServerHelper;
import org.jacorb.test.EmptyException;
import org.jacorb.test._ComplexTimingServerStub;
import org.jacorb.test.common.CallbackTestCase;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ServerSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.util.Time;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.SetOverrideType;
import org.omg.Messaging.ExceptionHolder;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 *
 */
public class ComplexTimingWithPIsTest extends CallbackTestCase
{
    private ComplexTimingServer server = null;

    private ComplexTimingServer fwdServer = null;

    private ServerSetup serverSetUp;

    public ComplexTimingWithPIsTest (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    protected void setUp() throws Exception
    {
        server = (ComplexTimingServer) ComplexTimingServerHelper.narrow (setup.getServerObject());

        org.omg.CORBA.ORB orb = setup.getClientOrb();

        Properties serverprops = new java.util.Properties();
        serverprops.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                 + ServerPIInitializer.class.getName(), "" );


        serverSetUp = new ServerSetup (setup,
                                       null,
                                       "org.jacorb.test.orb.policies.ComplexTimingServerImpl",
                                       serverprops);

        serverSetUp.setUp();

        fwdServer = (ComplexTimingServer)
           ComplexTimingServerHelper.narrow (orb.string_to_object (serverSetUp.getServerIOR()));

        /**
         * Perform an initial call to both servers as the initial connection
         * will take longer and would affect the timings and so cause failures
         * on slower machines.  This way the actual timed test is not affected
         * by any initial connection overheads
         */
        server.operation (999, 0);
        fwdServer.operation (666, 0);

    }

    protected void tearDown() throws Exception
    {
        server = null;
        fwdServer = null;
        serverSetUp.tearDown();
    }

    private class ReplyHandler
        extends CallbackTestCase.ReplyHandler
        implements AMI_ComplexTimingServerHandlerOperations
    {

        public void ex_op_excep (ExceptionHolder excep_holder)
        {
            wrong_exception ("ex_op_excep", excep_holder);
        }

        public void ex_op (char ami_return_val)
        {
            wrong_reply ("ex_op");
        }

        public void operation_excep (ExceptionHolder excep_holder)
        {
            wrong_exception ("operation_excep", excep_holder);
        }

        public void operation (int ami_return_val)
        {
            wrong_reply ("operation");
        }

        public void forwardOperation_excep (ExceptionHolder excep_holder)
        {
            wrong_exception ("forwardOperation_excep", excep_holder);
        }

        public void forwardOperation (int ami_return_val)
        {
            wrong_reply ("forwardOperation");
        }

        public void server_time_excep (ExceptionHolder excep_holder)
        {
            wrong_exception ("server_time_excep", excep_holder);
        }

        public void server_time (long ami_return_val)
        {
            wrong_reply ("server_time");
        }

        public void setServerConfig_excep (ExceptionHolder excep_holder)
        {
            wrong_exception ("setServerConfig_excep", excep_holder);
        }

        public void setServerConfig ()
        {
            wrong_reply ("setServerConfig");
        }
    }

    private AMI_ComplexTimingServerHandler ref (ReplyHandler handler)
    {
        AMI_ComplexTimingServerHandlerPOATie tie =
            new AMI_ComplexTimingServerHandlerPOATie (handler)
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
        TestSuite suite = new TestSuite ("Complex Timing Test with Portable Interceptors");

        Properties clientprops = new java.util.Properties();
        clientprops.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                 + ClientPIInitializer.class.getName(), "" );

        Properties serverprops = new java.util.Properties();
        serverprops.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass."
                                 + ServerPIInitializer.class.getName(), "" );
        ClientServerSetup setup =
            new ClientServerSetup
                (suite,
                 "org.jacorb.test.orb.policies.ComplexTimingServerImpl",
                 clientprops,
                 serverprops
                );

        TestUtils.addToSuite (suite, setup, ComplexTimingWithPIsTest.class);

        return setup;
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * be met by the invocation.
     */
    public void test_relative_roundtrip_sync_ok()
    {
        server.operation (999, 0);

        try
        {
            server.operation (434, 50);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("Unexpected timeout");
        }
    }

    public void test_relative_roundtrip_fwdreq_at_send_request()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (TestConfig.SEND_REQ,
                              fwdServer);

        try
        {
            server.operation (434, 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }

        /**
         * clear the configuration and call on the server
         * again to ensure that the TIMEOUT does not occur
         * on subsequent calls
         */
        TestConfig.setConfig (0,
                              null);

        try
        {
            server.operation (434, 50);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("Unexpected TIMEOUT");
        }

    }

    public void test_relative_roundtrip_fwdreq_at_rrsc()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (0,
                              null);

        server.setServerConfig (TestConfig.RRSC,
                                fwdServer);

        try
        {
            server.operation (434, 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
    }

    public void test_relative_roundtrip_fwdreq_at_receive_req()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (0,
                              null);

        server.setServerConfig (TestConfig.REC_REQ,
                                fwdServer);

        try
        {
            server.operation (434, 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }

    }

    public void test_relative_roundtrip_fwdreq_at_send_ex()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (0,
                              null);

        server.setServerConfig (TestConfig.SEND_EX,
                                fwdServer);

        try
        {
            server.ex_op ('e', 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
        catch (EmptyException ee)
        {
            fail ("Expected a TIMEOUT and got EmptyException");
        }
    }

    public void test_relative_roundtrip_fwdreq_at_receive_other()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 150);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (0,
                              null);

        server.setServerConfig (TestConfig.SEND_EX,
                                fwdServer);

        TestConfig.setConfig (TestConfig.REC_OTHER,
                              fwdServer);

        try
        {
            server.ex_op ('e', 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
        catch (EmptyException ee)
        {
            fail ("Expected a TIMEOUT and got EmptyException");
        }
    }

    public void test_relative_roundtrip_fwdreq_at_receive_ex()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 150);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (TestConfig.REC_EX,
                              fwdServer);

        server.setServerConfig (0,
                                null);

        try
        {
            server.ex_op ('e', 50);
            fail ("should have raised TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }
        catch (EmptyException ee)
        {
            fail ("Expected a TIMEOUT and got EmptyException");
        }
    }

    /**
     * Sets a RelativeRoundtripTimeout which will
     * expire during invocation.
     */
    public void test_relative_roundtrip_sync_expired()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);

        server.setServerConfig (0,
                                null);
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
               pass ();
            }
        };

        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 200);
        ((_ComplexTimingServerStub)server).sendc_operation (ref (handler), 765, 50);
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

        ((_ComplexTimingServerStub)server).sendc_operation (ref (handler),
                                                     767,
                                                     100);
        handler.wait_for_reply (400);

    }

    /**
     * Sets a RequestEndTime which will
     * be met by the invocation.when a ForwardRequest is thrown by the
     * client interceptor at SEND_REQUEST point.
     */
    public void test_request_end_time_fwdreq_at_send_request () throws Exception
    {
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() + 400);

        TestConfig.setConfig (TestConfig.SEND_REQ,
                              fwdServer);

        try
        {
            server.operation (434, 500);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("Unexpected TIMEOUT");
        }
    }

    /**
     * Sets a RequestEndTime which will have expired prior
     * to the invocation.after a ForwardRequest is thrown by the client
     * interceptor at the SEND_REQUEST point
     */
    public void test_request_end_time_fwdreq_at_send_request_expired()
    {
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() + 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (TestConfig.SEND_REQ,
                              fwdServer);
        try
        {
            server.operation (121, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // ok
        }


        /**
         * We need to reset the policy as we used the current time when
         * setting the timeout policy and that would mean the subsequent
         * call would also get a timeout because the time has already
         * expired
         */
        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() + 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (0,
                              null);

        /**
         * Test that the timeout has been cleared and subsequent
         * calls do not receive a TIMEOUT
         */
        try
        {
            server.operation (434, 500);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("Unexpected TIMEOUT");
        }
    }

    /**
     * Sets a RequestEndTime which will have expired prior to the invocation.
     * after a ForwardRequest is thrown by the client
     * interceptor at the SEND_REQUEST point
     */
    public void test_request_end_time_async_pre_expired()
    {
        ReplyHandler handler = new ReplyHandler();

        server = clearPolicies (server);
        server = setRequestEndTime (server, System.currentTimeMillis() + 200);

        ClientInterceptor.forwardRequestThrown = false;

        TestConfig.setConfig (TestConfig.SEND_REQ,
                              fwdServer);

        try
        {
            ((_ComplexTimingServerStub)server).sendc_operation (ref (handler), 765, 50);
            fail ("should have been a TIMEOUT");
        }
        catch (org.omg.CORBA.TIMEOUT e)
        {
            // ok
        }
    }

    public void test_relative_roundtrip_fwdcall_at_send_request_OK()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 300);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_SEND_REQ,
                              fwdServer);

        try
        {
            server.operation (434, 50);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            fail ("FAIL TIMEOUT not expected");
        }
    }

    public void test_relative_roundtrip_fwdcall_at_send_request_exp()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 100);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_SEND_REQ,
                              fwdServer);

        try
        {
            server.operation (434, 50);
            fail ("test_relative_roundtrip_fwdcall_at_send_request_exp - TIMEOUT expected");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // OK
        }
    }

    public void test_relative_roundtrip_fwdcall_at_send_request_exp2()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 400);

        fwdServer = clearPolicies (fwdServer);
        fwdServer = setRelativeRoundtripTimeout (fwdServer, 10);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_SEND_REQ,
                              fwdServer);

        try
        {
            server.operation (434, 50);
            fail ("test_relative_roundtrip_fwdcall_at_send_request_exp2 - TIMEOUT expected");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // OK
        }
    }

    public void test_relative_roundtrip_fwdcall_at_rec_exc_OK()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 400);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_REC_EX,
                              fwdServer);

        try
        {
            server.ex_op ('e', 50);
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
           fail ("FAIL TIMEOUT not expected");
        }
        catch (EmptyException ee)
        {
            // OK
        }
    }

    public void test_relative_roundtrip_fwdcall_at_rec_exc_exp()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 100);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_REC_EX,
                              fwdServer);

        try
        {
            server.ex_op ('e', 50);
            fail ("test_relative_roundtrip_fwdcall_at_receive_exc_exp - TIMEOUT expected");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // OK
        }
        catch (EmptyException ee)
        {
            fail ("Expected a TIMEOUT and got EmptyException");
        }
    }

    public void test_relative_roundtrip_fwdcall_at_receive_exc_exp2()
    {
        server = clearPolicies (server);
        server = setRelativeRoundtripTimeout (server, 400);

        fwdServer = clearPolicies (fwdServer);
        fwdServer = setRelativeRoundtripTimeout (fwdServer, 10);

        ClientInterceptor.forwardCallMade = false;

        TestConfig.setConfig (TestConfig.CALL_AT_REC_EX,
                              fwdServer);

        try
        {
            server.ex_op ('e', 50);
            fail ("test_relative_roundtrip_fwdcall_at_receive_exc_exp2 - TIMEOUT expected");
        }
        catch (org.omg.CORBA.TIMEOUT t)
        {
            // OK
        }
        catch (EmptyException ee)
        {
            fail ("Expected a TIMEOUT and got EmptyException");
        }
    }

    // convenience methods for policy manipulation

    // These methods create policies in the really cumbersome way
    // via the ORB, so that the mechanism gets tested.  Each of the
    // policy types in org.jacorb.orb.policies also has a convenience
    // constructor that makes it much easier.

    private ComplexTimingServer clearPolicies (ComplexTimingServer server)
    {
        org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{},
                                                              SetOverrideType.SET_OVERRIDE);

        return ComplexTimingServerHelper.narrow (r);
    }

    private ComplexTimingServer setRelativeRoundtripTimeout (ComplexTimingServer server,
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

            return ComplexTimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }

    private ComplexTimingServer setRequestEndTime (ComplexTimingServer server,
                                                   long unixTime)
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
            return ComplexTimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
        }
    }
}
