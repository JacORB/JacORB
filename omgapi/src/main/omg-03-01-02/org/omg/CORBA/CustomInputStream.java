/***** Copyright (c) 2001 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface CustomInputStream extends org.omg.CORBA.DataInputStream {
    public java.io.Serializable read_value();
    public java.io.Serializable read_value(java.lang.String repId);
    public java.io.Serializable read_value(java.lang.Class expected);
    public java.lang.Object read_abstract_interface();
    public java.lang.Object read_abstract_interface(java.lang.Class expected);
    public java.io.Serializable read_value(
       org.omg.CORBA.portable.BoxedValueHelper helper); // boxed valuetypes

}
