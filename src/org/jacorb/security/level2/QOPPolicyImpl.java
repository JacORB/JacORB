package org.jacorb.security.level2;

import org.omg.CORBA.*;
import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
/**
 * QOPPolicyImpl.java
 *
 *
 * Created: Tue Jun 13 11:02:20 2000
 *
 * $Id$
 */

public class QOPPolicyImpl 
    extends org.jacorb.orb.LocalityConstrainedObject 
    implements QOPPolicy
{

    private QOP qop = null;
  
    public QOPPolicyImpl(QOP qop)
    {
        this.qop = qop;
    }

    /**
     *
     * @return <description>
     */
    public QOP qop() 
    {
        return qop;
    }


    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {        
        return new QOPPolicyImpl(qop);
    }

    /**
     *
     */
    public void destroy() 
    {
        qop = null;
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {        
        return SecQOPPolicy.value;
    }

} // QOPPolicyImpl






