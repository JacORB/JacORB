package org.jacorb.orb.domain;
/**
 * Implementation of the IDL interface ManagementPolicy.
 * Created: Tue Feb 29 14:27:21 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class ManagementPolicyImpl 
  extends org.jacorb.orb.domain.ManagementPolicyPOA 
{
  
  /** a brief description ot this policy */
  protected String _short_description;
  /** a long description of this polic   */
  protected String  _long_description;
  
  // constructors 
  public ManagementPolicyImpl() { this("", ""); }
  public ManagementPolicyImpl(String short_desc) { this(short_desc, ""); }
  public ManagementPolicyImpl(String short_desc, String long_desc) 
  {
    _short_description= short_desc;
    _long_description=  long_desc;
  }

  // members
  public java.lang.String short_description() 
  { return _short_description; }

  public void short_description(java.lang.String arg)
  { _short_description= arg; }

  
  public java.lang.String long_description()  
  { return _long_description; }

  public void long_description(java.lang.String arg)
  { _long_description= arg; }

  // inherited members from org.omg.CORBA.Policy

  public int policy_type()
    { return MANAGEMENT_POLICY_ID.value; }

  public org.omg.CORBA.Policy copy() 
  { // the _this() may be dangerous if the orb is not set
    return (new ManagementPolicyImpl(_short_description,_long_description))._this();
  }
  public void destroy() {}
  
} // ManagementPolicyImpl
