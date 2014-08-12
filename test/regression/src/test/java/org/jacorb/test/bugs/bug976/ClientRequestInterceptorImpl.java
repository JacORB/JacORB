package org.jacorb.test.bugs.bug976;

import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.ORBPackage.InvalidName;
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

    public ClientRequestInterceptorImpl(String string, ORB orb, int slot)
    {
        this.name = string;
        this.orb = orb;
        this.slot = slot;
    }

    @Override
    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        Current current = getPICurrent(orb);

        Any any = orb.create_any();
        any.insert_string("Some Information to other interception point");
        try
        {
            current.set_slot(slot, any);
        }
        catch (InvalidSlot e)
        {
            throw new INTERNAL("Invalid Slot access");
        }
    }

    @Override
    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
        Current current = getPICurrent(orb);
        Any any;
        try
        {
            any = current.get_slot(slot);
        }
        catch (InvalidSlot e)
        {
            throw new INTERNAL("Invalid Slot access");
        }
        if (any.type().kind().value() == TCKind._tk_null)
        {
            throw new INTERNAL("Information from send_request is not there!");
        }
        else
        {
            TestUtils.getLogger().debug("receive_exception working as expected!");
            TestUtils.getLogger().debug("Info: " + any.extract_string());
        }
    }

    @Override
    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    @Override
    public void receive_reply(ClientRequestInfo ri)
    {
        Current current = getPICurrent(orb);
        Any any;
        try
        {
            any = current.get_slot(slot);
        }
        catch (InvalidSlot e)
        {
            throw new INTERNAL("Invalid Slot access");
        }
        if (any.type().kind().value() == TCKind._tk_null)
        {
            throw new INTERNAL("Information from send_request is not there!");
        }
        else
        {
            TestUtils.getLogger().debug("receive_reply working as expected!");
            TestUtils.getLogger().debug("Info: " + any.extract_string());
        }
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

    /**
     * Recupera o {@link Current} da thread em execu��o do ORB associado.
     *
     * @param orb
     *            o orb utilizado.
     * @return o {@link Current}.
     */
    private Current getPICurrent(ORB orb)
    {
        org.omg.CORBA.Object obj;
        try
        {
            obj = orb.resolve_initial_references("PICurrent");
        }
        catch (InvalidName e)
        {
            throw new INTERNAL(e.toString ());
        }
        return CurrentHelper.narrow(obj);
    }
}
