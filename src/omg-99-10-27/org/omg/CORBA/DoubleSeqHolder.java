/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class DoubleSeqHolder implements
                org.omg.CORBA.portable.Streamable {

    public double[] value;

    public DoubleSeqHolder() {
    }

    public DoubleSeqHolder(double[] initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = DoubleSeqHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        DoubleSeqHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return DoubleSeqHelper.type();
    }
}
