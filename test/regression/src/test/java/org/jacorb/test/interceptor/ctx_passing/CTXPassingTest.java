package org.jacorb.test.interceptor.ctx_passing;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.Current;

public class CTXPassingTest extends ClientServerTestCase
{
    private TestObject server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties severProp = new Properties();
        severProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                              ServerInitializer.class.getName());
        Properties clientProp = new Properties();
        clientProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                               ClientInitializer.class.getName());

        setup = new ClientServerSetup(TestObjectImpl.class.getName(), clientProp, severProp);
    }

    @Before
    public void setUp() throws Exception
    {
        server = TestObjectHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testCTXPassingTest() throws Exception
    {
        Current current = (Current) setup.getClientOrb().resolve_initial_references( "PICurrent" );

        Any any = setup.getClientOrb().create_any();
        any.insert_string( "This is a test!" );

        current.set_slot( ClientInitializer.slot_id, any );

        server.foo();
    }
}
