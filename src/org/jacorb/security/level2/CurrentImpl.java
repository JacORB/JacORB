package org.jacorb.security.level2;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-99  Gerald Brose.
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
 */

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.jacorb.util.*;

/**
 *
 * @author Nicolas Noffke, Gerald Brose, André Benvenuti
 * @version $Id$
 *
 */

public class CurrentImpl
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.omg.SecurityLevel2.Current
{
    private CredentialsImpl[] own_credentials;

    private PrincipalAuthenticator principalAuthenticator;
    private AccessDecision access_decision = null;
    private Hashtable policies = null;

    private SecAttributeManager attrib_mgr = null;
    
    //thread specific credentials
    private Hashtable ts_credentials = null;
    private Hashtable ts_received_credentials = null;
    
    private org.omg.CORBA.ORB orb = null;  
    
    public CurrentImpl(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
        
        attrib_mgr = SecAttributeManager.getInstance();

        ts_credentials = new Hashtable();
        ts_received_credentials = new Hashtable();
        policies = new Hashtable();

        //build access decision
        try
        {
            Class ad_class = 
                Class.forName(Environment.getProperty("jacorb.security.access_decision"));

            access_decision = (AccessDecision) ad_class.newInstance();
        }
        catch (Exception e)
        {
            Debug.output(Debug.SECURITY | Debug.IMPORTANT,
                         "Class " + Environment.getProperty("jacorb.security.access_decision") +
                         " not found!");
	    Debug.output( Debug.SECURITY | Debug.IMPORTANT,
			  "Please check property \"jacorb.security.access_decision\"" );

            Debug.output(Debug.SECURITY | Debug.DEBUG1, e);
            
            access_decision = new AccessDecisionImpl();
        }
    }

    public void init()
    {
	authenticate();
    }

    /**
     * Create a PrincipalAuthenticator for a given class name.
     * The class must have exactly one constructor ant that must
     * have either a constructor with no arg or a constructor with
     * exactly one arg of type org.omg.CORBA.ORB.
     *
     * @return The newly created PA or null, if anything failed.
     */
    private PrincipalAuthenticator createAuthenticator( String class_name )
    {
        try
        {
            Class pa_class = Class.forName( class_name );
            
            Constructor[] constructors = pa_class.getConstructors();
            
            if( constructors.length != 1 )
            {
                Debug.output( Debug.SECURITY | Debug.IMPORTANT,
                              "PA " + class_name + " must have exactly one constructor that takes either no arg or one arg of type org.omg.CORBA.ORB" );

                return null;
            }

            Class[] params = constructors[0].getParameterTypes();

            if( params.length == 0 )
            {
                return (PrincipalAuthenticator) pa_class.newInstance();
            }
            else if( params.length == 1 )
            {
                if( params[0].equals( org.omg.CORBA.ORB.class ))
                {
                    return (PrincipalAuthenticator) 
                        constructors[0].newInstance( new Object[]{ orb } );
                }
                else
                {
                  Debug.output( Debug.SECURITY | Debug.IMPORTANT,
                                "PA " + class_name + "s constructor has an arg of type " + params[0].getName() + " but it must have an arg of type  org.omg.CORBA.ORB" );
                }
            }  
            else
            {
                Debug.output( Debug.SECURITY | Debug.IMPORTANT,
                              "PA " + class_name + " must have exactly one constructor that takes either no arg or one arg of type org.omg.CORBA.ORB" );
            }
        }
        catch( Exception e )
        {
            Debug.output( Debug.SECURITY | Debug.INFORMATION, e );
        }

        return null;
    }

    /** 
     * This method does the following things:
     * 1.) take the property "jacorb.security.principal_authenticator", which
     * contains a comma separated list of PA class names.
     * 2.) create a PA Instance for each class name.
     * 3.) call authenticate() on each PA with the value of the property 
     * "jacorb.security.default_user" as the arg "security_name", the value
     * of the property "jacorb.security.default_password" as the arg 
     * "auth_data" and a CredentialsHolder. All other args are null.
     */
    private void authenticate()
    {
        String s = Environment.getProperty("jacorb.security.principal_authenticator");
        List authenticators = new Vector();

        if( s != null )
        {
            StringTokenizer st = new StringTokenizer( s, "," );
            
            while( st.hasMoreTokens() )
            {
                PrincipalAuthenticator pa = 
                    createAuthenticator( st.nextToken() );
                
                if( pa != null )
                {
                    authenticators.add( pa );
                }
            }
        }

        if( authenticators.size() == 0 )
        {
            Debug.output(Debug.SECURITY | Debug.IMPORTANT,
                         "WARNING: Unable to create custom PA. Will use default authenticator. Please check property \"jacorb.security.principal_authenticator\"" );
            authenticators.add( new PrincipalAuthenticatorImpl() );
        }

        principalAuthenticator = 
            (PrincipalAuthenticator) authenticators.get( 0 );

        String security_name = 
            Environment.getProperty( "jacorb.security.default_user" );
        String password = 
            Environment.getProperty( "jacorb.security.default_password" );
        byte[] pwd = (password == null)? null : password.getBytes();

        Vector own_creds = new Vector();

        for( int i = 0; i < authenticators.size(); i++ )
        {
            PrincipalAuthenticator pa = 
                (PrincipalAuthenticator) authenticators.get( i );

            CredentialsHolder coh = new CredentialsHolder();
        
            if( pa.authenticate( 0, 
                                 null, 
                                 security_name, 
                                 pwd,
                                 null,
                                 coh, 
                                 null, 
                                 null )
                == AuthenticationStatus.SecAuthSuccess) 
            {
                own_creds.add( (CredentialsImpl) coh.value );

                own_credentials = new CredentialsImpl[ own_creds.size() ];
                own_creds.copyInto( own_credentials );


                Debug.output( 2, "AuthenticationStatus.SecAuthSuccess");
            }
            else
            {
                Debug.output( 2, "AuthenticationStatus.SecAuthFailure");
            }
        }
    }

    /* thread specific, from SecurityLevel1*/
    public SecAttribute[] get_attributes(AttributeType[] types)
    {
        
        CredentialsImpl[] tsc = getTSCredentials();
        
        if( tsc != null && tsc.length > 0)
        {
            return tsc[0].get_attributes( types );
        }
        else if ( own_credentials != null &&
                  own_credentials.length > 0 )
        {
            return own_credentials[0].get_attributes( types );
        }
        else
        {
            return new SecAttribute[0];
        }
    }

    /* thread specific*/

    public ReceivedCredentials received_credentials()
    {
        return (ReceivedCredentials)ts_received_credentials.get( Thread.currentThread() );
    }
    
    /* thread specific*/
    public void set_credentials( CredentialType cred_type, 
                                 Credentials[] creds, 
                                 org.omg.SecurityLevel2.DelegationMode del )
    {
        //ignoring DelegationMode
        ts_credentials.put( Thread.currentThread(),
                            creds );
    }

    /* thread specific*/

    public void set_received_credentials( ReceivedCredentials creds ) 
    {
        //ignoring DelegationMode
        ts_received_credentials.put( Thread.currentThread(),
                            creds );
    }

    public void remove_received_credentials() 
    {
        //ignoring DelegationMode
        ts_received_credentials.remove( Thread.currentThread() );
    }

    

    /* thread specific*/
    public Credentials[] get_credentials(CredentialType cred_type)
    {
        CredentialsImpl[] tsc = getTSCredentials();

        if ( tsc == null )
        {
            tsc = own_credentials;
        }

        Vector found_creds = new Vector();

        for( int i = 0; i < tsc.length; i++ )
        {             
            if ( cred_type.value() == tsc[i].credentials_type().value() )
            {
                found_creds.addElement( tsc[i] );
            }
        }

        Credentials[] creds = new Credentials[found_creds.size()];

        for( int i = 0; i < creds.length; i++ )
        {             
            creds[i] = (Credentials) 
                found_creds.elementAt( i );
        }

        return creds;
    }
  
    /* application specific */
    public Credentials[] own_credentials()
    {
        return own_credentials;       
    }
  
    /* application specific */
    /**
     * This will remove the passed Credentials from the list 
     * of own_credentials.
     * The passed object has to be the same instance as the one 
     * to be removed.
     */
    public void remove_own_credentials(Credentials credentials)
    {
        boolean found_credentials = false;
        Vector kept_credentials = new Vector();
        
        for (int i = 0; i < own_credentials.length; i++)
        {
            if ( credentials == own_credentials[i] )
            {
                found_credentials = true;
            }
            else
            {
                kept_credentials.addElement( own_credentials[i] );
            }
        }
        
        if ( found_credentials )
        {
            own_credentials = new CredentialsImpl[kept_credentials.size()];

            for (int i = 0; i < kept_credentials.size(); i++)
            {
                own_credentials[i] = (CredentialsImpl) 
                    kept_credentials.elementAt( i );
            }
        }
        else
        {
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }

    /* application specific */
    public SecurityFeature[] received_security_features()
    {
        return null;
    }
  
    /* application specific */
    public org.omg.CORBA.Policy get_policy(int policy_type)
    {
        return (org.omg.CORBA.Policy) policies.get(new Integer(policy_type));
    }
  
    /* application specific */
    public org.omg.Security.MechandOptions[] supported_mechanisms()
    {
        return null;
    }

    /* application specific */
    public SecurityMechanismData[] get_security_mechanisms(org.omg.CORBA.Object obj_ref)
    {
        return null;
    }

    /* application specific */
    public RequiredRights required_rights_object()
    {
        return null;
    }
  
    /* application specific */
    public  PrincipalAuthenticator principal_authenticator()
    {
        return principalAuthenticator;
    }
  
    /* application specific */
    public AccessDecision access_decision()
    {
        return access_decision;
    }
  
    /* application specific */
    public AuditDecision audit_decision()
    {
        return null;
    }
  
    /* application specific */
    public QOPPolicy create_qop_policy(QOP qop)
    {
        return new QOPPolicyImpl(qop);
    }
  
    /* application specific */
    public MechanismPolicy create_mechanism_policy(String[] mechanisms)
    {
        return new MechanismPolicyImpl(mechanisms);
    }
  
    /* application specific */
    public InvocationCredentialsPolicy create_invoc_creds_policy(Credentials[] creds)
    {
        return new InvocationCredentialsPolicyImpl(creds);
    }

    private CredentialsImpl[] getTSCredentials()
    {
        return (CredentialsImpl[])
            ts_credentials.get( Thread.currentThread() );
    }

    public KeyAndCert[] getSSLCredentials()
    {
        if( own_credentials == null ||
            own_credentials.length == 0 )
        {   
            return new KeyAndCert[0];
        }

        AttributeType access_id = new AttributeType
            ( new ExtensibleFamily( (short) 0,
                                    (short) 1 ),
              AccessId.value );

        AttributeType role = new AttributeType
            ( new ExtensibleFamily( (short) 0,
                                    (short) 1 ),
              Role.value );

        SecAttribute[] attribs = 
            own_credentials[0].get_attributes( new AttributeType[]{ access_id,
                                                                    role } );
            
        KeyAndCert[] certs = new KeyAndCert[attribs.length];	

        for( int i = 0; i < certs.length; i++ )
        {
            certs[i] = attrib_mgr.getAttributeCertValue( attribs[i] );
        }

        return certs;
    }          


    public void close()
    {
        Debug.output( 3, "Closing Current");
        
	principalAuthenticator = null; // rt: use the gc for finalize
        policies.clear();
        ts_credentials.clear();
        ts_received_credentials.clear();
    }

    public void finalize()
    {
        close();
    }
}



