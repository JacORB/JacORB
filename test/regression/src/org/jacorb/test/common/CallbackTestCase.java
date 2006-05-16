package org.jacorb.test.common;

import org.omg.Messaging.*;

import junit.framework.*;

/**
 * A special <code>ClientServerTestCase</code> for testing asynchronous
 * method invocations following the callback model.
 * <p>
 * This class makes it convenient to issue an asynchronous invocation
 * and wait for the reply within the same test case.  There is an abstract
 * <code>ReplyHandler</code> defined as an inner class of this class.
 * It provides a synchronization mechanism so that the main thread of 
 * control that executes the test case can wait on the reply handler
 * until it has received and analyzed the reply.
 * <p>
 * As an example, assume that you want to test asynchronous invocations
 * of an interface <code>MyServer</code>.  You would create a subclass
 * of <code>CallbackTestCase</code>, and extend the generic
 * <code>ReplyHandler</code> for <code>MyServer</code> as follows:
 * 
 * <p><blockquote><pre>
 * public class MyServerTest extends CallbackTestCase
 * {
 *     private ReplyHandler extends CallbackTestCase.ReplyHandler
 *                          implements MyServerOperations
 *     {
 *         // ...   
 *     }
 *     
 * }
 * </pre></blockquote><p>
 * 
 * Each method from <code>MyServerOperations</code> should be 
 * implemented to call <code>wrong_reply()</code> (for reply methods) or 
 * <code>wrong_exception()</code> (for <code>_excep</code> methods).  In 
 * the actual test cases, these methods will in turn be overwritten 
 * anonymously for the correct replies.
 * <p>
 * Your type-specific <code>ReplyHandler</code> will be turned into a 
 * CORBA object using the tie approach.  This is best done in a 
 * convenience method such as the following:
 * 
 * <p><blockquote><pre>
 * private AMI_MyServerHandler ref ( ReplyHandler handler )
 * {
 *     AMI_MyServerHandlerPOATie tie =
 *         new AMI_MyServerHandlerPOATie( handler )
 *         {
 *             public org.omg.CORBA.portable.OutputStream 
 *                 _invoke( String method, 
 *                          org.omg.CORBA.portable.InputStream _input, 
 *                          org.omg.CORBA.portable.ResponseHandler handler )
 *                 throws org.omg.CORBA.SystemException   
 *             {
 *                 try
 *                 {
 *                     return super._invoke( method, _input, handler );
 *                 }
 *                 catch( AssertionFailedError e )
 *                 {
 *                     return null;
 *                 }
 *             }
 *         };
 *     return tie._this( setup.getClientOrb() );
 * }
 * </pre></blockquote><p>
 * 
 * The <code>_invoke()</code> method of the POATie is overridden anonymously 
 * here to catch JUnit's <code>AssertionFailedError</code>s.  Without this, 
 * a failed test case would cause all subsequent test cases to fail as well.
 * You can copy this method verbatim into your own source, except for 
 * changing the name of the AMI classes.
 * <p>
 * An individual test case may then look like this:
 * 
 * <p><blockquote><pre>
 * public void test_some_operation()
 * {
 *     ReplyHandler handler = new ReplyHandler()
 *     {
 *         public void some_operation( int ami_return_val )
 *         {
 *             assertEquals( 123, ami_return_val );
 *             pass();
 *         }
 *     };
 *     ( ( _CallbackServerStub ) server )
 *              .sendc_some_operation( ref( handler ) );
 *     handler.wait_for_reply( 200 );
 * }
 * </pre></blockquote><p>
 * 
 * The <code>ReplyHandler</code> is extended anonymously to handle and
 * analyze the expected reply.  You may use any of the
 * <code>assert...()</code> methods defined in
 * <code>junit.framework.Assert</code> to do this (they are redefined in
 * the generic <code>ReplyHandler</code>).  Unlike in a normal test case,
 * you must also call the <code>pass()</code> method explicitly if the
 * test case did not fail.  After the <code>ReplyHandler</code> has been
 * defined, the asynchronous operation is invoked using the corresponding
 * <code>sendc_</code> method.  The calling thread then waits for a reply
 * for at most 200 milliseconds.  If no reply is received within that
 * time, the test case fails.
 *  
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class CallbackTestCase extends ClientServerTestCase
{
    public CallbackTestCase(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected abstract class ReplyHandler
    {
        private boolean replyReceived  = false;
        private boolean testFailed     = false;
        private String  failureMessage = null;

        protected ReplyHandler() {
            super();
        }

        public synchronized void wait_for_reply(long timeout)
        {
            try 
            {
                long start = System.currentTimeMillis();
                
                while(!replyReceived && System.currentTimeMillis() < start + timeout)
                {
                    this.wait(timeout);
                }
                
                if ( !replyReceived )
                {
                        junit.framework.Assert.fail
                            ( "no reply within timeout (" 
                              + timeout + "ms)" );
                }

                if ( testFailed )
                {
                    junit.framework.Assert.fail( failureMessage );
                }
            }
            catch ( InterruptedException e )
            {
                junit.framework.Assert.fail
                    ( "interrupted while waiting for reply" );
            }
            finally
            {
                replyReceived  = false;
                testFailed     = false;
                failureMessage = null;
            }
        }

        public synchronized void fail(String message)
        {
            replyReceived = true;
            testFailed = true;
            failureMessage = message;
            this.notifyAll();
            throw new AssertionFailedError();
        }

        public void wrong_reply( String name )
        {
            fail( "wrong reply method: " + name );
        }

        public void wrong_exception( String methodName, 
                                     ExceptionHolder excep_holder )
        {
            try
            {
                excep_holder.raise_exception();
            }
            catch( Exception e )
            {
                fail( "unexpected exception: " 
                      + methodName + ", " + e );
            }   
        }

        public Exception getException( ExceptionHolder excep_holder )
        {
            try
            {
                excep_holder.raise_exception();
                return null;
            }
            catch( Exception e )
            {
                return e;
            }
        }

        public synchronized void pass()
        {
            replyReceived = true;
            testFailed = false;
            failureMessage = null;
            this.notifyAll();
        }

        // The assert methods below are lifted from junit.framework.Assert.
        // The only change is to make them non-static here.

        /**
         * Asserts that two bytes are equal.
        */
        public void assertEquals(byte expected, byte actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two chars are equal.
         */
        public void assertEquals(char expected, char actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two doubles are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        public void assertEquals(
            double expected,
            double actual,
            double delta)
        {
            assertEquals(null, expected, actual, delta);
        }

        /**
         * Asserts that two floats are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        public void assertEquals(float expected, float actual, float delta)
        {
            assertEquals(null, expected, actual, delta);
        }

        /**
         * Asserts that two ints are equal.
         */
        public void assertEquals(int expected, int actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two longs are equal.
         */
        public void assertEquals(long expected, long actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown.
         */
        public void assertEquals(Object expected, Object actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two bytes are equal.
         */
        public void assertEquals(String message, byte expected, byte actual)
        {
            assertEquals(message, new Byte(expected), new Byte(actual));
        }

        /**
         * Asserts that two chars are equal.
         */
        public void assertEquals(String message, char expected, char actual)
        {
            assertEquals(
                message,
                new Character(expected),
                new Character(actual));
        }

        /**
         * Asserts that two doubles are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        public void assertEquals(
            String message,
            double expected,
            double actual,
            double delta)
        {
            // handle infinity specially since subtracting to infinite values gives NaN and the
            // the following test fails
            if (Double.isInfinite(expected))
            {
                if (!(expected == actual))
                    failNotEquals(
                        message,
                        new Double(expected),
                        new Double(actual));
            }
            else if (
                !(Math.abs(expected - actual) <= delta))
                // Because comparison with NaN always returns false
                failNotEquals(
                    message,
                    new Double(expected),
                    new Double(actual));
        }

        /**
         * Asserts that two floats are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        public void assertEquals(
            String message,
            float expected,
            float actual,
            float delta)
        {
            // handle infinity specially since subtracting to infinite values gives NaN and the
            // the following test fails
            if (Float.isInfinite(expected))
            {
                if (!(expected == actual))
                    failNotEquals(
                        message,
                        new Float(expected),
                        new Float(actual));
            }
            else if (!(Math.abs(expected - actual) <= delta))
                failNotEquals(message, new Float(expected), new Float(actual));
        }

        /**
         * Asserts that two ints are equal.
         */
        public void assertEquals(String message, int expected, int actual)
        {
            assertEquals(message, new Integer(expected), new Integer(actual));
        }

        /**
         * Asserts that two longs are equal.
         */
        public void assertEquals(String message, long expected, long actual)
        {
            assertEquals(message, new Long(expected), new Long(actual));
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown.
         */
        public void assertEquals(
            String message,
            Object expected,
            Object actual)
        {
            if (expected == null && actual == null)
                return;
            if (expected != null && expected.equals(actual))
                return;
            failNotEquals(message, expected, actual);
        }

        /**
         * Asserts that two shorts are equal.
         */
        public void assertEquals(String message, short expected, short actual)
        {
            assertEquals(message, new Short(expected), new Short(actual));
        }

        /**
         * Asserts that two booleans are equal.
         */
        public void assertEquals(
            String message,
            boolean expected,
            boolean actual)
        {
            assertEquals(message, new Boolean(expected), new Boolean(actual));
        }

        /**
         * Asserts that two shorts are equal.
         */
        public void assertEquals(short expected, short actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that two booleans are equal.
         */
        public void assertEquals(boolean expected, boolean actual)
        {
            assertEquals(null, expected, actual);
        }

        /**
         * Asserts that an object isn't null.
         */
        public void assertNotNull(Object object)
        {
            assertNotNull(null, object);
        }

        /**
         * Asserts that an object isn't null.
         */
        public void assertNotNull(String message, Object object)
        {
            assertTrue(message, object != null);
        }

        /**
         * Asserts that an object is null.
         */
        public void assertNull(Object object)
        {
            assertNull(null, object);
        }

        /**
         * Asserts that an object is null.
         */
        public void assertNull(String message, Object object)
        {
            assertTrue(message, object == null);
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * the same an AssertionFailedError is thrown.
         */
        public void assertSame(Object expected, Object actual)
        {
            assertSame(null, expected, actual);
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * an AssertionFailedError is thrown.
         */
        public void assertSame(String message, Object expected, Object actual)
        {
            if (expected == actual)
                return;
            failNotSame(message, expected, actual);
        }

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError with the given message.
         */
        public void assertTrue(String message, boolean condition)
        {
            if (!condition)
                fail(message);
        }

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError.
         */
        public void assertTrue(boolean condition)
        {
            assertTrue(null, condition);
        }
        
        private void failNotEquals( String message,
                                    Object expected,
                                    Object actual )
        {
            String formatted = "";
            if (message != null)
                formatted = message + " ";
            fail( formatted + "expected:<" + expected
                            + "> but was:<" + actual + ">");
        }

        private void failNotSame( String message,
                                  Object expected,
                                  Object actual )
        {
            String formatted = "";
            if (message != null)
                formatted = message + " ";
            fail(formatted + "expected same");
        }

    }

}
