/***** Copyright (c) 2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.

       Change history: modified toString, hashCode, equals per Java 2k RTF 
           - 15 Jan 2000 
           - Jeff Mischkinsky (jeff@persistence.com, jeff_mischkinsky@omg.org)
*****/

package org.omg.CORBA.portable;

abstract public class ObjectImpl implements org.omg.CORBA.Object {

    private transient Delegate __delegate;

    public Delegate _get_delegate() {
        if (__delegate == null)
          throw new org.omg.CORBA.BAD_OPERATION();
        return __delegate;
    }

  
    public void _set_delegate(Delegate delegate) {
        __delegate = delegate;
    }
  
    public abstract String[] _ids();

    /**
    *@deprecated Deprecated by CORBA 2.3
    */
    public org.omg.CORBA.InterfaceDef _get_interface() {
        return _get_delegate().get_interface(this);
    }

    public org.omg.CORBA.Object _get_interface_def() {
        return _get_delegate().get_interface_def(this);
    }

    public org.omg.CORBA.Object _duplicate() {
        return _get_delegate().duplicate(this);
    }

    public void _release() {
        _get_delegate().release(this);
    }

    public boolean _is_a(String repository_id) {
        return _get_delegate().is_a(this, repository_id);
    }
  
    public boolean _is_equivalent(org.omg.CORBA.Object that) {
        return _get_delegate().is_equivalent(this, that);
    }

    public boolean _non_existent() {
        return _get_delegate().non_existent(this);
    }
    
    public org.omg.CORBA.Object _get_component() {
        return _get_delegate().get_component(this);
    }

    public int _hash(int maximum) {
        return _get_delegate().hash(this, maximum);
    }

    public org.omg.CORBA.Request _request(String operation) {
        return _get_delegate().request(this, operation);
    }
  
    public org.omg.CORBA.portable.OutputStream _request(String operation,
            boolean responseExpected) {
        return _get_delegate().request(this, operation, responseExpected);
    }

    public org.omg.CORBA.portable.InputStream _invoke(
                org.omg.CORBA.portable.OutputStream output)
            throws ApplicationException, RemarshalException {
        return _get_delegate().invoke(this, output);
    }
  
    public void _releaseReply(org.omg.CORBA.portable.InputStream input) {
        _get_delegate().releaseReply(this, input);
    }

    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context ctx,
                    String operation,
                    org.omg.CORBA.NVList arg_list,
                    org.omg.CORBA.NamedValue result) {
        return _get_delegate().create_request(this, ctx,operation, 
                    arg_list,result);
    }
  
    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context ctx,
                    String operation,
                    org.omg.CORBA.NVList arg_list,
                    org.omg.CORBA.NamedValue result,
                    org.omg.CORBA.ExceptionList exceptions,
                    org.omg.CORBA.ContextList contexts) {
        return _get_delegate().create_request(this, ctx, operation, arg_list,
                    result, exceptions, contexts);
    }

    public org.omg.CORBA.Policy _get_policy(int policy_type) {
        return _get_delegate().get_policy(this, policy_type);
    }
  
    public org.omg.CORBA.DomainManager[] _get_domain_managers() {
        return _get_delegate().get_domain_managers(this);
    }

    public org.omg.CORBA.Object _set_policy_override(
                    org.omg.CORBA.Policy[] policies,
                    org.omg.CORBA.SetOverrideType set_add) {
        return _get_delegate().set_policy_override(this, policies, set_add);
    }

    public org.omg.CORBA.ORB _orb() {
        return _get_delegate().orb(this);
    }
  
    public boolean _is_local() {
        return _get_delegate().is_local(this);
    }

    public ServantObject _servant_preinvoke(String operation,
                    Class expectedType) {
        return _get_delegate().servant_preinvoke(this, operation,expectedType);
    }

    public void _servant_postinvoke(ServantObject servant) {
        _get_delegate().servant_postinvoke(this, servant);
    }

    public String toString() {
        if ( __delegate != null )
            return __delegate.toString(this);
        else
            return getClass().getName()+":no delegate set";
    }

    public int hashCode() {
        if ( __delegate != null )
            return __delegate.hashCode(this);
        else
            return System.identityHashCode(this);
    }

    public boolean equals(java.lang.Object obj) {
        if ( __delegate != null )
            return __delegate.equals(this, obj);
        else
            return (this==obj);
    }
}
