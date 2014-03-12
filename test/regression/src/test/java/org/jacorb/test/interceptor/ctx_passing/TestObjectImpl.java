package org.jacorb.test.interceptor.ctx_passing;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.CORBA.INTERNAL;

public class TestObjectImpl extends TestObjectPOA implements Configurable
{
    private ORB orb;

    public void foo()
    {
        try
        {
            Current current = (Current) orb.resolve_initial_references( "PICurrent" );

            Any any = current.get_slot( ServerInitializer.slot_id );

            ((org.jacorb.orb.ORB)orb).getConfiguration ().getLogger("org.jacorb.test").debug ("Server extracted from PICurrent: >>" + any.extract_string() + "<<");
        }
        catch( Exception e )
        {
            throw new INTERNAL (e.getMessage());
        }
    }

    public void configure(Configuration arg) throws ConfigurationException
    {
        orb = arg.getORB();
    }
}
