
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.db.simple.offers;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynSequence;

public class AnyValue implements Serializable
{
    private TypeCodeValue m_type;
    private java.lang.Object m_value;
    static final long serialVersionUID = 3945922728443512828L;

    private transient org.omg.DynamicAny.DynAnyFactory factory;
    private transient org.omg.CORBA.ORB orb;
    private transient boolean initialized = false;

    private AnyValue()
    {
    }


    public AnyValue(org.omg.CORBA.ORB orb, Any any)
    {
	init( orb );
	setValue(any);
    }

    public void init(org.omg.CORBA.ORB orb)
    {
	this.orb = orb;
	try
	{	
	    factory = DynAnyFactoryHelper.narrow(orb.resolve_initial_references("DynAnyFactory"));
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	}	
	initialized = true;
    }


    /** Returns true if we can store a value of the given type */
    public static boolean isTypeSupported(org.omg.CORBA.TypeCode type)
    {
	boolean result = false;

	try {
	    TypeCode tc = type;

	    while (tc.kind() == TCKind.tk_alias)
		tc = tc.content_type();

	    TCKind kind = tc.kind();

	    if (kind == TCKind.tk_null || kind == TCKind.tk_void)
		result = true;
	    else {
		if (kind == TCKind.tk_sequence) {
		    TypeCode contentType = tc.content_type();
		    kind = contentType.kind();
		}

		switch (kind.value()) {
		case TCKind._tk_short:
		case TCKind._tk_long:
		case TCKind._tk_ushort:
		case TCKind._tk_ulong:
		case TCKind._tk_float:
		case TCKind._tk_double:
		case TCKind._tk_boolean:
		case TCKind._tk_char:
		case TCKind._tk_string:
		case TCKind._tk_octet:
		    result = true;
		    break;
		}
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) 
	{
	    org.jacorb.util.Debug.output(4, e );
	    // ignore
	}
	catch (BAD_OPERATION e) 
	{
	    org.jacorb.util.Debug.output(4, e );
	    // ignore
	}
	jacorb.util.Debug.output(4, "AnyValue, tc_kind " + type.kind() + (result? " " : " not" ) + " supported");
	return result;
    }


    public Any getValue()
    {
	if( !initialized )
	    throw new RuntimeException("AnyVale not initialized!");
	Any result = null;
	TypeCode origType = m_type.getValue();

	boolean isSequence = false;

	try {
	    TypeCode tc = origType;
	    while (tc.kind() == TCKind.tk_alias)
		tc = tc.content_type();

	    TCKind kind = tc.kind();

	    if (kind == TCKind.tk_sequence) {
		TypeCode content = tc.content_type();
		kind = content.kind();
		isSequence = true;
	    }

	    switch (kind.value()) {
	    case TCKind._tk_null:
	    case TCKind._tk_void:
		result = orb.create_any();
		result.type(origType);
		break;

	    case TCKind._tk_short: {
		if (isSequence) {
		    short[] arr = (short[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length; ds.next(),i++)
			ds.current_component().insert_short(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Short val = (Short)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_short(val.shortValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_long: {
		if (isSequence) {
		    int[] arr = (int[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_long(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Integer val = (Integer)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_long(val.intValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_ushort: {
		if (isSequence) {
		    short[] arr = (short[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length; ds.next(),i++)
			ds.current_component().insert_ushort(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Short val = (Short)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_ushort(val.shortValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_ulong: {
		if (isSequence) {
		    int[] arr = (int[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length; ds.next(),i++)
			ds.current_component().insert_ulong(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Integer val = (Integer)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_ulong(val.intValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_float: {
		if (isSequence) {
		    float[] arr = (float[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_float(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Float val = (Float)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_float(val.floatValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_double: {
		if (isSequence) {
		    double[] arr = (double[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_double(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Double val = (Double)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_double(val.doubleValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_boolean: {
		if (isSequence) {
		    boolean[] arr = (boolean[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_boolean(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Boolean val = (Boolean)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_boolean(val.booleanValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_char: {
		if (isSequence) {
		    char[] arr = (char[])m_value;
		    org.omg.DynamicAny.DynSequence ds =(DynSequence) factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_char(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Character val = (Character)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_char(val.charValue());
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_string: {
		if (isSequence) {
		    String[] arr = (String[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length;ds.next(), i++)
			ds.current_component().insert_string(arr[i]);
		    result = ds.to_any();
		}
		else {
		    String val = (String)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_string(val);
		    result = da.to_any();
		}
	    }
	    break;

	    case TCKind._tk_octet: {
		if (isSequence) {
		    byte[] arr = (byte[])m_value;
		    org.omg.DynamicAny.DynSequence ds = (DynSequence)factory.create_dyn_any_from_type_code(origType);
		    ds.set_length(arr.length);
		    ds.rewind();
		    for (int i = 0; i < arr.length; ds.next(), i++)
			ds.current_component().insert_octet(arr[i]);
		    result = ds.to_any();
		}
		else {
		    Byte val = (Byte)m_value;
		    org.omg.DynamicAny.DynAny da = factory.create_dyn_any_from_type_code(origType);
		    da.insert_octet(val.byteValue());
		    result = da.to_any();
		}
	    }
	    break;
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    org.jacorb.util.Debug.output(2,e);
	    throw new RuntimeException(e.getMessage());
	}
	catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue e) {
	    org.jacorb.util.Debug.output(2,e);
	    throw new RuntimeException(e.getMessage());
	}
	catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch e) {
	    org.jacorb.util.Debug.output(2,e);
	    throw new RuntimeException(e.getMessage());
	}
	catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e) {
	    org.jacorb.util.Debug.output(2,e);
	    throw new RuntimeException(e.getMessage());
	}
	//**//
	return result;
    }


    protected void setValue(Any val)
    {
	if( !initialized )
	    throw new RuntimeException("AnyVale not initialized!");

	TypeCode origType = val.type();
	m_type = new TypeCodeValue(origType);

	boolean isSequence = false;

	try {
	    TypeCode tc = origType;
	    while (tc.kind() == TCKind.tk_alias)
		tc = tc.content_type();

	    TCKind kind = tc.kind();

	    if (kind == TCKind.tk_sequence) {
		TypeCode content = tc.content_type();
		kind = content.kind();
		isSequence = true;
	    }

	    org.omg.DynamicAny.DynAny da = factory.create_dyn_any(val);

	    switch (kind.value()) {
	    case TCKind._tk_null:
		m_value = null;
		break;

	    case TCKind._tk_void:
		m_value = null;
		break;

	    case TCKind._tk_short: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    short[] arr = new short[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_short();
		    m_value = arr;
		}
		else
		    m_value = new Short(da.get_short());
	    }
	    break;

	    case TCKind._tk_long: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    int[] arr = new int[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_long();
		    m_value = arr;
		}
		else
		    m_value = new Integer(da.get_long());
	    }
	    break;

	    case TCKind._tk_ushort: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    short[] arr = new short[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_ushort();
		    m_value = arr;
		}
		else
		    m_value = new Short(da.get_ushort());
	    }
	    break;

	    case TCKind._tk_ulong: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    int[] arr = new int[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_ulong();
		    m_value = arr;
		}
		else
		    m_value = new Integer(da.get_ulong());
	    }
	    break;

	    case TCKind._tk_float: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    float[] arr = new float[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_float();
		    m_value = arr;
		}
		else
		    m_value = new Float(da.get_float());
	    }
	    break;

	    case TCKind._tk_double: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    double[] arr = new double[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_double();
		    m_value = arr;
		}
		else
		    m_value = new Double(da.get_double());
	    }
	    break;

	    case TCKind._tk_boolean: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    boolean[] arr = new boolean[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_boolean();
		    m_value = arr;
		}
		else
		    m_value = new Boolean(da.get_boolean());
	    }
	    break;

	    case TCKind._tk_char: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    char[] arr = new char[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_char();
		    m_value = arr;
		}
		else
		    m_value = new Character(da.get_char());
	    }
	    break;

	    case TCKind._tk_string: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    String[] arr = new String[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_string();
		    m_value = arr;
		}
		else
		    m_value = da.get_string();
	    }
	    break;

	    case TCKind._tk_octet: {
		if (isSequence) {
		    org.omg.DynamicAny.DynSequence ds = DynSequenceHelper.narrow(da);
		    int len = ds.get_length();
		    byte[] arr = new byte[len];
		    for (int i = 0; i < len; i++, ds.next())
			arr[i] = ds.current_component().get_octet();
		    m_value = arr;
		}
		else
		    m_value = new Byte(da.get_octet());
	    }
	    break;

	    default:
		throw new RuntimeException("Unsupported type");
	    }

	    da.destroy();
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    throw new RuntimeException(e.getMessage());
	}
	// GB: name changes
	catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch e) {
	    throw new RuntimeException(e.getMessage());
	}
	catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e) {
	    throw new RuntimeException(e.getMessage());
	}
	catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue e) {
	    throw new RuntimeException(e.getMessage());
	}
	catch (BAD_OPERATION e) {
	    throw new RuntimeException();
	}
    }

    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	init( org.jacorb.trading.TradingService.getORB());
    }


}




