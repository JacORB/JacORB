package org.omg.CORBA.portable; 

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public abstract class OutputStream extends java.io.OutputStream {
   
	public org.omg.CORBA.ORB orb() {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}      

	public void write(int b) throws java.io.IOException {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}  
 
 	public abstract InputStream create_input_stream();

	public abstract void write_boolean    (boolean        value);
	public abstract void write_char       (char           value);
	public abstract void write_wchar      (char           value);
	public abstract void write_octet      (byte           value);
	public abstract void write_short      (short          value);
	public abstract void write_ushort     (short          value);
	public abstract void write_long       (int            value);
	public abstract void write_ulong      (int            value);
	public abstract void write_longlong   (long           value);
	public abstract void write_ulonglong  (long           value);
	public abstract void write_float      (float          value);
	public abstract void write_double     (double         value);
	public abstract void write_string     (String         value);
	public abstract void write_wstring    (String         value);

	public abstract void write_boolean_array(boolean[] value, int offset, int length);
	public abstract void write_char_array(char[] value, int offset, int length);
	public abstract void write_wchar_array(char[] value, int offset, int length);
	public abstract void write_octet_array(byte[] value, int offset, int length);
	public abstract void write_short_array(short[] value, int offset, int length);
	public abstract void write_ushort_array(short[] value, int offset, int length);
	public abstract void write_long_array(int[] value, int offset, int length);
	public abstract void write_ulong_array(int[] value, int offset, int length);
	public abstract void write_longlong_array(long[] value, int offset, int length);
	public abstract void write_ulonglong_array(long[] value, int offset, int length);
	public abstract void write_float_array(float[] value, int offset, int length);
	public abstract void write_double_array(double[] value, int offset, int length);

	public abstract void write_Object     (org.omg.CORBA.Object value);
	public abstract void write_TypeCode   (org.omg.CORBA.TypeCode value);
	public abstract void write_any        (org.omg.CORBA.Any value);

	public void write_fixed(java.math.BigDecimal value) {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}      

	public void write_Context(org.omg.CORBA.Context ctx, org.omg.CORBA.ContextList context_list) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	/** 
	 * @deprecated Deprecated by CORBA 2.2
	 */
	public void write_Principal(org.omg.CORBA.Principal value) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public void write_fixed_array(java.math.BigDecimal[] value, int offset, int length) {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 

/* 
	public void write_Value(java.io.Serializable value) {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 
	
	public void write_Value(java.io.Serializable value, ValueHelper value_helper) {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 

	public void write_Abstract(java.lang.Object obj) {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 

	public void start_block() {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 

	public void end_block() {     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	} 
*/
}


