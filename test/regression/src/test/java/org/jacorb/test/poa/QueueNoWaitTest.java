package org.jacorb.test.poa;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import junit.framework.AssertionFailedError;
import org.jacorb.test.AMI_CallbackServerHandler;
import org.jacorb.test.AMI_CallbackServerHandlerOperations;
import org.jacorb.test.AMI_CallbackServerHandlerPOATie;
import org.jacorb.test.CallbackServer;
import org.jacorb.test.CallbackServerHelper;
import org.jacorb.test._CallbackServerStub;
import org.jacorb.test.harness.CallbackTestCase;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.orb.CallbackServerImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.Messaging.ExceptionHolder;

/**
 * Overrun the request queue with queue_wait=off.
 * This must lead to TRANSIENT exceptions.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 */
public class QueueNoWaitTest extends CallbackTestCase
{
    private CallbackServer server;

    @Before
    public void setUp() throws Exception
    {
        server = CallbackServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties props = new Properties();
        props.setProperty ("jacorb.poa.queue_max", "10");
        props.setProperty ("jacorb.poa.queue_min", "1");
        props.setProperty ("jacorb.poa.queue_wait", "off");

        setup = new ClientServerSetup( CallbackServerImpl.class.getName(),
                                   null,
                                   props );

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
     * Overrun the request queue, expect TRANSIENT exception.
     */
    @Test
    public void test_overrun() throws Exception
    {
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
                if (getException (excep).getClass().equals
                     (org.omg.CORBA.TRANSIENT.class))
                {
                    holder.exceptionReceived = true;
                }
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
                .sendc_delayed_ping( handlerRef, 1000);
        }

        Thread.sleep (2000);

        assertTrue("should have raised a TRANSIENT exception", holder.exceptionReceived);
    }
}
