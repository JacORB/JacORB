package org.jacorb.test.bugs.bug923;

import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class Bug923Test extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private org.omg.CORBA.Object server;

    @Before
    public void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     */
    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties client_props = new Properties();
        Properties server_props = new Properties();

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.MyInitializer",
                         "org.jacorb.test.bugs.bug923.MyInitializer");

        setup = new ClientServerSetup(
                              Server.class.getName(),
                              "org.jacorb.test.bugs.bug923.GoodDayImpl",
                              client_props,
                              server_props);
    }

    @Test
    public void test_locatorinterceptors()
    {
        // and narrow it to HelloWorld.GoodDay
        // if this fails, a BAD_PARAM will be thrown
        DayFactory gdayFactory = DayFactoryHelper.narrow( server );

        Base base = gdayFactory.getDay();

        GoodDay goodDay = GoodDayHelper.narrow( base );

        goodDay.hello_simple("Hey Mike");

        gdayFactory.deleteDay(goodDay);
    }
}
