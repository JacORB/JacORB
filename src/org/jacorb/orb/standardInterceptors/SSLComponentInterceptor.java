package org.jacorb.orb.standardInterceptors;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.jacorb.orb.*;
import org.jacorb.util.*;

/**
 * This interceptor creates an ssl TaggedComponent
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class SSLComponentInterceptor 
    extends LocalityConstrainedObject
    implements IORInterceptor
{
    private ORB orb = null;

    public SSLComponentInterceptor(ORB orb) {
        this.orb = orb;
    }
  
     public String name(){
        return "SSLComponentCreator";
    }

    // implementation of org.omg.PortableInterceptor.IORInterceptorOperations interface

    /**
     * Builds an ssl TaggedComponent.
     * Was formerly: ORB.makeSSLComponent()
     */

    public void establish_components(IORInfo info) 
    {
        try
        {
            org.omg.IIOP.Version v = 
                new org.omg.IIOP.Version((byte) 1, (byte) 1); // bnv
            org.omg.SSLIOP.SSL ssl = 
                new org.omg.SSLIOP.SSL ( Environment.supportedBySSL(),
                                         Environment.requiredBySSL(),
                                         (short) orb.getBasicAdapter().getSSLPort());

            if( !org.jacorb.util.Environment.enforceSSL() ) 
            {
                // target supports unprotected messages
                ssl.target_supports |= 0x1;
            }

            CDROutputStream sslDataStream = 
                new org.jacorb.orb.CDROutputStream(orb);
  
            sslDataStream.beginEncapsulatedArray();

            org.omg.SSLIOP.SSLHelper.write( sslDataStream , ssl );


            TaggedComponent tc = 
                new TaggedComponent(org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value,
                                    sslDataStream.getBufferCopy());
            sslDataStream.close();
            
            info.add_ior_component_to_profile (tc, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, e);
        }
    }
} // SSLComponentInterceptor






