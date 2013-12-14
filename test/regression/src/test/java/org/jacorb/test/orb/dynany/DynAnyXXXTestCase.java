package org.jacorb.test.orb.dynany;

import org.jacorb.test.common.ORBTestCase;
import org.junit.Before;

public class DynAnyXXXTestCase extends ORBTestCase
{
    protected org.omg.DynamicAny.DynAnyFactory factory = null;

    @Before
    public final void dynFactorySetup() throws Exception
    {
        org.omg.CORBA.Object obj = orb.resolve_initial_references("DynAnyFactory");
        factory = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(obj);
    }
}
