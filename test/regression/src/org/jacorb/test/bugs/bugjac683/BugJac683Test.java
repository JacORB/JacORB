package org.jacorb.test.bugs.bugjac683;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.orb.AnyServer;
import org.jacorb.test.orb.AnyServerHelper;
import org.jacorb.test.orb.AnyServerImpl;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

public class BugJac683Test extends ClientServerTestCase
{
    private AnyServer server;

    public BugJac683Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    public static Test suite()
    {
        if (TestUtils.isJ2ME())
        {
            return new TestSuite();
        }

        TestSuite suite = new TestSuite(BugJac683Test.class.getName());

        ClientServerSetup setup = new ClientServerSetup(suite, AnyServerImpl.class.getName());

        TestUtils.addToSuite(suite, setup, BugJac683Test.class);

        return setup;
    }

    protected void setUp() throws Exception
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }
    
    
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
