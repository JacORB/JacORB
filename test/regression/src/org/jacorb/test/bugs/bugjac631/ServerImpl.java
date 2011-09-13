package org.jacorb.test.bugs.bugjac631;

import org.omg.CORBA.INTERNAL;


/**
 * <code>ServerImpl</code> is a simple server for testing DII.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class ServerImpl extends MyServerPOA
{
    /**
     * <code>myFunction</code> is a simple function used when testing DII. It uses
     * IDL supplied by Thales.
     *
     * @param paramIn an <code>int</code> value. If larger than zero this throws
     *                an exception. If smaller than zero then the value is returned
     *                in paramOut.
     * @param paramOut an <code>org.omg.CORBA.IntHolder</code> value
     * @return an <code>int</code> value
     * @exception MyOwnException if an error occurs
     */
    public int myFunction(int paramIn, org.omg.CORBA.IntHolder paramOut) throws MyOwnException
    {
        if (paramIn > 0)
        {
            if (paramIn == 10)
            {
                throw new INTERNAL ("A system exception!");
            }
            if (paramIn == 20)
            {
                throw new NullPointerException ("A nullpointer exception!");
            }
            throw new MyOwnException (1, "Param1 > 0");
        }
        else if (paramIn < 0)
        {
            paramOut.value = paramIn;
        }
        return paramIn;
    }
}
