package org.jacorb.test.bugs.bug1012;

import java.util.Properties;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IMRExcludedClientServerCategory.class)
public class Bug1012Test extends ClientServerTestCase
{
    enum TestCase {
        WorkJustFine,
        ExtraCall,
        DequePop;
    }

    static TestCase tcase;

    static HashMap <TestCase, int[]> expected;
    static Door server;
    
    static int numberOfCallsToServer;
    static int numberOfItsMeCalls;
    static int numberOfCanIComeInCalls;
    static int comeIn = 0;
    static int itsMe = 0;
    static AtomicBoolean introduce = new AtomicBoolean(false);


    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        expected = new HashMap<TestCase, int[]>();

        expected.put(TestCase.WorkJustFine, new int[]{3,2,0});
        expected.put(TestCase.ExtraCall, new int[]{4,2,2});
        expected.put(TestCase.DequePop, new int[]{5,2,0});

        Properties props = new Properties();
        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass."
                + "ORBInit", DoorInitializer.class.getName());

        setup = new ClientServerSetup("org.jacorb.test.bugs.bug1012.DoorImpl", props, props);
    }

    @Before
    public void beforeSetup() throws Exception
    {
        server = DoorHelper.narrow(setup.getServerObject());
        numberOfCallsToServer = 0;
        numberOfItsMeCalls = 0;
        numberOfCanIComeInCalls = 0;
        comeIn = 0;
        itsMe = 0;
        introduce.set(false);
    }

    @Test
    public void testWorkJustFine()
    {
        tcase = TestCase.WorkJustFine;
        doTest();
    }

    @Test
    public void testDequePop()
    {
        tcase = TestCase.DequePop;
        doTest();
     }

    @Test
    public void testExtraCall()
    {
        tcase = TestCase.ExtraCall;
        doTest();
    }

    public void doTest ()
    {
        server.canIComeIn();
        int[] results = new int [] {numberOfCallsToServer,numberOfCanIComeInCalls,numberOfItsMeCalls};
        Assert.assertArrayEquals(expected.get(tcase),results);

    }

}
