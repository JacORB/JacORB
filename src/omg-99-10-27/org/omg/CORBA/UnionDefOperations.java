/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface UnionDefOperations extends org.omg.CORBA.TypedefDefOperations,
                org.omg.CORBA.ContainerOperations {

    public org.omg.CORBA.TypeCode discriminator_type();

    public org.omg.CORBA.IDLType discriminator_type_def();
    public void discriminator_type_def(
                org.omg.CORBA.IDLType discriminator_type_def);

    public org.omg.CORBA.UnionMember[] members();
    public void members(org.omg.CORBA.UnionMember[] members);
}
