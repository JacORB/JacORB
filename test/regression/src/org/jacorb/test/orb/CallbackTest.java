package org.jacorb.test.orb;

import junit.framework.*;
import junit.extensions.*;

import org.jacorb.Tests.*;

import org.jacorb.test.common.*;
import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class CallbackTest extends CallbackTestCase
{
    private CallbackServer server;

    private static final char EURO_SIGN = '\u20AC'; // not a CORBA char

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

        suite.addTest( new CallbackTest( "test_sync_ping", setup ) );            
        suite.addTest( new CallbackTest( "test_ping", setup ) );
        suite.addTest( new CallbackTest( "test_delayed_ping", setup ) );
        suite.addTest( new CallbackTest( "test_pass_in_char", setup ) );
        suite.addTest( new CallbackTest( "test_pass_in_illegal_char", setup ) );
        suite.addTest( new CallbackTest( "test_return_char", setup ) );
        suite.addTest( new CallbackTest( "test_return_illegal_char", setup ) );
        suite.addTest( new CallbackTest( "test_complex_operation", setup ) );
            
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

    private AMI_CallbackServerHandler ref ( ReplyHandler handler )
    {
        AMI_CallbackServerHandlerPOATie tie =
            new AMI_CallbackServerHandlerPOATie( handler );
        return tie._this( setup.getClientOrb() );
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
        
        ( ( _CallbackServerStub ) server ).sendc_ping( ref( handler ) );
        handler.wait_for_reply( 1000 );
    }

    public void test_delayed_ping()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void delayed_ping()
            {
                pass();
            }
        };
        
        ( ( _CallbackServerStub ) server )
                    .sendc_delayed_ping( ref( handler ), 500 );
        handler.wait_for_reply( 700 );
    }
    
    public void test_pass_in_char()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void pass_in_char()
            {
                pass();
            }
        };
        
        ( ( _CallbackServerStub ) server )
                   .sendc_pass_in_char( ref( handler ), 'x', 100 );
        handler.wait_for_reply( 200 );
    }
    
    public void test_pass_in_illegal_char()
    {
        ReplyHandler handler = new ReplyHandler();

        try
        {        
            ( ( _CallbackServerStub ) server )
                   .sendc_pass_in_char( ref( handler ), EURO_SIGN, 100 );
            fail( "DATA_CONVERSION exception expected" );
        }
        catch( org.omg.CORBA.DATA_CONVERSION ex )
        {
            // ok
        }
    }        
     
    public void test_return_char()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void return_char( char ami_return_val )
            {
                pass();
                assertEquals( 'a', ami_return_val );
            }
        };
        ( ( _CallbackServerStub ) server )
                 .sendc_return_char( ref( handler ), ( short ) 'a', 100 );
        handler.wait_for_reply( 200 );
    }
    
    public void test_return_illegal_char()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void return_char_excep( ExceptionHolder excep_holder )
            {
                pass();
                assertEquals( org.omg.CORBA.DATA_CONVERSION.class, 
                              getException( excep_holder ).getClass() );
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_return_char( ref( handler ), ( short ) EURO_SIGN, 100 );
        handler.wait_for_reply( 200 );           
    }
    
    public void test_complex_operation()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation( int ami_return_val, char p1, int p2 )
            {
                pass();
                assertEquals( 'A', p1 );
                assertEquals( 4321, p2 );
                assertEquals( p2, ami_return_val );     
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_operation( ref( handler ), 
                              new CharHolder( 'a' ), false, 100 );
        handler.wait_for_reply( 200 );
    }

}
