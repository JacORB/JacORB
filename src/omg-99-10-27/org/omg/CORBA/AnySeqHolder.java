/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class AnySeqHolder implements org.omg.CORBA.portable.Streamable {

    public org.omg.CORBA.Any[] value;

    public AnySeqHolder() {
    }

    public AnySeqHolder(org.omg.CORBA.Any[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = AnySeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        AnySeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return AnySeqHelper.type();
    }
}
