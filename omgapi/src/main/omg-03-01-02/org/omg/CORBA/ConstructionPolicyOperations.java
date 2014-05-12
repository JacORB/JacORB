/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ConstructionPolicyOperations
        extends org.omg.CORBA.PolicyOperations {

    void make_domain_manager(org.omg.CORBA.InterfaceDef object_type,
                             boolean constr_policy);
}
