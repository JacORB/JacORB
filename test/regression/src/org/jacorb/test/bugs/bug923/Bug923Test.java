package org.jacorb.test.bugs.bug923;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;


public class Bug923Test extends ClientServerTestCase
{
    /**
     * <code>server</code> is the server reference.
     */
    private org.omg.CORBA.Object server;

    /**
     * <code>TestCaseImpl</code> constructor for the suite.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public Bug923Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> for junit.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    /**
     * <code>suite</code> initialise the tests with the correct environment.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( "923 - nonretain test" );

        Properties client_props = new Properties();
        Properties server_props = new Properties();

        client_props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        server_props.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        server_props.put("org.omg.PortableInterceptor.ORBInitializerClass.MyInitializer",
                         "org.jacorb.test.bugs.bug923.MyInitializer");

        ClientServerSetup setup =
        new ClientServerSetup(suite,
                              Server.class.getName(),
                              "org.jacorb.test.bugs.bug923.GoodDayImpl",
                              client_props,
                              server_props);

        TestUtils.addToSuite(suite, setup, Bug923Test.class);

        return setup;
    }

    public void test_locatorinterceptors()
    {
        // and narrow it to HelloWorld.GoodDay
        // if this fails, a BAD_PARAM will be thrown
        DayFactory gdayFactory = DayFactoryHelper.narrow( server );

        Base base = gdayFactory.getDay();

        GoodDay goodDay = GoodDayHelper.narrow( base );

        System.out.println( goodDay.hello_simple("Hey Mike") );

        System.out.println("Calling deleteDay");
        gdayFactory.deleteDay(goodDay);
        System.out.println("deleteDay complete");
    }
}
