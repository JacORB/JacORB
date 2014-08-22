package org.jacorb.test.bugs.bug927;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.CORBA.INTERNAL;
import org.slf4j.Logger;

public class TestObjectImpl extends TestObjectPOA
{
    private ORB orb;
    private Logger logger;

    TestObjectImpl(ORB orb)
    {
       this.orb = orb;

       logger = ((org.jacorb.orb.ORB)orb).getConfiguration ().getLogger("org.jacorb.test");
   }

    public void foo() throws InterceptorOrderingException
    {
        try
        {
            Current current = (Current) orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot( MyInitializer.slot_id );

            String s = any.extract_string();

            logger.debug ("TestObjectImpl.foo, extracted from PICurrent: >>" + s + "<<");

            String expectedPiFlow = "JacOrbRocks:receive_request_service_contexts:preinvoke:receive_request:foo";
            if (! expectedPiFlow.equals(s))
            {
                throw new InterceptorOrderingException();
            }

            logger.debug ("TestObjectImpl.foo calling bar()");

            TestObjectHelper.narrow(_this_object()).bar();
        }
        catch (InterceptorOrderingException e)
        {
           throw e;
        }
        catch( Exception e )
        {
            throw new INTERNAL ("Caught " + e);
        }
    }

    public void bar() throws InterceptorOrderingException
    {
        try
        {
            logger.debug("TestObjectImpl.bar called");

            Current current = (Current) orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot( MyInitializer.slot_id );

            String s = any.extract_string();

            logger.debug ("TestObjectImpl.bar, extracted from PICurrent: >>" + s + "<<");

            String expectedPiFlow = "JacOrbRocks:receive_request_service_contexts:preinvoke:receive_request:foo:receive_request_service_contexts:preinvoke:receive_request:bar";
            if (! expectedPiFlow.equals(s))
            {
                throw new InterceptorOrderingException();
            }
        }
        catch (InterceptorOrderingException e)
        {
           throw e;
        }
        catch( Exception e )
        {
            throw new INTERNAL ("Caught " + e);
        }
    }

    public void configure(Configuration arg) throws ConfigurationException
    {
        orb = arg.getORB();
    }
}
