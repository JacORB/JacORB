package org.jacorb.test.bugs.bug983;

import org.jacorb.test.bugs.bug979.CredentialContextId;
import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSIONHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.InvalidSlot;

final public class ClientRequestInterceptorImpl extends LocalObject implements
        ClientRequestInterceptor
{

    private String name;
    private ORB orb;
    private int slot;

    static Current getPICurrent(ORB orb)
    {
        org.omg.CORBA.Object obj = null;
        try
        {
            obj = orb.resolve_initial_references("PICurrent");
        }
        catch (InvalidName e)
        {
            throw new RuntimeException("Falha inesperada ao obter o PICurrent: " + e);
        }
        return CurrentHelper.narrow(obj);
    }

    public ClientRequestInterceptorImpl(String string, ORB orb, int slot)
    {
        this.name = string;
        this.orb = orb;
        this.slot = slot;
    }

    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        try
        {
            Any flag = ri.get_slot(this.slot);

            TestUtils.getLogger().debug("SLOT VALUE TYPE: " + flag.type().kind());

            if (flag.type().kind().value() != TCKind._tk_null)
            {
                ServiceContext requestServiceContext = new ServiceContext(
                        CredentialContextId.value, new byte[0]);
                ri.add_request_service_context(requestServiceContext, false);

                Current current = getPICurrent(orb);
                current.set_slot(this.slot, orb.create_any());
            }
        }
        catch (InvalidSlot e)
        {
            throw new RuntimeException("Falha inesperada ao obter acessar slot: " + e);
        }
        TestUtils.getLogger().debug("send request: " + ri.operation());
    }

    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
        TestUtils.getLogger().debug("receive exception: " + ri.operation());
        try
        {
            if (ri.received_exception_id().equals(NO_PERMISSIONHelper.id()))
            {
                Current current = getPICurrent(orb);
                Any flag = orb.create_any();
                flag.insert_boolean(true);
                current.set_slot(this.slot, flag);
                TestUtils.getLogger().debug("doing ForwardRequest: " + ri.operation());
                throw new ForwardRequest(ri.target());
            }
        }
        catch (InvalidSlot e)
        {
            throw new RuntimeException("Falha inesperada ao salvar no slot: " + e);
        }
    }

    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    @Override
    public void receive_reply(ClientRequestInfo ri)
    {
        TestUtils.getLogger().debug("receive reply: " + ri.operation());
    }

    @Override
    public void send_poll(ClientRequestInfo ri)
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public String name()
    {
        return this.name;
    }

}