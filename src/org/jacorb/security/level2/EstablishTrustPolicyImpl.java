package org.jacorb.security.level2;

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;
import org.omg.CORBA.*;
/**
 * EstablishTrustPolicyImpl.java
 *
 *
 * Created: Tue Jun 13 16:59:40 2000
 *
 * $Id$
 */

public class EstablishTrustPolicyImpl 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements EstablishTrustPolicy
{
  
    public EstablishTrustPolicyImpl() {
    
    }

    public EstablishTrust trust()
    {
        return null;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return null;
    }

    /**
     *
     */
    public void destroy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return -1;
    }
} // EstablishTrustPolicyImpl
