package org.jacorb.test.orb;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.ExceptionServer;
import org.jacorb.test.ExceptionServerHelper;
import org.jacorb.test.MyUserException;
import org.jacorb.test.MyUserExceptionHelper;
import org.jacorb.test.NonEmptyException;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.JacORBTestSuite;
import org.jacorb.test.common.TestUtils;

/**
 * This class gathers all sorts of exception-related tests.
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ExceptionTest extends ClientServerTestCase
{
    private ExceptionServer server;

    public ExceptionTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public void setUp() throws Exception
    {
        server = ExceptionServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        Thread.sleep(1000);
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Client/server exception tests", ExceptionTest.class);
        ClientServerSetup setup = new ClientServerSetup(suite, ExceptionServerImpl.class.getName());

        TestUtils.addToSuite(suite, setup, ExceptionTest.class);

        return setup;
    }

    /**
     * Checks whether a RuntimeException in the Servant is
     * properly reported back to the client, including the
     * error message.
     * @jacorb-since cvs
     */
    public void _testRuntimeException()
    {
        try
        {
            server.throwRuntimeException("sample message");
            fail("should have raised a CORBA SystemException");
        }
        catch (org.omg.CORBA.SystemException ex)
        {
            assertEquals("Server-side Exception: java.lang.RuntimeException: sample message", ex.getMessage());
        }
    }

    /**
     * Checks if a user exception is properly reported back to the client.
     */
    public void _testUserException1()
    {
        try
        {
            server.throwUserExceptionWithMessage1(77, "my sample message");
            fail("should have thrown NonEmptyException");
        }
        catch (NonEmptyException ex)
        {
            assertEquals (77, ex.field1);
            assertEquals ("my sample message", ex.field2);
        }
    }


    public void _testUserException2()
    {
        try
        {
            server.throwUserException();
            fail();
        }
        catch(MyUserException e)
        {
            // expected
            assertEquals(MyUserExceptionHelper.id(), e.getMessage());
        }
    }

    public void testUserExceptionWithData()
    {
        try
        {
            server.throwUserExceptionWithMessage2("sample reason", "sample message");
            fail();
        }
        catch(MyUserException e)
        {
            // expected
            assertEquals("sample reason", e.getMessage());
            assertEquals("sample message", e.message);
        }
    }
}
