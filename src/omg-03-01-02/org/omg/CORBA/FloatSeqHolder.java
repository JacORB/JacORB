/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class FloatSeqHolder implements
                org.omg.CORBA.portable.Streamable {

    public float[] value;

    public FloatSeqHolder() {
    }

    public FloatSeqHolder(float[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = FloatSeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        FloatSeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FloatSeqHelper.type();
    }
}
