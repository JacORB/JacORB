/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.ServantLocatorPackage;

final public class CookieHolder implements org.omg.CORBA.portable.Streamable {

    public java.lang.Object value;

    public CookieHolder() {
    }

    public CookieHolder(java.lang.Object intial) {
        this.value = intial;
    }

    public void _read(org.omg.CORBA.portable.InputStream input) {
        value = CookieHelper.read(input);
    }

    public void _write(org.omg.CORBA.portable.OutputStream output) {
        CookieHelper.write(output, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return CookieHelper.type();
    }

}
