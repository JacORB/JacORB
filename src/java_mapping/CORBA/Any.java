package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
abstract public class Any {

	abstract public boolean equal(Any rhs);

	abstract public TypeCode type();
	abstract public void type(TypeCode type);

	/**
	 * throw excep when typecode inconsist with value
	 */
	abstract public void read_value(org.omg.CORBA.portable.InputStream in, TypeCode type) throws org.omg.CORBA.MARSHAL;
	abstract public void write_value(org.omg.CORBA.portable.OutputStream out);

	abstract public org.omg.CORBA.portable.OutputStream create_output_stream();
	abstract public org.omg.CORBA.portable.InputStream create_input_stream();

	abstract public short extract_short() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_short(short value);  

	abstract public int extract_long() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_long(int value);

	abstract public long extract_longlong() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_longlong(long value);

	abstract public short extract_ushort() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_ushort(short value);

	abstract public int extract_ulong() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_ulong(int value);

	abstract public long extract_ulonglong() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_ulonglong(long value);

	abstract public float extract_float() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_float(float value);

	abstract public double extract_double() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_double(double value);

	abstract public boolean extract_boolean() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_boolean(boolean value);

	abstract public char extract_char() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_char(char value) throws org.omg.CORBA.DATA_CONVERSION;

	abstract public char extract_wchar() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_wchar(char value);

	abstract public byte extract_octet() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_octet(byte value);

	abstract public Any extract_any() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_any(Any value);

	abstract public org.omg.CORBA.Object extract_Object() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_Object(org.omg.CORBA.Object value);
	abstract public void insert_Object(org.omg.CORBA.Object value, org.omg.CORBA.TypeCode type) throws org.omg.CORBA.BAD_PARAM;

	abstract public java.io.Serializable extract_Value() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_Value(java.io.Serializable value);
	abstract public void insert_Value(java.io.Serializable value, org.omg.CORBA.TypeCode type) throws org.omg.CORBA.MARSHAL;

	abstract public String extract_string() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_string(String value) throws org.omg.CORBA.DATA_CONVERSION, org.omg.CORBA.MARSHAL;

	abstract public String extract_wstring() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_wstring(String value) throws org.omg.CORBA.MARSHAL;

	abstract public TypeCode extract_TypeCode() throws org.omg.CORBA.BAD_OPERATION;
	abstract public void insert_TypeCode(TypeCode value);

	/** 
	 * @deprecated 
	 */
	public org.omg.CORBA.Principal extract_Principal() throws org.omg.CORBA.BAD_OPERATION {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	/** 
	 * @deprecated 
	 */
	public void insert_Principal(Principal value) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	/** 
	 * attempting to insert a native type into an Any using this method shall 
	 * throw a org.omg.CORBA.MARSHAL exep
	 */
	public void insert_Streamable(org.omg.CORBA.portable.Streamable  value) throws org.omg.CORBA.BAD_INV_ORDER {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public org.omg.CORBA.portable.Streamable extract_Streamable() {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public java.math.BigDecimal extract_fixed() {
		throw new org.omg.CORBA.NO_IMPLEMENT();	
	}

	public void insert_fixed(java.math.BigDecimal value) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
	public void insert_fixed(java.math.BigDecimal value, org.omg.CORBA.TypeCode type) throws org.omg.CORBA.BAD_INV_ORDER {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
}


