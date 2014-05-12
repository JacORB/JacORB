/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

/***** This class is ORB-vendor specific. 
       A "dummy" implementation is provided so that the "official" org.omg.*
       packages may be compiled.  In order to actually use a Java ORB,
       the ORB vendor's implementation will provide a "real"
       implementation of the class.

       In order to be conformant the class shall support the signatures
       specified here, but will have an orb-specific implementation.

       The class may support additional vendor specific functionality.
       It shall have at least the inheritance relationships specified
       here. Any additional (vendor specific) inheritance relationships may 
       only be with other classes and interfaces that are guaranteed to be 
       present in the JDK core.
*****/

package org.omg.PortableServer.ServantLocatorPackage;

public class CookieHolder implements org.omg.CORBA.portable.Streamable {

    public java.lang.Object value;

    public CookieHolder() {
    }

    public CookieHolder(java.lang.Object intial) {
        this.value = intial;
    }

    public void _read(org.omg.CORBA.portable.InputStream input) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void _write(org.omg.CORBA.portable.OutputStream output) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode _type() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
