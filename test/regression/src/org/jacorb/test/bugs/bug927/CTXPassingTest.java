package org.jacorb.test.bugs.bug927;

import static org.junit.Assert.fail;
import java.util.Properties;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.CommonSetup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

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

        clientProp.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
        clientProp.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");


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
