/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface DataOutputStream extends org.omg.CORBA.portable.ValueBase {

    public void write_any (org.omg.CORBA.Any value);
    public void write_boolean (boolean value);
    public void write_char (char value);
    public void write_wchar (char value);
    public void write_octet (byte value);
    public void write_short (short value);
    public void write_ushort (short value);
    public void write_long (int value);
    public void write_ulong (int value);
    public void write_longlong (long value);
    public void write_ulonglong (long value);
    public void write_float (float value);
    public void write_double (double value);
    public void write_longdouble (double value);
    public void write_string (java.lang.String value);
    public void write_wstring (java.lang.String value);
    public void write_Object (org.omg.CORBA.Object value);
    public void write_Abstract (java.lang.Object value);
    public void write_Value (java.io.Serializable value);
    public void write_TypeCode (org.omg.CORBA.TypeCode value);

    public void write_any_array (org.omg.CORBA.Any[] seq, 
                             int offset, 
                             int length);
    public void write_boolean_array (boolean[] seq, 
                                 int offset, 
                                 int length);
    public void write_char_array (char[] seq, 
                              int offset, 
                              int length);
    public void write_wchar_array (char[] seq, 
                               int offset, 
                               int length);
    public void write_octet_array (byte[] seq, 
                               int offset, 
                               int length);
    public void write_short_array (short[] seq, 
                               int offset, 
                               int length);
    public void write_ushort_array (short[] seq, 
                                int offset, 
                                int length);
    public void write_long_array (int[] seq, 
                              int offset, 
                              int length);
    public void write_ulong_array (int[] seq, 
                               int offset, 
                               int length);
    public void write_longlong_array (long[] seq, 
                                  int offset, 
                                  int length);
    public void write_ulonglong_array (long[] seq, 
                                   int offset, 
                                   int length);
    public void write_float_array (float[] seq, 
                               int offset, 
                               int length);
    public void write_double_array (double[] seq, 
                                int offset, 
                                int length);
}
