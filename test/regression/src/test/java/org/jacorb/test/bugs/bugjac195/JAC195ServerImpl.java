package org.jacorb.test.bugs.bugjac195;

/**
 * Implementation of a CORBA object
 */
public class JAC195ServerImpl extends JAC195ServerPOA
{
    /**
     * Count of number of connections opened - static so the listener can
     * update the value directly
     */
    public static int connectionsOpened;

    /**
     * Count of number of connections closed - static so the listener can
     * update the value directly
     */
    public static int connectionsClosed;

    /**
     * Returns the number of connections opened - used by the TestCaseImpl
     * to get the value from the server side
     */
    public int getConnectionsOpened()
    {
        return connectionsOpened;
    }

   /**
     * Returns the number of connections closed - used by the TestCaseImpl
     * to get the value from the server side
     */
    public int getConnectionsClosed()
    {
        return connectionsClosed;
    }
}
