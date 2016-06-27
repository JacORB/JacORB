package org.jacorb.test.bugs.bug1014;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSIONHelper;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ORBInit extends LocalObject
                                       implements ORBInitializer {
	public void pre_init(ORBInitInfo info) {
		try {
			info.add_client_request_interceptor(new RetryOnDenyInterceptor());
		} catch (DuplicateName e) {
			e.printStackTrace();
		}
	}

	public void post_init(ORBInitInfo info) { /* empty */ }

	private static class RetryOnDenyInterceptor extends LocalObject
	                                            implements ClientRequestInterceptor {

		public void send_request(ClientRequestInfo ri) throws ForwardRequest { /* empty */ }

		public void send_poll(ClientRequestInfo ri) { /* empty */ }

		public void receive_reply(ClientRequestInfo ri) { /* empty */ }

		public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
			if (ri.received_exception_id().equals(NO_PERMISSIONHelper.id())) {
				throw new ForwardRequest(ri.target());
			}
		}

		public void receive_other(ClientRequestInfo ri) throws ForwardRequest { /* empty */ }

		public String name() { return "my_client_interceptor"; }

		public void destroy() { /* empty */ }

	}
}
