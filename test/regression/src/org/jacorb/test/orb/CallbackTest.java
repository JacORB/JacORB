package org.jacorb.test.orb;

import junit.framework.*;
import junit.extensions.*;

import org.jacorb.Tests.*;

import org.jacorb.test.common.*;
import org.omg.Messaging.ExceptionHolder;

public class CallbackTest extends CallbackTestCase
{
    private CallbackServer server;

    public CallbackTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = CallbackServerHelper.narrow( setup.getServerObject() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Callback Test" );   
        ClientServerSetup setup = new ClientServerSetup
            ( suite, "org.jacorb.test.orb.CallbackServerImpl" );

        //suite.addTest( new CallbackTest( "test_sync_ping", setup ) );            
        suite.addTest( new CallbackTest( "test_ping", setup ) );
        suite.addTest( new CallbackTest( "test_ping", setup ) );
        suite.addTest( new CallbackTest( "test_ping", setup ) );
        suite.addTest( new CallbackTest( "test_ping", setup ) );
            
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
            wrong_exception(" return_char_excep", excep_holder );
        }

        public void return_char(char ami_return_val)
        {
            wrong_reply( "return_char" );
        }

    }

    public void test_sync_ping()
    {
        server.ping();
    }
    
    public void test_ping()
    {
        ReplyHandler handler = new ReplyHandler() 
        {
            public void ping()
            {
                pass();
            }
        };
        
        AMI_CallbackServerHandlerPOATie tie = 
            new AMI_CallbackServerHandlerPOATie( handler );
        AMI_CallbackServerHandler h = tie._this( setup.getClientOrb() );
        
        ( ( _CallbackServerStub ) server ).sendc_ping( h );
        handler.wait_for_reply( 300 );
    }

}
