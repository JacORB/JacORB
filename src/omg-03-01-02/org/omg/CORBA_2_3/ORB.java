/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

/***** This class is specifed by the mapping as abstract.
       A "dummy" implementation is provided so that the "official" org.omg.*
       packages may be compiled.

       ORB-vendors shall provide a complete implementation of the class
       by extending it with a vendor-specific class which
       provides "real" implementations for all the methods. E.g.

       package com.acme_orb_vendor.CORBA_2_3;
       public class ORB extends org.omg.CORBA_2_3 { ... }

       In order to be conformant the class shall support the signatures
       specified here, but will have an orb-specific implementation.

       The class may support additional vendor specific functionality.
*****/

package org.omg.CORBA_2_3;

public abstract class ORB extends org.omg.CORBA.ORB {

    // always return a ValueDef or throw BAD_PARAM if not repid of a value
    public org.omg.CORBA.Object get_value_def(String repid) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.ValueFactory register_value_factory(
                String id, org.omg.CORBA.portable.ValueFactory factory) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void unregister_value_factory(String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.ValueFactory lookup_value_factory(String id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_delegate(java.lang.Object wrapper) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
