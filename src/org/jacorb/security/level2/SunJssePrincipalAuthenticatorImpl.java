/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.security.level2;

import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import java.security.cert.*;

import org.omg.SecurityLevel2.*;
import org.omg.Security.*;

import org.jacorb.util.*;
//import org.jacorb.security.util.*;

/**
 * PrincipalAuthenticatorImpl
 *
 * This simple authenticator just retrieves X.509v3 certificates
 * from a Java key store
 *
 * @author Gerald Brose
 * $Id$
 */

public class SunJssePrincipalAuthenticatorImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.SecurityLevel2.PrincipalAuthenticator
{
    public SunJssePrincipalAuthenticatorImpl()
    {
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
	Debug.output( 3,"JPA: starting authentication" );
	try
	{
	    registerProvider();

            String keyStoreLocation =
                Environment.getProperty( "jacorb.security.keystore" );
            if ( keyStoreLocation == null )
            {
                System.out.print("Please enter key store file name: ");
                keyStoreLocation = (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String storePassphrase =
                Environment.getProperty("jacorb.security.keystore_password");
            if ( storePassphrase == null )
            {
                System.out.print("Please enter store pass phrase: ");
                storePassphrase = (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String alias = security_name;
            if ( alias == null )
            {
                System.out.print("Please enter alias  name: ");
                alias = (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String password = null;
            if ( auth_data == null )
            {
                System.out.print("Please enter password: ");
                password = (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }
            else
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

            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("JKS"/*, "SUN"*/);
            keyStore.load(new FileInputStream(keyStoreLocation), storePassphrase.toCharArray());
            //KeyStore keyStore =
            //    KeyStoreUtil.getKeyStore( keyStoreLocation,
            //                              storePassphrase.toCharArray() );

            java.security.cert.Certificate[] cert_chain = keyStore.getCertificateChain( alias );

            if( cert_chain == null )
            {
                Debug.output( 0, "No keys found in keystore for alias \""+
                              alias + "\"!" );

                if( Environment.getProperty( "jacorb.security.default_user" ) != null )
                {
                    Debug.output( 0, "Please check property \"jacorb.security.default_user\"" );
                }

                return org.omg.Security.AuthenticationStatus.SecAuthFailure;
            }

            PrivateKey priv_key = (PrivateKey)
                keyStore.getKey ( alias,
                                  password.toCharArray() );


            KeyAndCert k_a_c = new KeyAndCert( priv_key, cert_chain );

            AttributeType type = new AttributeType
                ( new ExtensibleFamily( (short) 0,
                                        (short) 1 ),
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

            Debug.output(3,"JPA: authentication succeeded");

            return AuthenticationStatus.SecAuthSuccess;
	}
	catch (Exception e)
	{
	    Debug.output(2,e);

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
        //iaik.security.provider.IAIK.addAsProvider();

        //Debug.output(3, "added Provider IAIK" );
    }
}










