package org.jacorb.test.interceptor.ctx_passing;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

public class CTXPassingTest extends ClientServerTestCase
{
    private TestObject server;

    public CTXPassingTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        Properties severProp = new Properties();
        severProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                              ServerInitializer.class.getName());
        Properties clientProp = new Properties();
        clientProp.setProperty("org.omg.PortableInterceptor.ORBInitializerClass.a",
                               ClientInitializer.class.getName());

        TestSuite suite = new TestSuite(CTXPassingTest.class.getName());
        ClientServerSetup setup = new ClientServerSetup(suite, TestObjectImpl.class.getName(), clientProp, severProp);
        TestUtils.addToSuite(suite, setup, CTXPassingTest.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = TestObjectHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void testCTXPassingTest()
    {
        try
        {
            Current current = (Current) setup.getClientOrb().resolve_initial_references( "PICurrent" );

            Any any = setup.getClientOrb().create_any();
            any.insert_string( "This is a test!" );

            current.set_slot( ClientInitializer.slot_id, any );
        }
        catch (InvalidName e)
        {
            e.printStackTrace();
        }
        catch (InvalidSlot e)
        {
            e.printStackTrace();
        }

        server.foo();
    }
}
