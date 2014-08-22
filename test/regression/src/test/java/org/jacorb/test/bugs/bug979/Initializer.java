package org.jacorb.test.bugs.bug979;

import org.jacorb.orb.portableInterceptor.ORBInitInfoImpl;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public final class Initializer extends LocalObject implements ORBInitializer
{

    /** Major da versão do codificador. */
    private static final byte ENCODING_CDR_ENCAPS_MAJOR_VERSION = 1;
    /** Minor da versão do codificador. */
    private static final byte ENCODING_CDR_ENCAPS_MINOR_VERSION = 2;

    @Override
    public void pre_init(ORBInitInfo info)
    {
        ORBInitInfoImpl infoImpl = (ORBInitInfoImpl) info;
        Codec codec = createCodec(infoImpl);
        try
        {
            info.add_client_request_interceptor(new ClientRequestInterceptorImpl(
                    "ClientRequestInterceptor", infoImpl.getORB(), codec));
            info.add_server_request_interceptor(new ServerRequestInterceptorImpl(
                    "ServerRequestInterceptor", infoImpl.getORB(), codec));
        }
        catch (DuplicateName e)
        {
            String message = "Unexpected error registering interceptors";
            throw new INITIALIZE(message);
        }
    }

    @Override
    public void post_init(ORBInitInfo info)
    {

    }

    /**
     * Cria uma instância do Codec a ser utilizado pelos interceptadores.
     * 
     * @param info
     *            informação do ORB
     * @return o Codec.
     */
    private Codec createCodec(ORBInitInfo info)
    {
        CodecFactory codecFactory = info.codec_factory();

        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                ENCODING_CDR_ENCAPS_MAJOR_VERSION, ENCODING_CDR_ENCAPS_MINOR_VERSION);

        try
        {
            return codecFactory.create_codec(encoding);
        }
        catch (UnknownEncoding e)
        {
            String message = "Unexpected error creating codec";
            throw new INITIALIZE(message);
        }
    }
}
