/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class CharHolder implements org.omg.CORBA.portable.Streamable {

    public char value;

    public CharHolder() {
    }

    public CharHolder(char initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = is.read_char();
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        os.write_char(value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_char);
    }

}
