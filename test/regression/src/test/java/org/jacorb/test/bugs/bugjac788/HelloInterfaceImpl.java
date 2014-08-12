package org.jacorb.test.bugs.bugjac788;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;


public class HelloInterfaceImpl extends HelloInterfacePOA {

    /**
     * A parent POA used for the container activation
     */
    private org.omg.PortableServer.POA poa;

    private int hello;

   private ComputInterface comput;


    public HelloInterfaceImpl(
        ORB orb, org.omg.PortableServer.POA poa, ComputInterface comput) {
        this.poa = poa;
        this.comput = comput;
        this.hello = 0;
    }

    @Override
    public org.omg.PortableServer.POA _default_POA() {
        return this.poa;
    }

    @Override
    public void hello() {
        this.hello++ ;

        comput.get_result(this.hello * 100);
    }

    @Override
    public void send_TRANSIENT_exception() {
        // TODO Auto-generated method stub
        throw new TRANSIENT(0, CompletionStatus.COMPLETED_MAYBE);
    }
}
