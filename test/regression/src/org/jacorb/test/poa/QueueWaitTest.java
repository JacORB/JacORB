package org.jacorb.test.poa;

import java.util.Properties;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.AMI_CallbackServerHandler;
import org.jacorb.test.AMI_CallbackServerHandlerOperations;
import org.jacorb.test.AMI_CallbackServerHandlerPOATie;
import org.jacorb.test.CallbackServer;
import org.jacorb.test.CallbackServerHelper;
import org.jacorb.test._CallbackServerStub;
import org.jacorb.test.common.CallbackTestCase;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.CallbackServerImpl;
import org.omg.Messaging.ExceptionHolder;


/**
 * Try to overrun the request queue with queue_wait=on.
 * Despite a heavy request storm all requests should come through.
 *
 * @author Andre Spiegel
 */
public class QueueWaitTest extends CallbackTestCase
{
    private CallbackServer server;

    public QueueWaitTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = CallbackServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Request Queue Overrun - waiting (" + QueueWaitTest.class.getName() + ")");

        Properties props = new Properties();
        props.setProperty ("jacorb.poa.queue_max", "10");
        props.setProperty ("jacorb.poa.queue_min", "5");
        props.setProperty ("jacorb.poa.queue_wait", "on");

        ClientServerSetup setup = new ClientServerSetup(suite, CallbackServerImpl.class.getName(), null, props);

        TestUtils.addToSuite(suite, setup, QueueWaitTest.class);

        return setup;
    }

    private class ReplyHandler
        extends CallbackTestCase.ReplyHandler
        implements AMI_CallbackServerHandlerOperations
    {
        public void delayed_ping_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "delayed_ping_excep", excep_holder );
        }

        public void delayed_ping()
        {
            wrong_reply( "delayed_ping" );
        }

        public void operation_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "operation_excep", excep_holder );
        }

        public void operation(int ami_return_val, char p1, int p2)
        {
            wrong_reply( "operation" );
        }

        public void pass_in_char_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "pass_in_char_excep", excep_holder );
        }

        public void pass_in_char()
        {
            wrong_reply( "pass_in_char" );
        }

        public void ping_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "ping_excep", excep_holder );
        }

        public void ping()
        {
            wrong_reply( "ping" );
        }

        public void return_char_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "return_char_excep", excep_holder );
        }

        public void return_char(char ami_return_val)
        {
            wrong_reply( "return_char" );
        }

        public void ex_1_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "ex_1_excep", excep_holder );
        }

        public void ex_1()
        {
            wrong_reply( "ex_1" );
        }

        public void ex_2_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "ex_2_excep", excep_holder );
        }

        public void ex_2(int ami_return_val, int p)
        {
            wrong_reply( "ex_2" );
        }

        public void ex_3_excep(ExceptionHolder excep_holder)
        {
            wrong_exception( "ex_3_excep", excep_holder );
        }

        public void ex_3()
        {
            wrong_reply( "ex_3" );
        }

    }

    private AMI_CallbackServerHandler ref ( ReplyHandler handler )
    {
        AMI_CallbackServerHandlerPOATie tie =
            new AMI_CallbackServerHandlerPOATie( handler )
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

    /**
     * Try to overrun the request queue, expect that all
     * requests come through without exceptions.
     */
    public void test_overrun() throws Exception
    {
        // for warm up
        server.ping();

        class Holder
        {
            public boolean exceptionReceived = false;
        }

        final Holder holder = new Holder();

        ReplyHandler handler = new ReplyHandler()
        {
            public void delayed_ping_excep (ExceptionHolder excep)
            {
                holder.exceptionReceived = true;
            }

            public void delayed_ping()
            {
                // ignore
            }
        };

        final AMI_CallbackServerHandler handlerRef = ref( handler );
        for (int i=0; i < 1000; i++)
        {
            ( ( _CallbackServerStub ) server )
                    .sendc_delayed_ping( handlerRef, 10 );
            assertFalse("should not have raised an exception", holder.exceptionReceived);
        }

        Thread.sleep (1000);

        assertFalse("should not have raised an exception", holder.exceptionReceived);
    }
}
