/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class ShortSeqHolder implements
                org.omg.CORBA.portable.Streamable {

    public short[] value;

    public ShortSeqHolder() {
    }

    public ShortSeqHolder(short[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = ShortSeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        ShortSeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ShortSeqHelper.type();
    }
}
