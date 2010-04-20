/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
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

    public POA _default_POA() {
        return _get_delegate().default_POA(this);
    }

    public boolean _is_a(String repository_id) {
        return _get_delegate().is_a(this, repository_id);
    }

    public boolean _non_existent() {
        return _get_delegate().non_existent(this);
    }

    public org.omg.CORBA.Object _get_component() {
        return _get_delegate().get_component(this);
    }

    /** @deprecated Deprecated by CORBA 2.4
     */
    public org.omg.CORBA.InterfaceDef _get_interface() {
        return _get_delegate().get_interface(this);
    }

    public org.omg.CORBA.Object _get_interface_def()
    {
        // First try to call the delegate implementation class's
        // "Object get_interface_def(..)" method (will work for ORBs
        // whose delegates implement this method).
        // Else call the delegate implementation class's
        // "InterfaceDef get_interface(..)" method using reflection
        // (will work for ORBs that were built using an older version
        // of the Delegate interface with a get_interface method
        // but not a get_interface_def method).

        org.omg.PortableServer.portable.Delegate delegate = _get_delegate();
        try {
            // If the ORB's delegate class does not implement
            // "Object get_interface_def(..)", this will throw
            // an AbstractMethodError.
            return delegate.get_interface_def(this);
        } catch( AbstractMethodError aex ) {
            // Call "InterfaceDef get_interface(..)" method using reflection.
            try {
                Class[] argc = { org.omg.PortableServer.Servant.class };
                java.lang.reflect.Method meth =
                     delegate.getClass().getMethod("get_interface", argc);
                Object[] argx = { this };
                return (org.omg.CORBA.Object)meth.invoke(delegate, argx);
            } catch( java.lang.reflect.InvocationTargetException exs ) {
                Throwable t = exs.getTargetException();
                if (t instanceof Error) {
                    throw (Error) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
            } catch( RuntimeException rex ) {
                throw rex;
            } catch( Exception exr ) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        }
    }

    public String _repository_id() {
       return _get_delegate().repository_id( this ) ;
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
