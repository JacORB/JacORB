package org.jacorb.test.bugs.bug960;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

public final class Initializer extends LocalObject implements ORBInitializer
{

    public void pre_init(ORBInitInfo info)
    {
    }

    private Codec createCodec(ORBInitInfo info)
    {
        org.omg.CORBA.Object obj;
        try
        {
            obj = info.resolve_initial_references("CodecFactory");
        }
        catch (InvalidName e)
        {
            String message = "Unexpected error accessing Codec Factory";
            throw new INITIALIZE(message);
        }
        CodecFactory codecFactory = CodecFactoryHelper.narrow(obj);

        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1,
                (byte) 2);
        try
        {
            return codecFactory.create_codec(encoding);
        }
        catch (UnknownEncoding e)
        {
            String message = "Unexpected error of encoding";
            throw new INITIALIZE(message);
        }
    }

    public void post_init(ORBInitInfo info)
    {
        Codec codec = createCodec(info);

        ORBInitInfoImpl infoImpl = (ORBInitInfoImpl) info;
        try
        {
            info.add_client_request_interceptor(new ClientRequestInterceptorImpl(
                    "ClientRequestInterceptor", codec));
            info.add_server_request_interceptor(new ServerRequestInterceptorImpl(
                    "ServerRequestInterceptor", infoImpl.getORB(), codec));
        }
        catch (DuplicateName e)
        {
            String message = "Unexpected error registering interceptors";
            throw new INITIALIZE(message);
        }
    }
}
