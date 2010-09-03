package org.jacorb.test.bugs.bug698;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

public class Bug698Test extends ClientServerTestCase
{
    private Server server;

    public Bug698Test (String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite ()
    {
        TestSuite suite = new TestSuite("bug698");
        ClientServerSetup setup = new ClientServerSetup(suite,
                                                        "org.jacorb.test.bugs.bug698.ServerImpl");

        TestUtils.addToSuite(suite, setup, Bug698Test.class);

        return setup;
    }

    protected void setUp () throws Exception
    {
        this.server = ServerHelper.narrow(this.setup.getServerObject());
    }

    protected void tearDown () throws Exception
    {
        this.server = null;
    }

    public void testMarshaling ()
    {
        Top test1 = new TopImpl();
        TestStruct test2 = new TestStruct(new byte[5000], new AValueTypeImpl(),
                                          new AValueTypeImpl());
        assertTrue(this.server.sendTop(test1) != null);
        assertTrue(this.server.sendTestStruct(test2) != null);
    }
}
