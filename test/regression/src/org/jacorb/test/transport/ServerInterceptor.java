package org.jacorb.test.transport;

import org.slf4j.Logger;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class ServerInterceptor extends org.omg.CORBA.LocalObject implements
                                                                ServerRequestInterceptor {

    private static ServerInterceptor instance_ = null;

    private final Logger logger_;
    private final AbstractTester tester_;
    private final ORB orb_;
    
    private int ninterceptions_;
    private int nfailures_;

    public ServerInterceptor(ORB orb, AbstractTester tester) {

        this.tester_ = tester;
        this.orb_ = orb;
        
        this.ninterceptions_ = 0;
        this.nfailures_ = 0;
        logger_ = ((org.jacorb.orb.ORB) orb).getConfiguration ()
                                            .getLogger ("jacorb.test.transport.si");

        logger_.info ("ServerInterceptor::created");

        // This is here to facilitate obtaining the correct instance from the
        // test code
        synchronized (this.getClass ()) {
            if (instance_ != null)
            {
                RuntimeException ex = new RuntimeException ("A server interceptor has already been instantiated. Can't have >1 - sorry.");
                logger_.error("interceptor", ex);
                throw ex;
            }

            instance_ = this;
        }
    }


    public String name() {

        return "ServerInterceptor";
    }


    public void destroy() {

    }


    public void receive_request_service_contexts(ServerRequestInfo ri)
                    throws ForwardRequest {

        this.ninterceptions_++;
        logger_.info ("ServerInterceptor::receive_request_service_contexts: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_, logger_);
    }


    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {

        this.ninterceptions_++;
        logger_.info ("ServerInterceptor::receive_request: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_, logger_);
    }


    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {

        this.ninterceptions_++;
        logger_.info ("ServerInterceptor::send_exception: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_, logger_);
    }


    public void send_other(ServerRequestInfo ri) throws ForwardRequest {

        this.ninterceptions_++;
        logger_.info ("ServerInterceptor::send_other: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_, logger_);
    }


    public void send_reply(ServerRequestInfo ri) {

        this.ninterceptions_++;
        logger_.info ("ServerInterceptor::send_reply: ["+ri.request_id()+"] "+ri.operation());
        tester_.test_transport_current (orb_, logger_);
    }


    public static int failures() {

        if (instance_ == null) return 0;

        return instance_.nfailures_;
    }


    public static int interceptions() {

        if (instance_ == null) return 0;

        return instance_.ninterceptions_;
    }


    public static void interceptions(int i) {

        if (instance_ != null) instance_.ninterceptions_ = i;

    }


    public static void reset() {

        instance_ = null;
    }


}// ClientInterceptor
