/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class WCharSeqHolder implements org.omg.CORBA.portable.Streamable {

    public char[] value;

    public WCharSeqHolder() {
    }

    public WCharSeqHolder(char[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = WCharSeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        WCharSeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return WCharSeqHelper.type();
    }
}
