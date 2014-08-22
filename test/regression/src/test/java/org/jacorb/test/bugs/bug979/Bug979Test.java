package org.jacorb.test.bugs.bug979;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IMRExcludedClientServerCategory.class)
public final class Bug979Test extends ClientServerTestCase
{
    private Hello server;

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        Properties props = new Properties();

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "ORBInit", Initializer.class.getName());

        setup = new ClientServerSetup( "org.jacorb.test.bugs.bug979.HelloImpl", props, props );
    }

    @Before
    public void setUp() throws Exception
    {
        server = HelloHelper.narrow(setup.getServerObject());

        server.setIOR (setup.getServerIOR());
    }

    @Test
    public void testRemoteServerWithLocalCall()
    {
        server._non_existent();

        server.sayHello();
    }
}
