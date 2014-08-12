package org.jacorb.test.bugs.bugjac189;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <code>TestCaseImpl</code> tests that a single threaded POA shuts down correctly.
 *
 * @author Nick Cross
 */
public class BugJac189Test extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private JAC189 server;


    /**
     * <code>TestCaseImpl</code> constructor for the suite.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception
    {
        server = JAC189Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     *
     * @return a <code>Test</code> value
     */
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup (JAC189Impl.class.getName());
    }


    /**
     * <code>test_singlethread</code> rapidly creates, calls an operation and
     * destroys single threaded poas.
     */
    @Test
    public void test_singlethread() throws Exception
    {
        for(int i=0; i<1000; i++)
        {
            Session session = server.login();

            session.test189Op();

            session.logout();

            Thread.sleep(10);
        }
    }
}
