/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ContainerOperations extends org.omg.CORBA.IRObjectOperations {

    public org.omg.CORBA.Contained lookup(java.lang.String search_name);

    public org.omg.CORBA.Contained[] contents(
                org.omg.CORBA.DefinitionKind limit_type, 
                boolean exclude_inherited);

    public org.omg.CORBA.Contained[] lookup_name(java.lang.String search_name,
                int levels_to_search, 
                org.omg.CORBA.DefinitionKind limit_type, 
                boolean exclude_inherited);

    public org.omg.CORBA.ContainerPackage.Description[] describe_contents(
                org.omg.CORBA.DefinitionKind limit_type, 
                boolean exclude_inherited, 
                int max_returned_objs);

    public org.omg.CORBA.ModuleDef create_module(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version);

    public org.omg.CORBA.ConstantDef create_constant(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType type, 
                org.omg.CORBA.Any value);

    public org.omg.CORBA.StructDef create_struct(java.lang.String id,
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.StructMember[] members);

    public org.omg.CORBA.UnionDef create_union(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType discriminator_type, 
                org.omg.CORBA.UnionMember[] members);

    public org.omg.CORBA.EnumDef create_enum(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                java.lang.String[] members);

    public org.omg.CORBA.AliasDef create_alias(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType original_type);

    public org.omg.CORBA.InterfaceDef create_interface(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.InterfaceDef[] base_interfaces, 
                boolean is_abstract);

    public org.omg.CORBA.ExceptionDef create_exception(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.StructMember[] members);

    public org.omg.CORBA.ValueDef create_value(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                boolean is_custom, 
                boolean is_abstract, 
                org.omg.CORBA.ValueDef base_value, 
                boolean is_truncatable, 
                org.omg.CORBA.ValueDef[] abstract_base_values, 
                org.omg.CORBA.InterfaceDef[] supported_interfaces, 
                org.omg.CORBA.Initializer[] initializers);

    public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version, 
                org.omg.CORBA.IDLType original_type);

    public org.omg.CORBA.NativeDef create_native(java.lang.String id, 
                java.lang.String name, 
                java.lang.String version);
}
