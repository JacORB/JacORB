/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ContainedOperations extends org.omg.CORBA.IRObjectOperations {

    public java.lang.String id();
    public void id(java.lang.String id);

    public java.lang.String name();
    public void name(java.lang.String name);

    public java.lang.String version();
    public void version(java.lang.String version);

    public org.omg.CORBA.Container defined_in();
    public java.lang.String absolute_name();
    public org.omg.CORBA.Repository containing_repository();

    public org.omg.CORBA.ContainedPackage.Description describe();

    public void move(org.omg.CORBA.Container new_container, 
                    java.lang.String new_name, 
                    java.lang.String new_version);
}
