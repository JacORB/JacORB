package org.jacorb.orb.domain;
/**
 * MapToDefaultDomainPolicy.java
 * implements the IDL-interface InitialMapPolicy. An instance of this class maps a newly
 * created object reference to one default domain.
 * This default domain is set and read via the operations "setDefaultDomain" and 
 * "getDefaultDomain".
 *
 * Created: Thu Apr 20 12:06:43 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class MapToDefaultDomainPolicy extends ManagementPolicyImpl
  implements InitialMapToDefaultDomainPolicyOperations {
  
  /** the default domain which is always returned from the function "OnReferenceCreation" */
  private Domain _default;
  
  public MapToDefaultDomainPolicy() { this(null); }
  public MapToDefaultDomainPolicy(Domain defaultDomain)
  { 
    super("initial map");

    _default= defaultDomain; 
    
    String desc= "This object implements the domain initial map policy. The initial map policy"
      +" is used to map a newly created object reference to one or more domains. The strategy "
      +"of this object is to map a new object reference to a single default domain";
   long_description(desc);
  } // MapToDefaultDomainPolicy

  public Domain getDefaultDomain()                        { return   _default; }
  public void   setDefaultDomain(Domain newDefaultDomain) { _default= newDefaultDomain; }

  /** maps a newly created object reference to the default domain. */
  public Domain[] OnReferenceCreation(org.omg.CORBA.Object newReference, Domain rootDomain)
  {
    Domain result[]= new Domain[1];
    if (_default != null) result[0]= _default;
    else result[0]= rootDomain;
    return result;
  }


 // inherited member functions

 public short strategy()
  { return InitialMapPolicy.DEFAULT_DOMAIN; } 

  public int policy_type()
    { return INITIAL_MAP_POLICY_ID.value; }

  public org.omg.CORBA.Policy copy() 
  { 
    return ( new InitialMapPolicyPOATie (new MapToDefaultDomainPolicy()) )._this();
  }
} // MapToDefaultDomainPolicy
