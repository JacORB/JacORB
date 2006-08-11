package org.jacorb.test.transport;

import org.jacorb.orb.ORB;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

// implementation of org.omg.PortableInterceptor.ORBInitializerOperations
// interface
public abstract class AbstractOrbInitializer extends org.omg.CORBA.LocalObject implements
                                                                            ORBInitializer {

    private final AbstractTester clienttester_;
    private final AbstractTester servertester_;

    protected AbstractOrbInitializer(AbstractTester clienttester, AbstractTester servertester) {

        clienttester_ = clienttester;
        servertester_ = servertester;
    }


    private AbstractOrbInitializer() {

        this (null, null);
    }


    /**
     * 
     * @param param1
     *            <description>
     */
    public void pre_init(ORBInitInfo info) {

    }


    /**
     * 
     * @param param1
     *            <description>
     */
    public void post_init(ORBInitInfo info) {

        ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB ();

        try {
            if (clienttester_ != null)
                info.add_client_request_interceptor (new ClientInterceptor (orb,
                                                                            clienttester_));

            if (servertester_ != null)
                info.add_server_request_interceptor(new ServerInterceptor(orb, 
                                                                          servertester_));
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }
}// AbstractOrbInitializer

