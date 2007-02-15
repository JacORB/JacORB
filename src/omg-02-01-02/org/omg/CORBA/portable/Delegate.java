/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public abstract class Delegate {

    /**
    *@deprecated Deprecated by CORBA 2.3
    */
    public abstract org.omg.CORBA.InterfaceDef get_interface(
                org.omg.CORBA.Object self);

    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public abstract org.omg.CORBA.Object duplicate(org.omg.CORBA.Object self);

    public abstract void release(org.omg.CORBA.Object self);

    public abstract boolean is_a(org.omg.CORBA.Object self,
                String repository_id);

    public abstract boolean non_existent(org.omg.CORBA.Object self);
    
    public abstract org.omg.CORBA.Object get_component(org.omg.CORBA.Object self);

    public abstract boolean is_equivalent(org.omg.CORBA.Object self,
                org.omg.CORBA.Object rhs);

    public abstract int hash(org.omg.CORBA.Object self, int max);

    public abstract org.omg.CORBA.Request create_request(
                org.omg.CORBA.Object self,
                org.omg.CORBA.Context ctx,
                String operation,
                org.omg.CORBA.NVList arg_list,
                org.omg.CORBA.NamedValue result);

    public abstract org.omg.CORBA.Request create_request(
                org.omg.CORBA.Object self,
                org.omg.CORBA.Context ctx,
                String operation,
                org.omg.CORBA.NVList arg_list,
                org.omg.CORBA.NamedValue result,
                org.omg.CORBA.ExceptionList exclist,
                org.omg.CORBA.ContextList ctxlist);

    public abstract org.omg.CORBA.Request request(
                org.omg.CORBA.Object self,
                String operation);

    public org.omg.CORBA.portable.OutputStream request(
                org.omg.CORBA.Object self,
                String operation,
                boolean responseExpected) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.InputStream invoke(org.omg.CORBA.Object self,
                org.omg.CORBA.portable.OutputStream os)
                throws ApplicationException, RemarshalException {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void releaseReply(org.omg.CORBA.Object self,
                org.omg.CORBA.portable.InputStream is) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self,
                int policy_type) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.DomainManager[] get_domain_managers(
                org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public org.omg.CORBA.Object set_policy_override(org.omg.CORBA.Object self,
                org.omg.CORBA.Policy[] policies,
                org.omg.CORBA.SetOverrideType set_add) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public boolean is_local(org.omg.CORBA.Object self) {
        return false;
    }

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self,
                String operation, Class expectedType) {
        return null;
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
                ServantObject servant) {
    }

    public String toString(org.omg.CORBA.Object self) {
        return self.getClass().getName() + ":" + this.toString();
    }

    public int hashCode(org.omg.CORBA.Object self) {
        return System.identityHashCode(self);
    }

    public boolean equals(org.omg.CORBA.Object self, java.lang.Object obj) {
        return (self == obj);
    }
}
