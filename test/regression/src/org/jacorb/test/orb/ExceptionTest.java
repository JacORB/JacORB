package org.jacorb.test.orb;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.*;
import org.jacorb.test.common.*;

/**
 * This class gathers all sorts of exception-related tests.
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
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
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("Client/server exception tests",
                                              ExceptionTest.class);

        ClientServerSetup setup =
            new ClientServerSetup(suite,
                                  "org.jacorb.test.orb.ExceptionServerImpl");

        suite.addTest(new ExceptionTest("testRuntimeException", setup));
        suite.addTest(new ExceptionTest("testUserException", setup));

        return setup;
    }

    /**
     * Checks whether a RuntimeException in the Servant is
     * properly reported back to the client, including the
     * error message.
     * @jacorb-since cvs
     */
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
    public void testUserException()
    {
        try
        {
            server.throwUserException(77, "my sample message");
            fail("should have thrown NonEmptyException");
        }
        catch (NonEmptyException ex)
        {
            assertEquals (77, ex.field1);
            assertEquals ("my sample message", ex.field2);
        }
    }

}
