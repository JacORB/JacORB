package org.jacorb.orb.domain;
/**
 * Implemenation of the strategy "SIMPLE" for conflict resolution.
 * FirstResolveConflictPolicy.java
 *
 *
 * Created: Wed Mar  8 13:33:41 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class FirstConflictResolutionPolicy extends ManagementPolicyImpl
  implements ConflictResolutionPolicyOperations {
  
  public FirstConflictResolutionPolicy() {
    super("conflict resolve");

   String desc= "This object implements the domain conflict resolving policy. The "
     +"conflict resolving policy decides which domain/policy in the case of overlapping domains"
     + " to use. This policy simply uses the first domain in the list of overlapping"
     +" domains.";
   long_description(desc);
  }

public org.omg.CORBA.Policy resolveConflict(Domain[] overlappingDMs, 
						int overlapType)
  {
    org.jacorb.util.Debug.assert(1, overlappingDMs.length > 0,"FirstConflictResolutionPolicy:"
			     +" list of overlapping domain managers is empty");
    return overlappingDMs[0].get_domain_policy(overlapType);
  }

 // inherited member functions 

   /** returns a list of policy types. For all the policy types in the list this policy is 
   *  a meta policy. The type 0 means for all policy types. */
  public int[] managedTypes()
  {
    int result[]= {0};
    return result;
  }

  public void setPolicyType(int type){}


 public short strategy()
  { return ConflictResolutionPolicy.FIRST; } 

  public int policy_type()
    { return CONFLICT_RESOLUTION_POLICY_ID.value; }

  public org.omg.CORBA.Policy copy() 
  { // the _this() call may be dangerous if orb is not set
    return ( new ConflictResolutionPolicyPOATie (new FirstConflictResolutionPolicy()) )._this();
  }
  
} // FirstConflictResolutionPolicy






