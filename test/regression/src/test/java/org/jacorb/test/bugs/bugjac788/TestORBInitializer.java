package org.jacorb.test.bugs.bugjac788;

import org.omg.PortableInterceptor.ORBInitInfo;

public class TestORBInitializer extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer {

    private static TestServerRequestInterceptorImpl serverInterceptor = null;
    private static TestClientRequestInterceptorImpl clientInterceptor = null;

    /**
     * Purpose: <p> Constructor
     *
     */
    public TestORBInitializer() {
    }

    /**
     * Purpose: <p> This operation is called during ORB initialization.
     *
     */
    public void pre_init(ORBInitInfo arg0) {
    }

    /**
     * Purpose: <p> This operation is called during ORB initialization.
     *
     * Note: the interceptors are registered in post_init() if their
     * constructors require ORBInitInfo.resolve_initial_reference(), which XXX
     * cannot be called in pre_init().
     *
     */
    public void post_init(ORBInitInfo info) {
        //
        // allocate needed slot_id
        //
        int isRequestIdSlotId = info.allocate_slot_id();

        //
        // Create and register the interceptors
        //
        serverInterceptor = new TestServerRequestInterceptorImpl(info, isRequestIdSlotId);
        clientInterceptor = new TestClientRequestInterceptorImpl(info, isRequestIdSlotId);

        try {
            info.add_server_request_interceptor(serverInterceptor);
            info.add_client_request_interceptor(clientInterceptor);
        }
        catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
        }
    }

}
