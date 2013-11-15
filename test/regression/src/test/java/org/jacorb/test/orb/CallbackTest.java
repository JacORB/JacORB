package org.jacorb.test.orb;

import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import junit.framework.AssertionFailedError;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ExceptionHolderImpl;
import org.jacorb.test.AMI_CallbackServerHandler;
import org.jacorb.test.AMI_CallbackServerHandlerOperations;
import org.jacorb.test.AMI_CallbackServerHandlerPOATie;
import org.jacorb.test.CallbackServer;
import org.jacorb.test.CallbackServerHelper;
import org.jacorb.test.EmptyException;
import org.jacorb.test.NonEmptyException;
import org.jacorb.test._CallbackServerStub;
import org.jacorb.test.common.CallbackTestCase;
import org.jacorb.test.common.ClientServerSetup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.UserException;
import org.omg.Messaging.ExceptionHolder;

public class CallbackTest extends CallbackTestCase
{
    private CallbackServer server;

    private static final char EURO_SIGN = '\u20AC'; // not a CORBA char

    @Before
    public void setUp() throws Exception
    {
        server = CallbackServerHelper.narrow( setup.getServerObject() );
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup( "org.jacorb.test.orb.CallbackServerImpl" );

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

    @Test
    public void test_sync_ping()
    {
        server.ping();
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void test_return_char()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void return_char( char ami_return_val )
            {
                this.assertEquals( 'a', ami_return_val );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
                 .sendc_return_char( ref( handler ), ( short ) 'a', 100 );
        handler.wait_for_reply( 200 );
    }

    @Test
    public void test_return_illegal_char()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void return_char_excep( ExceptionHolder excep_holder )
            {
                this.assertEquals( org.omg.CORBA.DATA_CONVERSION.class,
                              getException( excep_holder ).getClass() );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_return_char( ref( handler ), ( short ) EURO_SIGN, 100 );
        handler.wait_for_reply( 500 );
    }

    @Test
    public void test_complex_operation()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void operation( int ami_return_val, char p1, int p2 )
            {
                this.assertEquals( 'A', p1 );
                this.assertEquals( 4321, p2 );
                this.assertEquals( p2, ami_return_val );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_operation( ref( handler ), 'a', false, 100 );
        handler.wait_for_reply( 200 );
    }

    @Test
    public void test_empty_exception()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void ex_1_excep( ExceptionHolder excep_holder )
            {
                this.assertEquals( EmptyException.class,
                              getException( excep_holder ).getClass() );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_ex_1( ref( handler ), true, 100 );
        handler.wait_for_reply( 500 );
    }

    @Test
    public void test_empty_exception_not_raised()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void ex_1()
            {
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_ex_1( ref( handler ), false, 100 );
        handler.wait_for_reply( 500 );
    }

    @Test
    public void test_non_empty_exception()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void ex_2_excep( ExceptionHolder excep_holder )
            {
                Exception ex = getException( excep_holder );
                if ( !(ex instanceof NonEmptyException) )
                {
                    this.fail( "wrong exception type: " + ex );
                }
                else
                {
                    NonEmptyException nex = (NonEmptyException)ex;

                    // The CORBA Spec and the Java Mapping are not
                    // entirely clear whether the "_reason" parameter
                    // should be marshaled along with the id.
                    // JacORB doesn't do it, and hence we don't check
                    // for it here.

                    // assertTrue( nex.getMessage().endsWith( " just do it" ) );

                    this.assertEquals( 17, nex.field1 );
                    this.assertEquals( "xxx", nex.field2 );
                    pass();
                }
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_ex_2( ref( handler ), 17, true, 100 );
        handler.wait_for_reply( 500 );
    }

    @Test
    public void test_either_exception_1()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void ex_3_excep( ExceptionHolder excep_holder )
            {
                this.assertEquals( EmptyException.class,
                              getException( excep_holder ).getClass() );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_ex_3( ref( handler ), false, 100 );
        handler.wait_for_reply( 500 );
    }

    @Test
    public void test_either_exception_2()
    {
        ReplyHandler handler = new ReplyHandler()
        {
            public void ex_3_excep( ExceptionHolder excep_holder )
            {
                this.assertEquals( NonEmptyException.class,
                              getException( excep_holder ).getClass() );
                pass();
            }
        };
        ( ( _CallbackServerStub ) server )
            .sendc_ex_3( ref( handler ), true, 100 );
        handler.wait_for_reply( 500 );
    }


    @Test
    public void test_jac492_exceptionholderlogging()
    {
        // This decidely hacky set of code is to provoke a rather unlikely error
        // from exception holder which in turn reveals that logging has not been
        // configured which throws another error.
        ExceptionHolder eh = new ExceptionHolderImpl ((org.jacorb.orb.ORB)setup.getClientOrb ());
        try
        {
            final Field fields[] = ExceptionHolder.class.getDeclaredFields();
            CDROutputStream cdr = new CDROutputStream (setup.getClientOrb ());
            cdr.write_string ("Dummy_id");

            for (int i = 0; i < fields.length; ++i)
            {
                if ("marshaled_exception".equals(fields[i].getName()))
                {
                    Field f = fields[i];
                    f.setAccessible(true);
                    f.set (eh, cdr.getBufferCopy ());
                    break;
                }
            }
            cdr.close();
            eh.raise_exception ();

            fail ("No exception raised");
        }
        catch (NullPointerException e)
        {
            fail ("Raised a null pointer exception" + e);
        }
        catch (org.omg.CORBA.UnknownUserException e)
        {
            // Success - expected exception.
        }
      catch (IllegalArgumentException e)
      {
         fail ("Wrong exception thrown" + e);
      }
      catch (IllegalAccessException e)
      {
         fail ("Wrong exception thrown" + e);
      }
      catch (UserException e)
      {
         fail ("Wrong exception thrown" + e);
      }
    }
}
