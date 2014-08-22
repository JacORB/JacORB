package org.jacorb.test.bugs.bug980;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

public final class Initializer extends LocalObject implements ORBInitializer {

  @Override
  public void pre_init(ORBInitInfo info) {
    int slot = info.allocate_slot_id();
    ORBMediator mediator = new ORBMediator(slot);
    try {
      info.register_initial_reference(ORBMediator.INITIAL_REFERENCE_ID,
        mediator);
      info.add_client_request_interceptor(new DummyInterceptor());
    }
    catch (InvalidName e) {
      throw new INITIALIZE("Unexpected error setting initial reference");
    }
    catch (DuplicateName e) {
      throw new INITIALIZE("Duplicate name of interceptor");
    }

  }

  @Override
  public void post_init(ORBInitInfo info) {

  }

  public final static class ORBMediator extends LocalObject {
    private int slot;
    public static final String INITIAL_REFERENCE_ID = "MyORBMediator";

    public ORBMediator(int slot) {
      this.slot = slot;
    }

    public int getSlot() {
      return this.slot;
    }
  }

  public static class DummyInterceptor extends LocalObject implements
    ClientRequestInterceptor {

    @Override
    public void receive_exception(ClientRequestInfo arg0) throws ForwardRequest {
      // TODO Auto-generated method stub

    }

    @Override
    public void receive_other(ClientRequestInfo arg0) throws ForwardRequest {
      // TODO Auto-generated method stub

    }

    @Override
    public void receive_reply(ClientRequestInfo arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void send_poll(ClientRequestInfo arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void send_request(ClientRequestInfo arg0) throws ForwardRequest {
      // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
      // TODO Auto-generated method stub

    }

    @Override
    public String name() {
      return "Dummy";
    }

  }
}
