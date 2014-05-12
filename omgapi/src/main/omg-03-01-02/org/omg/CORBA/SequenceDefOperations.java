/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface SequenceDefOperations extends
                org.omg.CORBA.IDLTypeOperations {

    public int bound();
    public void bound(int bound);

    public org.omg.CORBA.TypeCode element_type();
    public org.omg.CORBA.IDLType element_type_def();

    public void element_type_def(org.omg.CORBA.IDLType element_type_def);

}
