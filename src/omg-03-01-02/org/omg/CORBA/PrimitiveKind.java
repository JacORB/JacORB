/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA;

public class PrimitiveKind implements org.omg.CORBA.portable.IDLEntity {

    public static final int _pk_null = 0;
    public static final PrimitiveKind pk_null = new PrimitiveKind(_pk_null);

    public static final int _pk_void = 1;
    public static final PrimitiveKind pk_void = new PrimitiveKind(_pk_void);

    public static final int _pk_short = 2;
    public static final PrimitiveKind pk_short = new PrimitiveKind(_pk_short);

    public static final int _pk_long = 3;
    public static final PrimitiveKind pk_long = new PrimitiveKind(_pk_long);

    public static final int _pk_ushort = 4;
    public static final PrimitiveKind pk_ushort =
            new PrimitiveKind(_pk_ushort);

    public static final int _pk_ulong = 5;
    public static final PrimitiveKind pk_ulong = new PrimitiveKind(_pk_ulong);

    public static final int _pk_float = 6;
    public static final PrimitiveKind pk_float = new PrimitiveKind(_pk_float);

    public static final int _pk_double = 7;
    public static final PrimitiveKind pk_double =
            new PrimitiveKind(_pk_double);

    public static final int _pk_boolean = 8;
    public static final PrimitiveKind pk_boolean =
            new PrimitiveKind(_pk_boolean);

    public static final int _pk_char = 9;
    public static final PrimitiveKind pk_char = new PrimitiveKind(_pk_char);

    public static final int _pk_octet = 10;
    public static final PrimitiveKind pk_octet = new PrimitiveKind(_pk_octet);

    public static final int _pk_any = 11;
    public static final PrimitiveKind pk_any = new PrimitiveKind(_pk_any);

    public static final int _pk_TypeCode = 12;
    public static final PrimitiveKind pk_TypeCode =
            new PrimitiveKind(_pk_TypeCode);

    public static final int _pk_Principal = 13;
    public static final PrimitiveKind pk_Principal =
            new PrimitiveKind(_pk_Principal);

    public static final int _pk_string = 14;
    public static final PrimitiveKind pk_string =
            new PrimitiveKind(_pk_string);

    public static final int _pk_objref= 15;
    public static final PrimitiveKind pk_objref =
            new PrimitiveKind(_pk_objref);

    public static final int _pk_longlong = 16;
    public static final PrimitiveKind pk_longlong =
            new PrimitiveKind(_pk_longlong);

    public static final int _pk_ulonglong = 17;
    public static final PrimitiveKind pk_ulonglong =
            new PrimitiveKind(_pk_ulonglong);

    public static final int _pk_longdouble = 18;
    public static final PrimitiveKind pk_longdouble =
            new PrimitiveKind(_pk_longdouble);

    public static final int _pk_wchar = 19;
    public static final PrimitiveKind pk_wchar = new PrimitiveKind(_pk_wchar);

    public static final int _pk_wstring = 20;
    public static final PrimitiveKind pk_wstring =
            new PrimitiveKind(_pk_wstring);

    public static final int _pk_value_base = 21;
    public static final PrimitiveKind pk_value_base
            = new PrimitiveKind(_pk_value_base);

    public int value() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static PrimitiveKind from_int(int val)
                /* Issue 3669 throws org.omg.CORBA.BAD_PARAM */ {
    switch (val) {
        case _pk_null:
            return pk_null;
        case _pk_void:
            return pk_void;
        case _pk_short:
            return pk_short;
        case _pk_long:
            return pk_long;
        case _pk_ushort:
            return pk_ushort;
        case _pk_ulong:
            return pk_ulong;
        case _pk_float:
            return pk_float;
        case _pk_double:
            return pk_double;
        case _pk_boolean:
            return pk_boolean;
        case _pk_char:
            return pk_char;
        case _pk_octet:
            return pk_octet;
        case _pk_any:
            return pk_any;
        case _pk_TypeCode:
            return pk_TypeCode;
        case _pk_Principal:
            return pk_Principal;
        case _pk_string:
            return pk_string;
        case _pk_objref:
            return pk_objref;
        case _pk_longlong:
            return pk_longlong;
        case _pk_ulonglong:
            return pk_ulonglong;
        case _pk_longdouble:
            return pk_longdouble;
        case _pk_wchar:
            return pk_wchar;
        case _pk_wstring:
            return pk_wstring;
        case _pk_value_base:
            return pk_value_base;
    default:
        throw new org.omg.CORBA.BAD_PARAM();
    }
    }

    protected PrimitiveKind(int _value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.lang.Object readResolve() throws java.io.ObjectStreamException
    {
       return from_int( value() ) ;
    }
}
