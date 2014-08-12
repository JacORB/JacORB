package org.jacorb.test.bugs.bug927;

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
    private org.omg.CORBA.Object server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties severProp = new Properties();
        severProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                              MyInitializer.class.getName());

        Properties clientProp = new Properties();
        clientProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                               MyInitializer.class.getName());

        setup = new ClientServerSetup(Server.class.getName()
                                      , "org.jacorb.test.bugs.bug927.TestObjectImpl"
                                      , clientProp, severProp);
    }

    @Before
    public void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    @Test
    public void testCTXPassingTest() throws Exception
    {
        TestObject testObject = TestObjectHelper.narrow( server );
        Current current = (Current) setup.getClientOrb().resolve_initial_references( "PICurrent" );

        Any any = setup.getClientOrb().create_any();
        any.insert_string( "JacOrbRocks" );

        current.set_slot( MyInitializer.slot_id, any );

        testObject.foo();
    }
}
