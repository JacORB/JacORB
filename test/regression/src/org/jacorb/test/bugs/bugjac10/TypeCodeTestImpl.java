package org.jacorb.test.bugs.bugjac10;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodeHolder;


/**
 * @author Carol Jordon
 * @version $Id$
 */
public class TypeCodeTestImpl extends TypeCodeTestServerPOA
{
    public TypeCode respond (boolean compact,
                             TypeCode argin,
                             TypeCodeHolder argout,
                             TypeCodeHolder arginout)
    {
        argout.value = C_exceptHelper.type();
        arginout.value = C_exceptHelper.type();

        return C_exceptHelper.type();
    }
}
