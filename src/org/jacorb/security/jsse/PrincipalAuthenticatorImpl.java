package org.jacorb.security.jsse;

import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import java.security.cert.*;

import org.omg.SecurityLevel2.*;
import org.omg.Security.*;

import org.jacorb.util.Environment;
import org.jacorb.security.level2.*;

/**
 * PrincipalAuthenticatorImpl
 * 
 * This simple authenticator just retrieves X.509v3 certificates
 * from a Java key store
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class PrincipalAuthenticatorImpl
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.omg.SecurityLevel2.PrincipalAuthenticator
{  
    public PrincipalAuthenticatorImpl()
    {

    }  

    public int[] get_supported_authen_methods(java.lang.String mechanism)
    {
	return new int[]{0};
    }

    public AuthenticationStatus authenticate(int method, 
                                             String mechanism, 
                                             String security_name, //alias
                                             byte[] auth_data, //  passwd
                                             SecAttribute[] privileges, 
                                             CredentialsHolder creds, 
                                             OpaqueHolder continuation_data, 
                                             OpaqueHolder auth_specific_data
                                             )
    {
        java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );

	org.jacorb.util.Debug.output(3,"starting authentication");
	try 
	{	
            String keystore_location = Environment.keyStore();
            if( keystore_location == null ) 
            {
                System.out.print( "Please enter key store file name: " );
                keystore_location = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String keystore_passphrase = 
                Environment.getProperty( "jacorb.security.keystore_password" );
            if( keystore_passphrase == null ) 
            {
                System.out.print( "Please enter store pass phrase: " );
                keystore_passphrase= 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String alias = security_name;
            if( alias == null ) 
            {
                System.out.print( "Please enter alias  name: " );
                alias = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }

            String password = null;
            if ( auth_data == null ) 
            {
                System.out.print( "Please enter password: " );
                password = 
                    (new BufferedReader(new InputStreamReader(System.in))).readLine();
            }
            else
            {
                password = new String( auth_data );
            }
              

            if(( keystore_location == null ) || 
               ( keystore_passphrase == null ) ||
               ( alias == null ) || 
               ( password == null ))
            {
                return AuthenticationStatus.SecAuthFailure;
            }

            KeyStore key_store = KeyStore.getInstance( "JKS" );
            key_store.load( new FileInputStream( keystore_location ),
                            keystore_passphrase.toCharArray() );

            X509Certificate[] cert_chain = (X509Certificate[]) 
                key_store.getCertificateChain( alias );

            PrivateKey priv_key = (PrivateKey) 
                key_store.getKey ( alias, password.toCharArray() );

            KeyAndCert k_a_c = new KeyAndCert( priv_key, cert_chain );

            AttributeType type = new AttributeType
                ( new ExtensibleFamily( (short) 0,
                                        (short) 1 ),
                  AccessId.value );

            SecAttributeManager attrib_mgr = SecAttributeManager.getInstance();

            //only considering AccessId attributes
            for( int i = 0; i < privileges.length; i++ )
            {
                if( privileges[i].attribute_type.attribute_family.family_definer != 0 ||
                    privileges[i].attribute_type.attribute_family.family != 1 ||
                    privileges[i].attribute_type.attribute_type != AccessId.value)
                {
                    throw new RuntimeException("Cannot handle security attribute.");
                }
                privileges[i]  = attrib_mgr.createAttribute( k_a_c,
                                                             type );
            }
        
            CredentialsImpl credsImpl = 
                new CredentialsImpl( privileges,
                                     AuthenticationStatus.SecAuthSuccess,
                                     InvocationCredentialsType.SecOwnCredentials);

            credsImpl.accepting_options_supported( Environment.supportedBySSL() );
            credsImpl.accepting_options_required( Environment.requiredBySSL() );
            credsImpl.invocation_options_supported( Environment.supportedBySSL() );
            credsImpl.invocation_options_required( Environment.requiredBySSL() );

            creds.value = credsImpl;

            org.jacorb.util.Debug.output(3,"authentication succeeded");

            return AuthenticationStatus.SecAuthSuccess;
	}
	catch (Exception e) 
	{
	    org.jacorb.util.Debug.output(2,e);

	    return org.omg.Security.AuthenticationStatus.SecAuthFailure;
	}
    }

    /** 
     * not implemented
     */
  
    public AuthenticationStatus continue_authentication(
							byte[] response_data, 
							Credentials creds, 
							OpaqueHolder continuation_data, 
							OpaqueHolder auth_specific_data)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}










