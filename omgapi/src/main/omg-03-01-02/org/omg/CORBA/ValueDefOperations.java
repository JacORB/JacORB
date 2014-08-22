/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ValueDefOperations extends
                org.omg.CORBA.ContainerOperations,
                org.omg.CORBA.ContainedOperations,
                org.omg.CORBA.IDLTypeOperations {

    public org.omg.CORBA.InterfaceDef[] supported_interfaces ();
    public void supported_interfaces(
            org.omg.CORBA.InterfaceDef[] supported_interfaces);

    public org.omg.CORBA.Initializer[] initializers ();
    public void initializers(org.omg.CORBA.Initializer[] initializers);

    public org.omg.CORBA.ValueDef base_value();
    public void base_value(org.omg.CORBA.ValueDef base_value);

    public org.omg.CORBA.ValueDef[] abstract_base_values ();
    public void abstract_base_values(
                org.omg.CORBA.ValueDef[] abstract_base_values);

    public boolean is_abstract();
    public void is_abstract(boolean is_abstract);

    public boolean is_custom();
    public void is_custom(boolean is_custom);

    public boolean is_truncatable();
    public void is_truncatable(boolean is_truncatable);

    public boolean is_a(java.lang.String value_id);

    public org.omg.CORBA.ValueDefPackage.FullValueDescription describe_value();

    public org.omg.CORBA.ValueMemberDef create_value_member(
                java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType type_def, 
                short access);

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
