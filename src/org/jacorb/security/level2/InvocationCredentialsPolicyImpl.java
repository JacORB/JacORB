package org.jacorb.security.level2;

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;
import org.omg.CORBA.*;
/**
 * InvocationCredentialsPolicyImpl
 *
 *
 * Created: Tue Jun 13 16:55:36 2000
 *
 * $Id$
 */

public class InvocationCredentialsPolicyImpl
    extends org.jacorb.orb.LocalityConstrainedObject  
    implements InvocationCredentialsPolicy
{
    private Credentials[] creds = null;

    public InvocationCredentialsPolicyImpl(Credentials[] creds) 
    {
        this.creds = creds;
    }

    public Credentials[] creds()
    {
        return creds;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        Credentials[] new_creds = new Credentials[creds.length];
        for(int i = 0; i < new_creds.length; i++)
            new_creds[i] = creds[i].copy();
        
        return new InvocationCredentialsPolicyImpl(new_creds);
    }

    /**
     *
     */
    public void destroy() 
    {
        creds = null;
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        return SecInvocationCredentialsPolicy.value;
    }
} // InvocationCredentialsPolicyImpl
