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

    /*
        typedef unsigned short   AssociationOptions;

        const AssociationOptions NoProtection = 1; 0x001
        const AssociationOptions Integrity = 2; 0x002
        const AssociationOptions Confidentiality = 4; 0x004
        const AssociationOptions DetectReplay = 8; 0x008
        const AssociationOptions DetectMisordering = 16;0x010
        const AssociationOptions EstablishTrustInTarget = 32; 0x020
        const AssociationOptions EstablishTrustInClient = 64; 0x040
        const AssociationOptions NoDelegation = 128; 0x080
        const AssociationOptions SimpleDelegation = 256; 0x100
        const AssociationOptions CompositeDelegation = 512; 0x200
     */

    public void establish_components(IORInfo info) 
    {
        try
        {
            org.omg.SSLIOP.SSL ssl = 
                new org.omg.SSLIOP.SSL ( Environment.supportedBySSL(),
                                         Environment.requiredBySSL(),
                                         (short) orb.getBasicAdapter().getSSLPort());

            if( ! Environment.enforceSSL() ) 
            {
                // target (we) also supports unprotected messages
                // viz. on the other, non-SSL socket
                ssl.target_supports |= 0x001;
            }

            //we don't support delegation
            //0x80 -> NoDelegation
            //we don't care if the other side delegates,
            //so no required options are set.
            ssl.target_supports |= 0x080;

	    //this is SSLs default behaviour, included for
	    //completeness	    
	    ssl.target_supports |= 0x020; //establish trust in target
	    if( Environment.enforceSSL() ) 
	    {
		//tell the client right away that we only accept ssl
		//connections
		ssl.target_requires |= 0x020; //establish trust in target
	    }

            ssl.target_requires |= 0x002; //Integrity - SSL default
            ssl.target_requires |= 0x004; //Confidentiality - SSL default

            //for completeness
            ssl.target_supports |= 0x002; //Integrity - SSL default
            ssl.target_supports |= 0x004; //Confidentiality - SSL default
            
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


