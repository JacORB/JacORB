package org.jacorb.test.bugs.bugjac683;

import static org.junit.Assert.fail;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.AnyServer;
import org.jacorb.test.orb.AnyServerHelper;
import org.jacorb.test.orb.AnyServerImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

public class BugJac683Test extends ClientServerTestCase
{
    private AnyServer server;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup(AnyServerImpl.class.getName());
    }

    @Before
    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testSetValue ()
    {
        org.omg.CORBA.Object obj = null;
        org.omg.DynamicAny.DynAnyFactory factory = null;
        org.omg.DynamicAny.DynFixed dynFixed = null;

        try
        {
            obj = setup.getClientOrb().resolve_initial_references( "DynAnyFactory" );
        }
        catch (InvalidName e)
        {
            fail("Failed on DynAnyFactory getting: " + e.getMessage());
        }

        factory =  org.omg.DynamicAny.DynAnyFactoryHelper.narrow( obj );

        try
        {
            dynFixed = ( org.omg.DynamicAny.DynFixed ) factory.create_dyn_any_from_type_code(
                    setup.getClientOrb().create_fixed_tc( ( short ) 6, ( short ) 3 ) );
        }
        catch (InconsistentTypeCode e)
        {
            fail("Failed on fixed typecode creation: " + e.getMessage());
        }

        // should be ok
        checkLocalValue(dynFixed, "0123.450d");
        checkLocalValue(dynFixed, "+556.02");
        checkLocalValue(dynFixed, "-556.23");

        // incorrect value
        checkLocalValue(dynFixed, "-2526.23", true);
    }

    @Test
    public void testGetValue()
    {
        org.omg.CORBA.Object obj = null;
        org.omg.DynamicAny.DynAnyFactory factory = null;
        org.omg.DynamicAny.DynFixed dynFixed = null;

        try
        {
            obj = setup.getClientOrb().resolve_initial_references( "DynAnyFactory" );
        }
        catch (InvalidName e)
        {
            fail("Failed on DynAnyFactory getting: " + e.getMessage());
        }

        factory =  org.omg.DynamicAny.DynAnyFactoryHelper.narrow( obj );

        try
        {
            dynFixed = ( org.omg.DynamicAny.DynFixed ) factory.create_dyn_any_from_type_code(
                    setup.getClientOrb().create_fixed_tc( ( short ) 6, ( short ) 2 ) );
        }
        catch (InconsistentTypeCode e)
        {
            fail("Failed on fixed typecode creation: " + e.getMessage());
        }

        checkRemoteValue(dynFixed, "0123.450d");
        checkRemoteValue(dynFixed, "+556.02");
        checkRemoteValue(dynFixed, "-556.23");
    }

    private void checkRemoteValue(org.omg.DynamicAny.DynFixed dynFixed, String value)
    {
        org.omg.CORBA.Any any = null;
        org.omg.CORBA.Any returned = null;

        any = setup.getClientOrb().create_any();

        try
        {
            dynFixed.set_value(value);
            any = dynFixed.to_any();
        }
        catch (Exception e)
        {
            fail("Incorrect value: '" + value + "' in value's setting: " + e.getMessage());
        }


        try
        {
            returned = server.bounce_any(any);
            dynFixed.from_any(returned);
        }
        catch (Exception e)
        {
            fail("Returned value '" + returned.toString() + "' is incorrect: " + e.getMessage());
        }
    }

    private void checkLocalValue(org.omg.DynamicAny.DynFixed dynFixed, String value)
    {
        checkLocalValue(dynFixed, value, false);
    }

    private void checkLocalValue(org.omg.DynamicAny.DynFixed dynFixed, String value, boolean expectedFail)
    {
        try
        {
            dynFixed.set_value(value);
            if (expectedFail)
            {
                fail( "Value '" + value + "' should be unacceptable. InvalidValue exception should be thrown.");
            }
        }
        catch (Exception e)
        {
            if (!expectedFail)
            {
                fail("Failed during set_value  '" + value + "': " + e.getMessage());
            }
        }
    }
}
