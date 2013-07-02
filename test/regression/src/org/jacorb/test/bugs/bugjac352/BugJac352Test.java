package org.jacorb.test.bugs.bugjac352;

import java.util.Arrays;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.bugs.bug352.JAC352;
import org.jacorb.test.bugs.bug352.JAC352Helper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;

/**
 * @author Alphonse Bendt
 */
public class BugJac352Test extends ClientServerTestCase
{
    private JAC352 server;

    public BugJac352Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        if (TestUtils.isJ2ME())
        {
            // J2ME doesn't provide an ORB
            return new TestSuite();
        }

        TestSuite suite = new TestSuite(BugJac352Test.class.getName());
        Properties clientProps = new Properties();
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        clientProps.setProperty("jacorb.interop.sun", "on");
        final Properties serverProps = TestUtils.newForeignORBProperties();
        serverProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        ClientServerSetup setup = new ClientServerSetup(suite, Jac352Server.class.getName(), clientProps, serverProps);

        TestUtils.addToSuite(suite, setup, BugJac352Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = JAC352Helper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testWStringValue() throws Exception
    {
        assertNull(server.bounce_wstringvalue(null));
        assertEquals("WStringValue", server.bounce_wstringvalue("WStringValue"));
    }

    public void testStringValue()
    {
        assertNull(server.bounce_stringvalue(null));
        assertEquals("StringValue", server.bounce_stringvalue("StringValue"));
    }

    public void testWStringValues() throws Exception
    {
        String[] values = {"abc", "abc"};
        assertTrue(Arrays.equals(values, server.bounce_wstrings(values)));
    }
}
