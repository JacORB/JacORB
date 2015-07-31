package org.jacorb.test.bugs.bug1011;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

public final class Bug1011Test extends ClientServerTestCase
{
    static TestCase tcase;
    static Door server;

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        tcase = TestCase.DequePop;

        Properties props = new Properties();
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                + "ORBInit", DoorInitializer.class.getName());

        setup = new ClientServerSetup("org.jacorb.test.bugs.bug1011.DoorImpl", props, props);
    }

    @Before
    public void beforeSetup() throws Exception
    {
        server = DoorHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testNoSuchElement()
    {
        server.canIComeIn();
    }

    enum TestCase {
        WorkJustFine,
        ExtraCall,
        DequePop;
    }
}
