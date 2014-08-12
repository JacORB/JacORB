package org.jacorb.test.transport;

import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

public class ClientInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor {

    private static ClientInterceptor instance_ = null;

    private int count_ = 0;

    private final AbstractTester tester_;
    private final ORB orb_;


    public ClientInterceptor(ORB orb, AbstractTester tester) {

        tester_ = tester;
        orb_ = orb;

        synchronized (this.getClass ()) {
            instance_ = this;
        }
    }


    @Override
    public String name() {

        return "ClientInterceptor";
    }


    @Override
    public void destroy() {

    }


    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest {

        this.count_++;
        TestUtils.getLogger().debug ("ClientInterceptor::send_request: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_);
    }


    @Override
    public void send_poll(ClientRequestInfo ri) {

        this.count_++;
        TestUtils.getLogger().debug ("ClientInterceptor::send_poll: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_);
    }


    @Override
    public void receive_reply(ClientRequestInfo ri) {

        this.count_++;
        TestUtils.getLogger().debug ("ClientInterceptor::receive_reply: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_);
    }


    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {

        this.count_++;
        TestUtils.getLogger().debug ("ClientInterceptor::receive_exception: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_);
    }


    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest {

        this.count_++;
        TestUtils.getLogger().debug ("ClientInterceptor::receive_other");
        tester_.test_transport_current (orb_);
    }


    static int interceptions() {

        if (instance_ == null) return 0;
        return instance_.count_;
    }


    public static void interceptions(int i) {

        if (instance_ != null) instance_.count_ = i;

    }

}// ClientInterceptor
