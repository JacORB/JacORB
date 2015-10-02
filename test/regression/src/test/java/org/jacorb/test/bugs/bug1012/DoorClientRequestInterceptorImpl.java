package org.jacorb.test.bugs.bug1012;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_PERMISSIONHelper;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

final public class DoorClientRequestInterceptorImpl extends LocalObject implements ClientRequestInterceptor
{

    private String name;
    private ORB orb;
    private Codec codec;

    public DoorClientRequestInterceptorImpl(String string, ORB orb, Codec codec)
    {
        this.name = string;
        this.orb = orb;
        this.codec = codec;
    }

    private String getSecret(String op)
    {
        int i = 1;
        if (op.equals("canIComeIn"))
        {
            i = Bug1012Test.comeIn++;
            // this is a workaround to the "ExtraCall" case.
            if (i % 3 == 0)
            {
                return "Any wrong secret";
            }
            else
            {
                return CORRECT_SECRET.value;
            }
            // END workaround
        }
        else if (op.equals("itsMe"))
        {
            i = Bug1012Test.itsMe++;
        }

        if (i % 2 == 0)
        {
            return "Any wrong secret";
        }
        else

        {
            return CORRECT_SECRET.value;
        }
    }

    @Override public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        String operation = ri.operation();
        TestUtils.getLogger().debug("send_request: " + operation);
        Bug1012Test.numberOfCallsToServer++;
        if(operation.equals("canIComeIn")) {
          Bug1012Test.numberOfCanIComeInCalls++;
        } else if(operation.equals("itsMe")) {
          Bug1012Test.numberOfItsMeCalls++;
        }
        TestUtils.getLogger().debug("total calls: " + Bug1012Test.numberOfCallsToServer +
                " canIcomeIn: " + Bug1012Test.numberOfCanIComeInCalls + " itsMe " + Bug1012Test.numberOfItsMeCalls);
        if (Bug1012Test.introduce.compareAndSet(true, false))
        {
            switch (Bug1012Test.tcase)
            {
                case WorkJustFine:
                    TestUtils.getLogger().debug("calling knock_knock");
                    Bug1012Test.server.knock_knock("Penny");
                    break;
                case DequePop:
                    Bug1012Test.server.knock_knock("Penny");
                    // 2 or more knock knock causes the bug
                    Bug1012Test.server.knock_knock("Penny");
                    Bug1012Test.server.knock_knock("Penny");
                    break;
                case ExtraCall:
                    // this cause an "extra" send_request execution.
                    // cannot understand what happened to the previous one.
                    Bug1012Test.server.itsMe("Sheldon");
                    break;
            }
        }

        Any anyCredential = orb.create_any();
        String secret = getSecret(operation);
        TestUtils.getLogger().debug(operation + " -> secret = " + secret);

        Credential credential = new Credential(secret);
        CredentialHelper.insert(anyCredential, credential);
        byte[] encodedCredential;
        try
        {
            encodedCredential = codec.encode_value(anyCredential);
        }
        catch (InvalidTypeForEncoding e)
        {
            String message = "Unexpected error encoding credential";
            throw new INTERNAL(message);
        }
        ServiceContext requestServiceContext = new ServiceContext(CredentialContextId.value, encodedCredential);
        ri.add_request_service_context(requestServiceContext, false);
    }

    @Override public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
        TestUtils.getLogger().debug("receive_exception: " + ri.operation());
        if (ri.operation().equals("canIComeIn"))
        {
            Bug1012Test.introduce.set(true);
        }
        if (ri.received_exception_id().equals(NO_PERMISSIONHelper.id()))
        {
            throw new ForwardRequest(ri.target());
        }
    }

    @Override public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {

    }

    @Override public void receive_reply(ClientRequestInfo ri)
    {

    }

    @Override public void send_poll(ClientRequestInfo ri)
    {

    }

    @Override public void destroy()
    {

    }

    @Override public String name()
    {
        return this.name;
    }

}