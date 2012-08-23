package org.jacorb.test.bugs.bug927;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

public class CTXPassingTest extends ClientServerTestCase
{
    private org.omg.CORBA.Object server;

    public CTXPassingTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties severProp = new Properties();
        severProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                              MyInitializer.class.getName());

        Properties clientProp = new Properties();
        clientProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                               MyInitializer.class.getName());

        clientProp.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        clientProp.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");

        TestSuite suite = new TestSuite(CTXPassingTest.class.getName());

        ClientServerSetup setup = new ClientServerSetup(suite, Server.class.getName()
                                      , "org.jacorb.test.bugs.bug927.TestObjectImpl"
                                      , clientProp, severProp);
        TestUtils.addToSuite(suite, setup, CTXPassingTest.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = setup.getServerObject();
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testCTXPassingTest()
    {
        TestObject testObject = null;
        try
        {
            testObject = TestObjectHelper.narrow( server );
            Current current = (Current) setup.getClientOrb().resolve_initial_references( "PICurrent" );

            Any any = setup.getClientOrb().create_any();
            any.insert_string( "JacOrbRocks" );

            current.set_slot( MyInitializer.slot_id, any );
        }
        catch (InvalidName e)
        {
            e.printStackTrace();
        }
        catch (InvalidSlot e)
        {
            e.printStackTrace();
        }

        try {
           testObject.foo();
        } catch (InterceptorOrderingException e) {
            e.printStackTrace();
           fail( "Unexpected exception: " + e);
        }
    }
}
