/***** Copyright (c) 2001 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface CustomOutputStream extends org.omg.CORBA.DataOutputStream {
    public void write_value(java.io.Serializable value);
    public void write_value(java.io.Serializable value, java.lang.String repId);
    public void write_value(java.io.Serializable value,
                            org.omg.CORBA.portable.BoxedValueHelper helper);
    public void write_abstract_interface(java.lang.Object obj);
}
