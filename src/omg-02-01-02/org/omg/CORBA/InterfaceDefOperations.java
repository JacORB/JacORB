/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface InterfaceDefOperations extends
            org.omg.CORBA.ContainerOperations, 
            org.omg.CORBA.ContainedOperations, 
            org.omg.CORBA.IDLTypeOperations {

    public org.omg.CORBA.InterfaceDef[] base_interfaces();
    public void base_interfaces(org.omg.CORBA.InterfaceDef[] base_interfaces);

    public boolean is_abstract();
    public void is_abstract(boolean is_abstract);

    public boolean is_a(java.lang.String interface_id);

    public org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription 
        describe_interface();

    public org.omg.CORBA.AttributeDef create_attribute(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType type, 
                org.omg.CORBA.AttributeMode mode);

    public org.omg.CORBA.OperationDef create_operation(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType result, 
                org.omg.CORBA.OperationMode mode, 
                org.omg.CORBA.ParameterDescription[] params, 
                org.omg.CORBA.ExceptionDef[] exceptions, 
                java.lang.String[] contexts);

}
