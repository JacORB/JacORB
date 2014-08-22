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

package org.omg.CORBA_2_5;

public abstract class ORB extends org.omg.CORBA_2_3.ORB {
    public String id()
    {
       throw new org.omg.CORBA.NO_IMPLEMENT() ;
    }

    public void register_initial_reference(
       String object_name,
       org.omg.CORBA.Object object
    ) throws org.omg.CORBA.ORBPackage.InvalidName
    {
       throw new org.omg.CORBA.NO_IMPLEMENT() ;
    }

    public org.omg.CORBA.TypeCode create_local_interface_tc(
                                    String id,
                                    String name)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
