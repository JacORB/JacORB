package org.jacorb.test.poa;

import java.util.Properties;

import junit.framework.*;
import junit.extensions.*;

import org.jacorb.test.*;

import org.jacorb.test.common.*;
import org.omg.CORBA.*;
import org.omg.Messaging.*;


/**
 * Try to overrun the request queue with queue_wait=on.
 * Despite a heavy request storm all requests should come through.
 * 
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
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

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Request Queue Overrun - waiting" );   

        Properties props = new Properties();
        props.setProperty ("jacorb.poa.queue_max", "10");
        props.setProperty ("jacorb.poa.queue_min", "5");
        props.setProperty ("jacorb.poa.queue_wait", "on");

        ClientServerSetup setup = new ClientServerSetup
            ( suite, "org.jacorb.test.orb.CallbackServerImpl",
              null, props );

        suite.addTest( new QueueWaitTest( "test_warm_up", setup ) );
        suite.addTest( new QueueWaitTest( "test_overrun", setup ) );
            
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

    public void test_warm_up()
    {
        server.ping();
    }

    /**
     * Try to overrun the request queue, expect that all
     * requests come through without exceptions.
     */
    public void test_overrun()
    {
        class Holder {
            public boolean exceptionReceived = false;
        }
        final Holder h = new Holder();
        
        ReplyHandler handler = new ReplyHandler()
        {
            public void delayed_ping_excep (ExceptionHolder excep)
            {
                h.exceptionReceived = true;
            }   
            
            public void delayed_ping()
            {
                // ignore
            }
        };

        for (int i=0; i < 1000; i++)
        {
            ( ( _CallbackServerStub ) server )
                    .sendc_delayed_ping( ref( handler ), 10 );
            if (h.exceptionReceived)
                fail ("should not have raised an exception");
        }
        try 
        { 
            Thread.sleep (1000); 
        } 
        catch (InterruptedException ex) 
        {}

        if (h.exceptionReceived)
            fail ("should not have raised an exception");
    }
    

}
