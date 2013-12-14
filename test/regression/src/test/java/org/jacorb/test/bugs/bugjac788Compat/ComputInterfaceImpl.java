
package org.jacorb.test.bugs.bugjac788Compat;

import org.omg.CORBA.SystemException;

public class ComputInterfaceImpl extends ComputInterfacePOA
{
    /**
     * A parent POA used for the container activation
     */
    private org.omg.PortableServer.POA poa;


    /**
     * Purpose:
     * <p> [Constructor description if necessary]
     *
     *@param
     **/
    public ComputInterfaceImpl(org.omg.PortableServer.POA poa) {
        this.poa = poa;
    }

    public org.omg.PortableServer.POA _default_POA() {
        return this.poa;
    }

    public int get_result(int timeMs) throws SystemException {
        try
        {
            Thread.sleep (timeMs);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return timeMs;
    }
}
