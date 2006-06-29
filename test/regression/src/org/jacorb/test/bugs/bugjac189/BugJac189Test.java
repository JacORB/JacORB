package org.jacorb.test.bugs.bugjac189;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * <code>TestCaseImpl</code> tests that a single threaded POA shuts down correctly.
 *
 * @author Nick Cross
 * @version $Id$
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
    public BugJac189Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = JAC189Helper.narrow( setup.getServerObject() );
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "Jac189 - Context Key test" );

        ClientServerSetup setup = new ClientServerSetup
            (suite, JAC189Impl.class.getName());

        TestUtils.addToSuite(suite, setup, BugJac189Test.class);

        return setup;
    }


    /**
     * <code>test_singlethread</code> rapidly creates, calls an operation and
     * destroys single threaded poas.
     */
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
