package org.jacorb.test.orb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.jacorb.test.ExceptionServer;
import org.jacorb.test.ExceptionServerHelper;
import org.jacorb.test.MyUserException;
import org.jacorb.test.MyUserExceptionHelper;
import org.jacorb.test.NonEmptyException;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class gathers all sorts of exception-related tests.
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ExceptionTest extends ClientServerTestCase
{
    private ExceptionServer server;

    @Before
    public void setUp() throws Exception
    {
        server = ExceptionServerHelper.narrow( setup.getServerObject() );
    }

    @After
    public void tearDown() throws Exception
    {
        Thread.sleep(1000);
        server = null;
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup(ExceptionServerImpl.class.getName());

    }

    /**
     * Checks whether a RuntimeException in the Servant is
     * properly reported back to the client, including the
     * error message.
     */
    @Test
    public void testRuntimeException()
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
    @Test
    public void testUserException1()
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

    @Test
    public void testUserException2()
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

    @Test
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
