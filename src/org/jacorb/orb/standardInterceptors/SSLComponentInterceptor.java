package org.jacorb.orb.standardInterceptors;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.SSLIOP.*;

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
    private TaggedComponent tc = null;

    public SSLComponentInterceptor( ORB orb ) 
    {
        this.orb = orb;
    }
  
    public String name()
    {
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
            if( tc == null )
            {
                short supported = (short)
                    Environment.getIntProperty( "jacorb.security.ssl.server.supported_options", 16 );

                short required = (short)
                    Environment.getIntProperty( "jacorb.security.ssl.server.required_options", 16 );

                SSL ssl = 
                    new SSL ( supported,
                              required,
                              (short) orb.getBasicAdapter().getSSLPort());

                //we don't support delegation 0x80 -> NoDelegation we don't
                //care if the other side delegates, so no required options are
                //set.
                ssl.target_supports |= 0x80;

                //this is SSLs default behaviour, included for completeness
                ssl.target_supports |= 0x20; //establish trust in target
       
                CDROutputStream sslDataStream = 
                    new CDROutputStream( orb );
  
                sslDataStream.beginEncapsulatedArray();

                SSLHelper.write( sslDataStream , ssl );

                tc = new TaggedComponent( TAG_SSL_SEC_TRANS.value,
                                          sslDataStream.getBufferCopy() );
            }

            info.add_ior_component_to_profile (tc, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, e);
        }
    }
} // SSLComponentInterceptor


