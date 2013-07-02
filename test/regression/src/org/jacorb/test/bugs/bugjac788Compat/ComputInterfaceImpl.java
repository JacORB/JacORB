
package org.jacorb.test.bugs.bugjac788Compat;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;

public class ComputInterfaceImpl extends ComputInterfacePOA
{

    private ORB orb;

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
    public ComputInterfaceImpl(ORB orb, org.omg.PortableServer.POA poa) {
        this.orb = orb;
        this.poa = poa;
    }

    public org.omg.PortableServer.POA _default_POA() {
        return this.poa;
    }

    public int get_result(int timeMs) throws SystemException {
        System.out.println(" <####### Comput " + timeMs + "  ######>");
        try {
           Thread.sleep (timeMs);
//            OS.sleep((int)timeMs);
        }
        catch (InterruptedException e) {
             e.printStackTrace();
        }
        System.out.println(" <####### End of Comput " + timeMs + "  ######>");

        return timeMs;
    }
}
