/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA;

public class TCKind implements org.omg.CORBA.portable.IDLEntity {

    public static final int _tk_null = 0;
    public static final TCKind tk_null = new TCKind(_tk_null);

    public static final int _tk_void = 1;
    public static final TCKind tk_void = new TCKind(_tk_void);

    public static final int _tk_short = 2;
    public static final TCKind tk_short = new TCKind(_tk_short);

    public static final int _tk_long = 3;
    public static final TCKind tk_long = new TCKind(_tk_long);

    public static final int _tk_ushort = 4;
    public static final TCKind tk_ushort = new TCKind(_tk_ushort);

    public static final int _tk_ulong = 5;
    public static final TCKind tk_ulong = new TCKind(_tk_ulong);

    public static final int _tk_float = 6;
    public static final TCKind tk_float = new TCKind(_tk_float);

    public static final int _tk_double = 7;
    public static final TCKind tk_double = new TCKind(_tk_double);

    public static final int _tk_boolean = 8;
    public static final TCKind tk_boolean = new TCKind(_tk_boolean);

    public static final int _tk_char = 9;
    public static final TCKind tk_char = new TCKind(_tk_char);

    public static final int _tk_octet = 10;
    public static final TCKind tk_octet = new TCKind(_tk_octet);

    public static final int _tk_any = 11;
    public static final TCKind tk_any = new TCKind(_tk_any);

    public static final int _tk_TypeCode = 12;
    public static final TCKind tk_TypeCode = new TCKind(_tk_TypeCode);

    public static final int _tk_Principal = 13;
    public static final TCKind tk_Principal = new TCKind(_tk_Principal);

    public static final int _tk_objref= 14;
    public static final TCKind tk_objref = new TCKind(_tk_objref);

    public static final int _tk_struct = 15;
    public static final TCKind tk_struct = new TCKind(_tk_struct);

    public static final int _tk_union = 16;
    public static final TCKind tk_union = new TCKind(_tk_union);

    public static final int _tk_enum = 17;
    public static final TCKind tk_enum = new TCKind(_tk_enum);

    public static final int _tk_string = 18;
    public static final TCKind tk_string = new TCKind(_tk_string);

    public static final int _tk_sequence = 19;
    public static final TCKind tk_sequence = new TCKind(_tk_sequence);

    public static final int _tk_array = 20;
    public static final TCKind tk_array = new TCKind(_tk_array);

    public static final int _tk_alias = 21;
    public static final TCKind tk_alias = new TCKind(_tk_alias);

    public static final int _tk_except = 22;
    public static final TCKind tk_except = new TCKind(_tk_except);

    public static final int _tk_longlong = 23;
    public static final TCKind tk_longlong = new TCKind(_tk_longlong);

    public static final int _tk_ulonglong = 24;
    public static final TCKind tk_ulonglong = new TCKind(_tk_ulonglong);

    public static final int _tk_longdouble = 25;
    public static final TCKind tk_longdouble = new TCKind(_tk_longdouble);

    public static final int _tk_wchar = 26;
    public static final TCKind tk_wchar = new TCKind(_tk_wchar);

    public static final int _tk_wstring = 27;
    public static final TCKind tk_wstring = new TCKind(_tk_wstring);

    public static final int _tk_fixed = 28;
    public static final TCKind tk_fixed = new TCKind(_tk_fixed);

    public static final int _tk_value = 29;
    public static final TCKind tk_value = new TCKind(_tk_value);

    public static final int _tk_value_box = 30;
    public static final TCKind tk_value_box = new TCKind(_tk_value_box);

    public static final int _tk_native = 31;
    public static final TCKind tk_native = new TCKind(_tk_native);

    public static final int _tk_abstract_interface = 32;
    public static final TCKind tk_abstract_interface =
            new TCKind(_tk_abstract_interface);

    public static final int _tk_local_interface = 33;
    public static final TCKind tk_local_interface =
            new TCKind(_tk_local_interface);

    public int value() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static TCKind from_int(int val)
       /* Issue 3669  throws org.omg.CORBA.BAD_PARAM */ {
    switch (val) {
        case _tk_null:
            return tk_null;
        case _tk_void:
            return tk_void;
        case _tk_short:
            return tk_short;
        case _tk_long:
            return tk_long;
        case _tk_ushort:
            return tk_ushort;
        case _tk_ulong:
            return tk_ulong;
        case _tk_float:
            return tk_float;
        case _tk_double:
            return tk_double;
        case _tk_boolean:
            return tk_boolean;
        case _tk_char:
            return tk_char;
        case _tk_octet:
            return tk_octet;
        case _tk_any:
            return tk_any;
        case _tk_TypeCode:
            return tk_TypeCode;
        case _tk_Principal:
            return tk_Principal;
        case _tk_objref:
            return tk_objref;
        case _tk_struct:
            return tk_struct;
        case _tk_union:
            return tk_union;
        case _tk_enum:
            return tk_enum;
        case _tk_string:
            return tk_string;
        case _tk_sequence:
            return tk_sequence;
        case _tk_array:
            return tk_array;
        case _tk_alias:
            return tk_alias;
        case _tk_except:
            return tk_except;
        case _tk_longlong:
            return tk_longlong;
        case _tk_ulonglong:
            return tk_ulonglong;
        case _tk_longdouble:
            return tk_longdouble;
        case _tk_wchar:
            return tk_wchar;
        case _tk_wstring:
            return tk_wstring;
        case _tk_fixed:
            return tk_fixed;
        case _tk_value:
            return tk_value;
        case _tk_value_box:
            return tk_value_box;
        case _tk_native:
            return tk_native;
        case _tk_abstract_interface:
            return tk_abstract_interface;
        case _tk_local_interface:
            return tk_local_interface;
    default:
        throw new org.omg.CORBA.BAD_PARAM();
    }
    }

    protected TCKind(int _value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.lang.Object readResolve() throws java.io.ObjectStreamException
    {
       return from_int( value() ) ;
    }
}
