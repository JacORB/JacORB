/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class ShortHolder implements org.omg.CORBA.portable.Streamable {

    public short value;

    public ShortHolder() {
    }

    public ShortHolder(short initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = is.read_short();
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        os.write_short(value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_short);
    }

}
