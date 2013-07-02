package org.jacorb.test.bugs.bugjac788;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;


public class HelloInterfaceImpl extends HelloInterfacePOA {

    private ORB orb;

    /**
     * A parent POA used for the container activation
     */
    private org.omg.PortableServer.POA poa;

    private int hello;

   private ComputInterface comput;


    public HelloInterfaceImpl(
        ORB orb, org.omg.PortableServer.POA poa, ComputInterface comput) {
        this.orb = orb;
        this.poa = poa;
        this.comput = comput;
        this.hello = 0;
    }

    public org.omg.PortableServer.POA _default_POA() {
        return this.poa;
    }

    public void hello() {
        this.hello++ ;
        System.out.println(" <####### HELLO " + this.hello + "  ######>");

        try {
            long result = comput.get_result(this.hello * 100);
            System.out.println("result = " + result);
        }
        catch (SystemException e) {
            System.err.println("CORBA::SystemException raised : " + e.getMessage());
        }

    }

    public void send_TRANSIENT_exception() {
        // TODO Auto-generated method stub
        throw new TRANSIENT(0, CompletionStatus.COMPLETED_MAYBE);
    }
}
