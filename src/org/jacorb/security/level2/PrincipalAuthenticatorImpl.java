package org.jacorb.security.level2;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import java.security.cert.*;

import org.omg.SecurityLevel2.*;
import org.omg.Security.*;

import org.jacorb.util.*;
import org.jacorb.security.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;


/**
 * PrincipalAuthenticatorImpl
 * 
 * This simple authenticator just retrieves X.509v3 certificates
 * from a Java key store
 *
 * @author Gerald Brose
 * $Id$
 */

public class PrincipalAuthenticatorImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.SecurityLevel2.PrincipalAuthenticator, Configurable
{  
    private Logger logger;

    private String keyStoreLocation;
    private String storePassphrase;

    public void configure(Configuration config)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)config).getNamedLogger("jacorb.security");
        keyStoreLocation = 
            config.getAttribute("jacorb.security.keystore", null );
        
        storePassphrase = 
            config.getAttribute("jacorb.security.keystore_password", null);

    }  

    public int[] get_supported_authen_methods(java.lang.String mechanism)
    {
	return new int[]{0};
    }

    public AuthenticationStatus authenticate(int method, 
                                             String mechanism, 
                                             String security_name, //user name
                                             byte[] auth_data, //  passwd
                                             SecAttribute[] privileges, 
                                             CredentialsHolder creds, 
                                             OpaqueHolder continuation_data, 
                                             OpaqueHolder auth_specific_data
                                             )
    {
        if (logger.isInfoEnabled())
            logger.info( "starting authentication" );

	try 
	{	
	    registerProvider();

            String alias = security_name;
            String password = null;
            if ( auth_data != null )
            {
                password = new String( auth_data );
            }

            if (( keyStoreLocation == null ) || 
                ( storePassphrase == null ) ||
                ( alias == null ) || 
                ( password == null ))
            {
                return AuthenticationStatus.SecAuthFailure;
            }

            KeyStore keyStore = 
                KeyStoreUtil.getKeyStore( keyStoreLocation, 
                                          storePassphrase.toCharArray() );

            X509Certificate[] cert_chain = 
                (X509Certificate[])keyStore.getCertificateChain( alias );

            if( cert_chain == null )
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "No keys found in keystore for alias \""+
                              alias + "\"!" );
                }            
                return org.omg.Security.AuthenticationStatus.SecAuthFailure;
            }
            
            PrivateKey priv_key = 
                (PrivateKey)keyStore.getKey(alias, password.toCharArray() );


            KeyAndCert k_a_c = new KeyAndCert( priv_key, cert_chain );

            AttributeType type = 
                new AttributeType( new ExtensibleFamily((short)0,(short)1 ),
                                   AccessId.value );



            SecAttributeManager attrib_mgr = SecAttributeManager.getInstance();
            SecAttribute attrib = attrib_mgr.createAttribute( k_a_c,
                                                              type );
                
            CredentialsImpl credsImpl = 
                new CredentialsImpl( new SecAttribute[]{ attrib },
                AuthenticationStatus.SecAuthSuccess,
                InvocationCredentialsType.SecOwnCredentials);

            /*
            credsImpl.accepting_options_supported( (short) Environment.getIntProperty( "jacorb.security.ssl.client.supported_options", 16 ));

            credsImpl.accepting_options_required( (short) Environment.getIntProperty( "jacorb.security.ssl.client.required_options", 16 ));

            credsImpl.invocation_options_supported( (short) Environment.getIntProperty( "jacorb.security.ssl.client.supported_options", 16 ));

            credsImpl.invocation_options_required( (short) Environment.getIntProperty( "jacorb.security.ssl.client.required_options", 16 ));
            */
            
            creds.value = credsImpl;

            if (logger.isInfoEnabled())
                logger.info( "authentication succesfull" );

            return AuthenticationStatus.SecAuthSuccess;
	}
	catch (Exception e) 
	{
            if (logger.isDebugEnabled())
                logger.debug( "Exception: " + e.getMessage());
            
            if (logger.isInfoEnabled())
                logger.info( "authentication failed" );

	    return org.omg.Security.AuthenticationStatus.SecAuthFailure;
	}
    }

    /** 
     * not implemented
     */
  
    public AuthenticationStatus continue_authentication(byte[] response_data, 
							Credentials creds, 
							OpaqueHolder continuation_data, 
							OpaqueHolder auth_specific_data)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    private void registerProvider()
    {
        iaik.security.provider.IAIK.addAsProvider();
        if (logger.isDebugEnabled())
            logger.debug( "Provider IAIK added" );
    }
}










