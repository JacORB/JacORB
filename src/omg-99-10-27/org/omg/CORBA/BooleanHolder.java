/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class BooleanHolder implements org.omg.CORBA.portable.Streamable {

    public boolean value;

    public BooleanHolder() {
    }

    public BooleanHolder(boolean initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = is.read_boolean();
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        os.write_boolean(value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_boolean);
    }

}
