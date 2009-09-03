package org.jacorb.test.bugs.bugjac10;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodeHolder;


/**
 * <code>DNSServerImpl</code> is the server implementation for testing
 * DNS.  This exists purely to fit in with the hierarchy and to allow a
 * CORBA object to be created, to get an IOR.  It doesn't actually do
 * anything.  It has a ping method just so its not empty.
 *
 * @author <a href="mailto:cj@prismtechnologies.com"></a>
 * @version 1.0
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
