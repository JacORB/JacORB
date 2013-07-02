package org.jacorb.test.bugs.bugjac493;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.AMI_TimingServerHandler;
import org.jacorb.test.AMI_TimingServerHandlerOperations;
import org.jacorb.test.AMI_TimingServerHandlerPOATie;
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
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.Messaging.ExceptionHolder;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
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


    // convenience methods for policy manipulation

    // These methods create policies in the really cumbersome way
    // via the ORB, so that the mechanism gets tested.  Each of the
    // policy types in org.jacorb.orb.policies also has a convenience
    // constructor that makes it much easier.
    private TimingServer clearPolicies (TimingServer server)
    {
        org.omg.CORBA.Object r = server._set_policy_override (new Policy[]{}, SetOverrideType.SET_OVERRIDE);
        return TimingServerHelper.narrow (r);
    }


    private TimingServer setReplyEndTime (TimingServer server, long unixTime)
    {
        UtcT corbaTime = Time.corbaTime (unixTime);

        org.omg.CORBA.ORB orb = setup.getClientOrb();
        org.omg.CORBA.Any any = orb.create_any();
        UtcTHelper.insert (any, corbaTime);
        try
        {
            Policy policy = orb.create_policy (REPLY_END_TIME_POLICY_TYPE.value, any);

            org.omg.CORBA.Object r = server._set_policy_override
                (new Policy[]{ policy }, SetOverrideType.ADD_OVERRIDE);

            return TimingServerHelper.narrow (r);
        }
        catch (PolicyError e)
        {
            throw new RuntimeException ("policy error: " + e);
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
                 TimingServerImpl.class.getName());

        TestUtils.addToSuite(suite, setup, TimingTest.class);

        return setup;
    }


    public void testDuplicateObject1 ()
    {
       String ior1 = setup.getClientOrb().object_to_string (server);
       TimingServer server2 = TimingServerHelper.narrow (setup.getClientOrb ().string_to_object (ior1));

       assertNotSame (server, server2);
       assertNotSame(((ObjectImpl)server)._get_delegate (), ((ObjectImpl)server2)._get_delegate ());
    }


    public void testDuplicateObject2 ()
    {
        TimingServer server2 = TimingServerHelper.narrow (server._duplicate());

        assertNotSame (server, server2);
        assertNotSame(((ObjectImpl)server)._get_delegate (), ((ObjectImpl)server2)._get_delegate ());
    }


    /**
     * Sets a ReplyEndTime which will
     * expire during the invocation.
     */
    public void test_reply_end_time_async_expired()
    {
        ReplyHandler handler1 = new ReplyHandler()
        {
            public void operation_excep (ExceptionHolder excep_holder)
            {
                this.assertEquals (org.omg.CORBA.TIMEOUT.class,
                                   getException (excep_holder).getClass());
                pass();
            }
        };
        ReplyHandler handler2 = new ReplyHandler()
        {
            public void operation (int ami_return_val)
            {
                this.assertEquals (765, ami_return_val);
                pass();
            }
        };

        TimingServer server2 = clearPolicies (server);
        server2 = setReplyEndTime (server2, System.currentTimeMillis() + 1000);

        // This one should timeout (using the new server2)
        ((_TimingServerStub)server2).sendc_operation (ref (handler1), 767, 2000);
        handler1.wait_for_reply (4000);

        // This one should not timeout (using the original server)
        ((_TimingServerStub)server).sendc_operation (ref (handler2), 765, 2000);
        handler2.wait_for_reply (4000);
    }


    private class ReplyHandler extends CallbackTestCase.ReplyHandler
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
}
