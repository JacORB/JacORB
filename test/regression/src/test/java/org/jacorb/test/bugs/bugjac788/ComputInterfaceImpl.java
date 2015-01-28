package org.jacorb.test.bugs.bugjac788;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;

public class ComputInterfaceImpl extends ComputInterfacePOA
{
    /**
     * A parent POA used for the container activation
     */
    private org.omg.PortableServer.POA poa;

    public ComputInterfaceImpl(ORB orb, org.omg.PortableServer.POA poa)
    {
        this.poa = poa;
    }

    public org.omg.PortableServer.POA _default_POA()
    {
        return this.poa;
    }

    public int get_result(int timeMs) throws SystemException
    {
        try
        {
            Thread.sleep(timeMs);
        }
        catch (InterruptedException e)
        {
        }

        return timeMs;
    }
}
