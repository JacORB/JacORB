package org.jacorb.test.orb.dynany;

import org.jacorb.test.common.ORBTestCase;

public class DynAnyXXXTestCase extends ORBTestCase
{
    protected org.omg.DynamicAny.DynAnyFactory factory = null;

    protected final void doSetUp() throws Exception
    {
        org.omg.CORBA.Object obj = orb.resolve_initial_references("DynAnyFactory");
        factory = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(obj);
    }

    protected void doTearDown()
    {
        factory = null;
    }
}
