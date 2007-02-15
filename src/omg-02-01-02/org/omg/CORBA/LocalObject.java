/***** Copyright (c) 2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;
import org.omg.CORBA.portable.*;

public abstract class LocalObject implements org.omg.CORBA.Object {
	public LocalObject()
		{}
	public boolean _is_equivalent(org.omg.CORBA.Object that){
		return equals(that);
	}
	public boolean _non_existent(){
		return false;
	}
	public int _hash(int maximum){
		return hashCode();
	}
	public String[] _ids() {
	    throw new NO_IMPLEMENT() ;
	}
	public boolean _is_a(String repositoryId){
	    String ids[] = _ids() ;
	    for (int i = 0; i<ids.length; i++ ) {
		if (repositoryId.equals( ids[i] ))
		    return true ;
	    }

	    return false ;
	}
	public org.omg.CORBA.Object _duplicate(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public void _release(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public Request _request(String operation){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.Object _get_component() {
    // There is an inconsistency between CORBA 3.0 spec. and 'IDL to Java 
		// mapping' spec. The former says that the operation should return 
		// the exception with minor code 8. But the latter suggests to return 
		// a default instance of NO_IMPLEMENT exception which conveys minor code 0.
		// Which on to follow?
    //throw new org.omg.CORBA.NO_IMPLEMENT(8, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
		throw new org.omg.CORBA.NO_IMPLEMENT();
  }
	public Request _create_request(
		Context ctx,
		String operation,
		NVList arg_list,
		NamedValue result){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public Request _create_request(
		Context ctx,
		String operation,
		NVList arg_list,
		NamedValue result,
		ExceptionList exceptions,
		ContextList contexts) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	/**
	*@deprecated Deprecated by CORBA 2.3.
	*/
	public org.omg.CORBA.InterfaceDef _get_interface(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.Object _get_interface_def(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.ORB _orb(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.Policy _get_policy(int policy_type){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.DomainManager[]
	_get_domain_managers(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.Object _set_policy_override(
		org.omg.CORBA.Policy[] policies,
		org.omg.CORBA.SetOverrideType set_add){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public boolean _is_local(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.portable.ServantObject _servant_preinvoke(
		String operation, Class expectedType){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public void _servant_postinvoke(
		org.omg.CORBA.portable.ServantObject servant){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.portable.OutputStream _request(
		String operation, boolean responseExpected){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.portable.InputStream _invoke(
		org.omg.CORBA.portable.OutputStream output)
		throws org.omg.CORBA.portable.ApplicationException,
			org.omg.CORBA.portable.RemarshalException {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public void _releaseReply(
		org.omg.CORBA.portable.InputStream input){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public boolean validate_connection(){
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
}
