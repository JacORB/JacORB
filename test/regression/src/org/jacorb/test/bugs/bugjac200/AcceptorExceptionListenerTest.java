package org.jacorb.test.bugs.bugjac200;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POAHelper;

/**
 * <code>AcceptorExceptionListenerTest</code> tests one of the two
 * forms that can cause an infinite loop with IIOPListener.
 *
 * 1) If security has been configured but the cipher suites are invalid
 *    a SSLExecption is thrown. However as the server socket cannot be
 *    opened properly this leads to an infinite loop. The AcceptorListener
 *    can detect this and shutdown the ORB.
 * 2) Alternatively if the ORB has been built with JDK13 & JSSE but is run
 *    without the JSSE jars this will lead to a NoClassDefError for the
 *    SSL jars from the listener. This, as above, also results in an infinite
 *    loop.
 */
public class AcceptorExceptionListenerTest extends TestCase
{
    private ORB orb;
    private volatile boolean orbIsDown = false;

    public static Test suite()
    {
        TestSuite suite = new TestSuite ("AcceptorExceptionListener Test");

        // Due to the changes in Java 7
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4673444
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4873188
        // this test only works on JDK 6
        if ( ! TestUtils.JDK_17 )
        {
            suite.addTestSuite(AcceptorExceptionListenerTest.class);
        }

        return suite;
    }

    public void setUp() throws Exception
    {
        Properties props = CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks");

        props.put("jacorb.implname",
                  "org.jacorb.test.bugs.bugjac200.AcceptorExceptionListenerTest");
        props.put("jacorb.security.ssl.server.cipher_suites",
                  "SSL_RSA_WITH_RC4_128_MD5");
        props.put("jacorb.security.ssl.client.cipher_suites",
                  "SSL_RSA_WITH_RC4_128_MD5");

        props.put("jacorb.acceptor_exception_listener",
                  TestAcceptorExceptionListener.class.getName());

        //init ORB
        orb = ORB.init( (String[]) null, props );

        TestAcceptorExceptionListener.reset();

        new Thread()
        {
            public void run() {
                orb.run();
                orbIsDown = true;
            };
        }.start();
    }

    public void tearDown() throws Exception
    {
        orb.shutdown(true);
        orb = null;
    }

    public void testListener() throws Exception
    {
        TestAcceptorExceptionListener.doShutdown = false;

        final Exception[] exception = new Exception[1];

        startAcceptor(exception);

        Thread.sleep(TestUtils.getMediumTimeout());

        assertNull("exception during orb start: " + exception[0], exception[0]);

        assertTrue("Acceptor was not created", TestAcceptorExceptionListener.hasBeenCreated);
        assertTrue("Acceptor not called", TestAcceptorExceptionListener.getHasBeenCalled(TestUtils.getMediumTimeout(), true));
        assertFalse(orbIsDown);
    }

    public void testShutdown() throws Exception
    {
        TestAcceptorExceptionListener.doShutdown = true;

        final Exception[] exception = new Exception[1];

        startAcceptor(exception);

        Thread.sleep(TestUtils.getMediumTimeout());

        assertNull("exception during orb start: " + exception[0], exception[0]);
        assertTrue("Listener was not created", TestAcceptorExceptionListener.hasBeenCreated);
        assertTrue("Listener was not invoked", TestAcceptorExceptionListener.getHasBeenCalled(TestUtils.getMediumTimeout(), true));
        assertTrue("ORB was not shutdown", orbIsDown);
    }

    /**
     * access the RootPOA thereby causing the Acceptors to be started
     */
    private void startAcceptor(final Exception[] exception)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    //init POA
                    POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    exception[0] = e;
                }
            }
        }.start();
    }
}
