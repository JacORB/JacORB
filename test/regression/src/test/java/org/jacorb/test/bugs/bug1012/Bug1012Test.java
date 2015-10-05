package org.jacorb.test.bugs.bug1012;

import java.util.Properties;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class Bug1012Test extends ClientServerTestCase
{
    static TestCase tcase;
    static Door server;
    
    static int numberOfCallsToServer;
    static int numberOfItsMeCalls;
    static int numberOfCanIComeInCalls;

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
//        tcase = TestCase.DequePop;
        tcase = TestCase.ExtraCall;

        Properties props = new Properties();
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                + "ORBInit", DoorInitializer.class.getName());

        setup = new ClientServerSetup("org.jacorb.test.bugs.bug1012.DoorImpl", props, props);
    }

    @Before
    public void beforeSetup() throws Exception
    {
        server = DoorHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testNumberOfCallsToServer()
    {
        server.canIComeIn();
        Assert.assertArrayEquals(new int[]{4}, new int[]{numberOfCallsToServer});
        Assert.assertArrayEquals(new int[]{2}, new int[]{numberOfCanIComeInCalls});
        Assert.assertArrayEquals(new int[]{2}, new int[]{numberOfItsMeCalls});
    }

    enum TestCase {
        WorkJustFine,
        ExtraCall,
        DequePop;
    }
}
