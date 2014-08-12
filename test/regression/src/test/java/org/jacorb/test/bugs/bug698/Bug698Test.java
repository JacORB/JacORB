package org.jacorb.test.bugs.bug698;

import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bug698Test extends ClientServerTestCase
{
    private Server server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.bugs.bug698.ServerImpl");
    }

    @Before
    public void setUp() throws Exception
    {
        this.server = ServerHelper.narrow(setup.getServerObject());
    }

    @After
    public void tearDown() throws Exception
    {
        this.server = null;
    }

    @Test
    public void testMarshaling ()
    {
        Top test1 = new TopImpl();
        TestStruct test2 = new TestStruct(new byte[5000], new AValueTypeImpl(),
                                          new AValueTypeImpl());
        assertTrue(this.server.sendTop(test1) != null);
        assertTrue(this.server.sendTestStruct(test2) != null);
    }
}
