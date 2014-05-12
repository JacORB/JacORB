/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public interface Streamable {

    void _read(org.omg.CORBA.portable.InputStream is);
    void _write(org.omg.CORBA.portable.OutputStream os);
    org.omg.CORBA.TypeCode _type();
}
