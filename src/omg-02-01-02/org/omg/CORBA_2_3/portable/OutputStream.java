/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA_2_3.portable;

abstract public class OutputStream extends org.omg.CORBA.portable.OutputStream{

    public void write_value(java.io.Serializable value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value,
                    java.lang.String rep_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value, Class clz) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value(java.io.Serializable value, 
                            org.omg.CORBA.portable.BoxedValueHelper factory) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_abstract_interface(java.lang.Object object) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
