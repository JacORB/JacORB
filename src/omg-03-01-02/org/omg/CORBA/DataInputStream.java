/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface DataInputStream extends org.omg.CORBA.portable.ValueBase {

    public org.omg.CORBA.Any read_any();
    public boolean read_boolean();
    public char read_char();
    public char read_wchar();
    public byte read_octet();
    public short read_short();
    public short read_ushort();
    public int read_long();
    public int read_ulong();
    public long read_longlong();
    public long read_ulonglong();
    public float read_float();
    public double read_double();
    public double read_longdouble();
    public java.lang.String read_string();
    public java.lang.String read_wstring();
    public org.omg.CORBA.Object read_Object();
    public java.lang.Object read_Abstract();
    public java.io.Serializable read_Value();
    public org.omg.CORBA.TypeCode read_TypeCode();

    public void read_any_array(org.omg.CORBA.AnySeqHolder seq, 
                        int offset, 
                        int length);
    public void read_boolean_array(org.omg.CORBA.BooleanSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_char_array(org.omg.CORBA.CharSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_wchar_array(org.omg.CORBA.WCharSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_octet_array(org.omg.CORBA.OctetSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_short_array(org.omg.CORBA.ShortSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_ushort_array(org.omg.CORBA.UShortSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_long_array(org.omg.CORBA.LongSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_ulong_array(org.omg.CORBA.ULongSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_ulonglong_array(org.omg.CORBA.ULongLongSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_longlong_array(org.omg.CORBA.LongLongSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_float_array(org.omg.CORBA.FloatSeqHolder seq, 
                        int offset, 
                        int length);
    public void read_double_array(org.omg.CORBA.DoubleSeqHolder seq, 
                        int offset, 
                        int length);
}
