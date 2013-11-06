package org.jacorb.test.bugs.bug927;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;

public class TestObjectImpl extends TestObjectPOA
{
    private ORB orb;

    TestObjectImpl(ORB orb) {
       this.orb = orb;
    }

    public void foo() throws InterceptorOrderingException
    {
        try
        {
            Current current = (Current) orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot( MyInitializer.slot_id );

             String s = any.extract_string();
            System.out.println("TestObjectImpl.foo, extracted from PICurrent: >>" +
                               s + "<<");

            String expectedPiFlow = "JacOrbRocks:receive_request_service_contexts:preinvoke:receive_request:foo";
            if (! expectedPiFlow.equals(s))
            {
                System.out.println ("### THROWING EX " + expectedPiFlow + " and " + s);
               throw new InterceptorOrderingException();
            }

            System.out.println("TestObjectImpl.foo calling bar()");
            TestObjectHelper.narrow(_this_object()).bar();

        }
        catch (InterceptorOrderingException e) {
           throw e;
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void bar() throws InterceptorOrderingException
    {
        try
        {
            System.out.println("TestObjectImpl.bar called");

            Current current = (Current) orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot( MyInitializer.slot_id );

            String s = any.extract_string();
            System.out.println("TestObjectImpl.bar, extracted from PICurrent: >>" +
                               s + "<<");

            String expectedPiFlow = "JacOrbRocks:receive_request_service_contexts:preinvoke:receive_request:foo:receive_request_service_contexts:preinvoke:receive_request:bar";
            if (! expectedPiFlow.equals(s))
            {
                // System.out.println ("### THROWING EX2 " + expectedPiFlow + " and " + s);
                throw new InterceptorOrderingException();
            }
        }
        catch (InterceptorOrderingException e) {
           throw e;
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void configure(Configuration arg) throws ConfigurationException
    {
        orb = arg.getORB();
    }
}
