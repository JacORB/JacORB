/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer;

abstract public class Servant {

    final public org.omg.CORBA.Object _this_object() {
        return _get_delegate().this_object(this);
    }

    final public org.omg.CORBA.Object _this_object(org.omg.CORBA.ORB orb) {
        try {
            ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);
        }
        catch (ClassCastException e) {
            throw new org.omg.CORBA.BAD_PARAM(
    "POA Servant requires an instanceof org.omg.CORBA_2_3.ORB");
        }
        return _this_object();
    }

    final public org.omg.CORBA.ORB _orb() {
        return _get_delegate().orb(this);
    }

    final public POA _poa() {
        return _get_delegate().poa(this);
    }

    final public byte[] _object_id() {
        return _get_delegate().object_id(this);
    }

    final public POA _default_POA() {
        return _get_delegate().default_POA(this);
    }

    final public boolean _is_a(String repository_id) {
        return _get_delegate().is_a(this, repository_id);
    }

    final public boolean _non_existent() {
        return _get_delegate().non_existent(this);
    }

    final public org.omg.CORBA.InterfaceDef _get_interface() {
        return _get_delegate().get_interface(this);
    }

    abstract public String[] _all_interfaces(POA poa, byte[] objectID);

    private transient org.omg.PortableServer.portable.Delegate _delegate =null;

    final public org.omg.PortableServer.portable.Delegate _get_delegate() {
        if (_delegate == null) {
            throw new org.omg.CORBA.BAD_INV_ORDER(
                "The Servant has not been associated with an ORBinstance");
        }
        return _delegate;
    }

    final public void _set_delegate(
            org.omg.PortableServer.portable.Delegate delegate) {
        _delegate = delegate;
    }
}
