/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class OctetSeqHolder implements
                org.omg.CORBA.portable.Streamable {

    public byte[] value;

    public OctetSeqHolder() {
    }

    public OctetSeqHolder(byte[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = OctetSeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        OctetSeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return OctetSeqHelper.type();
    }
}
