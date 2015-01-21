package org.jacorb.test.bugs.bugjac200;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.harness.CommonSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POAHelper;

/**
 * <code>AcceptorExceptionListenerTest</code> tests one of the two
 * forms that can cause an infinite loop with IIOPListener.
 *
 * <p></p>
 * 1. If security has been configured but the cipher suites are invalid
 *    a SSLExecption is thrown. However as the server socket cannot be
 *    opened properly this leads to an infinite loop. The AcceptorListener
 *    can detect this and shutdown the ORB.
 * <p></p>
 * 2. Alternatively if the ORB has been built with JDK13 and JSSE but is run
 *    without the JSSE jars this will lead to a NoClassDefError for the
 *    SSL jars from the listener. This, as above, also results in an infinite
 *    loop.
 */
public class AcceptorExceptionListenerTest
{
    @Rule
    public TestName name = new TestName();

    private ORB orb;
    private volatile boolean orbIsDown = false;


    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.is17);
    }

    protected Properties initORBProperties() throws Exception
    {
        Properties props = new Properties ();
        props.putAll(CommonSetup.loadSSLProps("jsse_client_props", "jsse_client_ks"));

        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        props.put("jacorb.implname",
                "org.jacorb.test.bugs.bugjac200.AcceptorExceptionListenerTest");
        props.put("jacorb.security.ssl.server.cipher_suites",
                "SSL_RSA_WITH_RC4_128_MD5");
        props.put("jacorb.security.ssl.client.cipher_suites",
                "SSL_RSA_WITH_RC4_128_MD5");
        props.put("jacorb.acceptor_exception_listener",
                TestAcceptorExceptionListener.class.getName());

        return props;
    }

    @Before
    public void setUp() throws Exception
    {
        orb = ORB.init(new String[] { "-ORBID" , name.getMethodName() }, initORBProperties());

        TestAcceptorExceptionListener.reset();

        new Thread()
        {
            @Override
            public void run() {
                orb.run();
                orbIsDown = true;
            };
        }.start();
    }

    @After
    public void tearDown() throws Exception
    {
        orb.shutdown(false);
    }

    @Test
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

    @Test
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
            @Override
            public void run()
            {
                try
                {
                    //init POA
                    POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));
                }
                catch (Exception e)
                {
                    exception[0] = e;
                }
            }
        }.start();
    }
}
