package org.jacorb.test.bugs.bugjac262;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;

public class ComplexTypeCodesServerImpl extends ComplexTypeCodesServerPOA
{
    public boolean passAny( Any any )
    {
        try
        {
            System.err.println (any.extract_TypeCode());
        }
        catch (Exception e )
        {
            e.printStackTrace();
            throw new INTERNAL();
        }

        return true;
    }
}
