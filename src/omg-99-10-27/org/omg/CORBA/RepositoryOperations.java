/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface RepositoryOperations extends
                    org.omg.CORBA.ContainerOperations {

    public org.omg.CORBA.Contained lookup_id(java.lang.String search_id);

    public org.omg.CORBA.TypeCode get_canonical_typecode(
                    org.omg.CORBA.TypeCode tc);

    public org.omg.CORBA.PrimitiveDef get_primitive(
                    org.omg.CORBA.PrimitiveKind kind);

    public org.omg.CORBA.StringDef create_string(int bound);

    public org.omg.CORBA.WstringDef create_wstring(int bound);

    public org.omg.CORBA.SequenceDef create_sequence(int bound, 
                    org.omg.CORBA.IDLType element_type);

    public org.omg.CORBA.ArrayDef create_array(int length, 
                    org.omg.CORBA.IDLType element_type);

    public org.omg.CORBA.FixedDef create_fixed(short digits, short scale);
}
