package org.jacorb.orb;

/**
 * JacORB - a free Java ORB
 *
 * @version $Id$
 */

public class LocalityConstrainedObject
    implements org.omg.CORBA.Object 
{

    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context ctx, java.lang.String operation, 
						 org.omg.CORBA.NVList arg_list, 
						 org.omg.CORBA.NamedValue result) 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context ctx,
						 java.lang.String operation, 
						 org.omg.CORBA.NVList arg_list, 
						 org.omg.CORBA.NamedValue result,
						 org.omg.CORBA.ExceptionList exc_list,
						 org.omg.CORBA.ContextList ctx_list)
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public org.omg.CORBA.Object _duplicate() 
    {
	  
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public org.omg.CORBA.InterfaceDef _get_interface() 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _get_interface_def() 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy _get_policy(int policy_type) 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public int _hash(int maximum) 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public boolean _is_a( String identifier ) 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public boolean _is_equivalent(org.omg.CORBA.Object other_object) 
    {	
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public boolean _non_existent() 
    {
		
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public void _release() 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();									
    }

    public org.omg.CORBA.Request _request( java.lang.String operation ) 
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.DomainManager[] _get_domain_managers()
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }
  
  
    public org.omg.CORBA.Object _set_policy_override(org.omg.CORBA.Policy[] policies,
                                            org.omg.CORBA.SetOverrideType set_add)
    {
	throw new org.omg.CORBA.NO_IMPLEMENT();
    }
  
    public String toString() 
    {
	throw new org.omg.CORBA.MARSHAL();
    }
}








