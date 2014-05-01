/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA;

public abstract class Any implements org.omg.CORBA.portable.IDLEntity {

    abstract public boolean equal(org.omg.CORBA.Any a);

    abstract public org.omg.CORBA.TypeCode type();
    abstract public void type(org.omg.CORBA.TypeCode type);

    abstract public void read_value(org.omg.CORBA.portable.InputStream is, 
                    org.omg.CORBA.TypeCode type);
    abstract public void write_value(org.omg.CORBA.portable.OutputStream os);

    abstract public org.omg.CORBA.portable.OutputStream create_output_stream();
    abstract public org.omg.CORBA.portable.InputStream create_input_stream();

    abstract public short extract_short();
    abstract public void insert_short(short s);

    abstract public int extract_long();
    abstract public void insert_long(int i);

    abstract public long extract_longlong();
    abstract public void insert_longlong(long l);

    abstract public short extract_ushort();
    abstract public void insert_ushort(short s);

    abstract public int extract_ulong();
    abstract public void insert_ulong(int i);

    abstract public long extract_ulonglong();
    abstract public void insert_ulonglong(long l);

    abstract public float extract_float();
    abstract public void insert_float(float f);

    abstract public double extract_double();
    abstract public void insert_double(double d);

    abstract public boolean extract_boolean();
    abstract public void insert_boolean(boolean b);

    abstract public char extract_char();
    abstract public void insert_char(char c);

    abstract public char extract_wchar();
    abstract public void insert_wchar(char c);

    abstract public byte extract_octet();
    abstract public void insert_octet(byte b);

    abstract public org.omg.CORBA.Any extract_any();
    abstract public void insert_any(org.omg.CORBA.Any a);

    abstract public org.omg.CORBA.Object extract_Object();
    abstract public void insert_Object(org.omg.CORBA.Object obj);

    abstract public java.io.Serializable extract_Value();
    abstract public void insert_Value(java.io.Serializable v);
    abstract public void insert_Value(java.io.Serializable v, 
                        org.omg.CORBA.TypeCode t);

    abstract public void insert_Object(org.omg.CORBA.Object obj,
                        org.omg.CORBA.TypeCode type);

    abstract public String extract_string();
    abstract public void insert_string(String s);

    abstract public String extract_wstring();
    abstract public void insert_wstring(String value);

    abstract public TypeCode extract_TypeCode();
    abstract public void insert_TypeCode(TypeCode value);

    /**
    *@ deprecated
    */
    public Principal extract_Principal() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    /**
    *@ deprecated
    */
    public void insert_Principal(Principal p) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.Streamable extract_Streamable() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public void insert_Streamable(org.omg.CORBA.portable.Streamable s) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.math.BigDecimal extract_fixed() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public void insert_fixed(java.math.BigDecimal f,
                org.omg.CORBA.TypeCode t) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}

