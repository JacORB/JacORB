package test.dynany;

/**
 * Testing.java
 *
 *
 * Created: Wed Nov 28 10:06:41 2001
 *
 * @author <a href="mailto: "Jason Courage</a>
 */

import test.dynany.*;
import java.util.Properties;

public class T5
{
    private static org.omg.DynamicAny.DynAnyFactory factory = null;
    private static org.omg.CORBA.ORB orb = null;
  
    public T5 ()
    {
        // setup orb
        String [] args = new String [0];
        Properties props = new Properties ();

        props.setProperty
            ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty
            ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        orb = org.omg.CORBA.ORB.init (args, props);
      
        // setup DynAnyFactory
        org.omg.CORBA.Object obj = null;

        try
        {
            obj = orb.resolve_initial_references ("DynAnyFactory");
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName ex)
        {
            fail ("Failed to resolve DynAnyFactory: " + ex, ex);
            return;
        }
        try
        {
            factory = org.omg.DynamicAny.DynAnyFactoryHelper.narrow (obj);
        }
        catch (Throwable ex)
        {
            fail ("Failed to narrow to DynAnyFactory: " + ex, ex);
            return;
        }

        System.err.println ("***** Starting Test Set *****");
        testCreateFromAny ();
        System.err.println ("***** Starting Test Set *****");
        testGenerateAnyFromDynAny ();
    }


    public void testCreateFromAny ()
    {
        String msg;
        org.omg.DynamicAny.DynStruct dynAny = null;
        org.omg.CORBA.Any any = null;

        // test non-empty exception
        start ("Create DynAny from Any with non-empty exception");
        NonEmptyException type1 = new NonEmptyException (1, "Hello");
        any = orb.create_any ();
        NonEmptyExceptionHelper.insert (any, type1);
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct) factory.create_dyn_any (any);
            pass ("Successfully created DynAny from Any");
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from Any using ";
            msg += "DynAny::create_dyn_any operation: " + ex;
            fail (msg, ex);
        }

        // test empty exception
        start ("Create DynAny from Any with empty exception");
        EmptyException type2 = new EmptyException ();
        any = orb.create_any ();
        EmptyExceptionHelper.insert (any, type2);
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct) factory.create_dyn_any (any);
            pass ("Successfully created DynAny from Any");
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from Any using ";
            msg += "DynAny::create_dyn_any operation: " + ex;
            fail (msg, ex);
        }

        // test struct
        start ("Create DynAny from Any with struct");
        StructType type3 = new StructType (1, "Hello");
        any = orb.create_any ();
        StructTypeHelper.insert (any, type3);
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct) factory.create_dyn_any (any);
            pass ("Successfully created DynAny from Any");      
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from Any using ";
            msg += "DynAny::create_dyn_any operation: " + ex;
            fail (msg, ex);
        }
    }


    public void testGenerateAnyFromDynAny ()
    {
        String msg;
        org.omg.CORBA.Any any = null;
        org.omg.CORBA.TypeCode tc = null;
        org.omg.DynamicAny.DynStruct dynAny = null;

        // test non-empty exception
        start ("Create Any from DynAny with non-empty exception");
        tc = NonEmptyExceptionHelper.type ();
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct)
                factory.create_dyn_any_from_type_code (tc);
            try
            {
                any = dynAny.to_any ();
                pass ("Successfully created Any from DynAny");
            }
            catch (Throwable ex)
            {
                msg = "The DynAny::to_any operation failed to create an Any object";
                fail (msg, ex);
            }
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from TypeCode using ";
            msg += "DynAny::create_dyn_any_from_type_code operation: " + ex;
            fail (msg, ex);
        }

        // test empty exception
        start ("Create Any from DynAny with empty exception");
        tc = EmptyExceptionHelper.type ();
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct)
                factory.create_dyn_any_from_type_code (tc);         
            try
            {
                any = dynAny.to_any ();
                pass ("Successfully created Any from DynAny");
            }
            catch (Throwable ex)
            {
                msg = "The DynAny::to_any operation failed to create an Any object";
                fail (msg, ex);
            }
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from TypeCode using ";
            msg += "DynAny::create_dyn_any_from_type_code operation: " + ex;
            fail (msg, ex);
        }

        // test struct
        start ("Create Any from DynAny with struct");
        tc = StructTypeHelper.type ();
        try
        {
            dynAny = (org.omg.DynamicAny.DynStruct)
                factory.create_dyn_any_from_type_code (tc);         
            try
            {
                any = dynAny.to_any ();
                pass ("Successfully created Any from DynAny");
            }
            catch (Throwable ex)
            {
                msg = "The DynAny::to_any operation failed to create an Any object";
                fail (msg, ex);
            }
        }
        catch (Throwable ex)
        {
            msg = "Factory failed to create DynAny from TypeCode using ";
            msg += "DynAny::create_dyn_any_from_type_code operation: " + ex;
            fail (msg, ex);
        }
    }


    public static void main (String [] args)
    {
        new T5();
    }

    private static void fail (String msg, Throwable ex)
    {
        System.err.println ("Test failed with reason: " + msg);
        ex.printStackTrace (System.err);
        System.err.println ();
    }

    private static void pass (String msg)
    {
        System.err.println ("Test passed: " + msg);
        System.err.println ();
    }

    private static void start (String msg)
    {
        System.err.println ("Starting test: " + msg);
    }
   
}// Testing

