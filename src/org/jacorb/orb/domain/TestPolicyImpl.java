package org.jacorb.orb.domain;

/**
 * This class implements a simple test policy
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */
public class TestPolicyImpl extends TestPolicyPOA {
  private String _desc;
  private int _fakeType;

  // overall Policy funtions from CORBA::Policy
	public  TestPolicyImpl(int i) { 
	  _desc= "test policy No. "+i; 
	  _fakeType= i;
	}

	public org.omg.CORBA.Policy copy() {
		return (org.omg.CORBA.Policy) new TestPolicyImpl(_fakeType);
	}
	public void destroy() {}

	public int policy_type() {
	  return _fakeType;
	  // return TEST_POLICY_ID.value; // fake
	}

  // specific to TestPolicy
  public String description() { return _desc; }
  public void description(String arg) { _desc= arg; }

  public String testFunction() { return _desc; }
  public String _toString() { return _desc; }

}
