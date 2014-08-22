package org.jacorb.test.bugs.bugjac352;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Properties;
import org.jacorb.test.bugs.bug352.JAC352;
import org.jacorb.test.bugs.bug352.JAC352Helper;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alphonse Bendt
 */
@Category(IMRExcludedClientServerCategory.class)
public class BugJac352Test extends ClientServerTestCase
{
    private JAC352 server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        Properties clientProps = new Properties();
        clientProps.setProperty("jacorb.interop.sun", "on");
        final Properties serverProps = TestUtils.newForeignORBProperties();
        setup = new ClientServerSetup(Jac352Server.class.getName(), clientProps, serverProps);
    }

    @Before
    public void setUp() throws Exception
    {
        server = JAC352Helper.narrow(setup.getServerObject());
    }

    @Test
    public void testWStringValue() throws Exception
    {
        assertNull(server.bounce_wstringvalue(null));
        assertEquals("WStringValue", server.bounce_wstringvalue("WStringValue"));
    }

    @Test
    public void testStringValue()
    {
        assertNull(server.bounce_stringvalue(null));
        assertEquals("StringValue", server.bounce_stringvalue("StringValue"));
    }

    @Test
    public void testWStringValues() throws Exception
    {
        String[] values = {"abc", "abc"};
        assertTrue(Arrays.equals(values, server.bounce_wstrings(values)));
    }
}
