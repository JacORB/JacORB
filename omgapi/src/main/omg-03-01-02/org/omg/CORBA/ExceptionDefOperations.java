/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ExceptionDefOperations extends 
                org.omg.CORBA.ContainedOperations,
                org.omg.CORBA.ContainerOperations {

    public org.omg.CORBA.TypeCode type();

    public org.omg.CORBA.StructMember[] members();
    public void members(org.omg.CORBA.StructMember[] members);
}
