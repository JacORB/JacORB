package org.jacorb.orb.domain;

import java.util.Hashtable;

/**
 * Implementation of the strategy "PARENT_RULES" for conflict resoulution.
 * Created: Mon Apr 10 11:33:41 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class ParentRulesPolicy extends ManagementPolicyImpl
  implements ConflictResolutionPolicyOperations {
  
  public ParentRulesPolicy() {
    super("conflict resolve");

   String desc= "This object implements the domain conflict resolving policy. The "
     +"conflict resolving policy decides which domain/policy in the case of overlapping domains"
     + " to use. This policy gives precedence to the domain which is the highest domain "
     + "(nearest to the common root domain) in the list of conflicting domains. Simplified to the"
     +" case of two conflicting domains which are in a parent-child relationship this means the "
     +"parent domain takes precedence. If there is no such unique domain (which is possible, "
     +"but should not be the case, because then all the conflictiong domains do not have "
     +"anything in common and should therefore not conflict) then the first domain of the list "
     +"is taken.";
   long_description(desc);
  }

public org.omg.CORBA.Policy resolveConflict(Domain[] overlappingDMs, 
					    int overlapType)
  {
    org.jacorb.util.Debug.myAssert(1, overlappingDMs.length > 0,"SimpleConflictResolutionPolicy:"
			     +" list of overlapping domain managers is empty");
    if (overlappingDMs.length == 2)
      { // two conflicting domains may be a frequent case, fasten it
	if ( overlappingDMs[0].isReachable(overlappingDMs[1]) ) 
	  return overlappingDMs[0].get_domain_policy(overlapType);

	if ( overlappingDMs[1].isReachable(overlappingDMs[0]) ) 
	  return overlappingDMs[1].get_domain_policy(overlapType);

	// there is no relationship between the two conflicting domains, take first one
	return overlappingDMs[0].get_domain_policy(overlapType);
      }

    // do a BFS to obtain the distances from the root domain to all other domains
    Domain root= DomainImpl.getRootDomain(overlappingDMs[0], new Hashtable() );
    Hashtable distances= DomainImpl.calculateDistances(root);

    // look for the domain with the SMALLEST distance
    int min= Integer.MAX_VALUE;
    Domain result= null; int distance= 0;
    for (int i= 0; i < overlappingDMs.length ; i++)
      {
	distance = ((Integer) distances.get(overlappingDMs[i])).intValue();
	org.jacorb.util.Debug.output(4, "ParentRules.resolveConflict: distance of " 
				 + overlappingDMs[i].name() + " to root "
				 + root.name() + " is " + distance);
	if ( distance < min ) 
	  {
	    min= distance;
	    result= overlappingDMs[i];
	  }
      }

    org.jacorb.util.Debug.output(3, "ParentRules.resolveConflict: chosen " 
				 + result.name() + " with distance "
				 + min + " to domain " + root.name());
    return result.get_domain_policy(overlapType);

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
  { return ConflictResolutionPolicy.PARENT_RULES; } 

  public int policy_type()
    { return CONFLICT_RESOLUTION_POLICY_ID.value; }

  public org.omg.CORBA.Policy copy() 
  { // the _this() call may be dangerous if orb is not set
    return ( new ConflictResolutionPolicyPOATie (new ParentRulesPolicy()) )._this();
  }
  
} // SimpleConflictResolutionPolicy






