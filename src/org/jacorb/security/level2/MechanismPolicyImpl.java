package org.jacorb.security.level2;

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;
import org.omg.CORBA.*;
/**
 * MechanismPolicyImpl.java
 *
 *
 * Created: Tue Jun 13 11:37:29 2000
 *
 * $Id$
 */

public class MechanismPolicyImpl 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements MechanismPolicy
{

    private String[] mechanisms = null;

    public MechanismPolicyImpl(String[] mechanisms) 
    {        
        this.mechanisms = mechanisms;
    }

    /**
     *
     * @return <description>
     */
    public String[] mechanisms()
    {
        return mechanisms;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        return new MechanismPolicyImpl((String[]) mechanisms.clone());
    }

    /**
     *
     */
    public void destroy() 
    {
        mechanisms = null;
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        return SecMechanismsPolicy.value;
    }    
} // MechanismPolicyImpl
