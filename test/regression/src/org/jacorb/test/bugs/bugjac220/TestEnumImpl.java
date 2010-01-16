package org.jacorb.test.bugs.bugjac220;

import org.omg.CORBA.Any;

/**
 * Implementation to test passing of an enum TypeCode.  This is
 * to test CDRInputStream so the push method does not need to do
 * anything.
 */
public class TestEnumImpl
   extends TestEnumPOA
{
    public TestEnumImpl()
    {
    }

    public Any push(Any enumAny)
    {
        return enumAny;
    }
}
