
// Copyright (C) 1998-2001
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

/**
 * TypeCodeValue represents a serialized CORBA TypeCode object
 */

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;

public class TypeCodeValue implements Serializable
{
    // not the most space-efficient representation, but the alternative
    // is a bunch of subclasses
    private int m_kind;
    private String m_id;
    private String m_name;
    private int m_length;
    private TypeCodeValue m_content;
    private java.lang.Object m_arg;
    private transient TypeCode m_typeCode;

    static final long serialVersionUID = -2067446388881696087L;


    private TypeCodeValue()
    {
    }


    public TypeCodeValue(TypeCode tc)
    {
	setValue(tc);
    }


    public TypeCode getValue()
    {
	TypeCode result = m_typeCode;

	if (result == null) {
	    ORB orb = org.omg.CORBA.ORB.init();

	    switch (m_kind) {
		// the primitive types
	    case TCKind._tk_null:
	    case TCKind._tk_void:
	    case TCKind._tk_short:
	    case TCKind._tk_ushort:
	    case TCKind._tk_long:
	    case TCKind._tk_ulong:
	    case TCKind._tk_float:
	    case TCKind._tk_double:
	    case TCKind._tk_boolean:
	    case TCKind._tk_char:
	    case TCKind._tk_octet:
	    case TCKind._tk_any:
	    case TCKind._tk_TypeCode:
	    case TCKind._tk_Principal:
		result = orb.get_primitive_tc(TCKind.from_int(m_kind));
		break;

	    case TCKind._tk_string:
		result = orb.create_string_tc(m_length);
		break;

	    case TCKind._tk_sequence:
		result = orb.create_sequence_tc(m_length, m_content.getValue());
		break;

	    case TCKind._tk_array:
		result = orb.create_array_tc(m_length, m_content.getValue());
		break;

	    case TCKind._tk_alias:
		result = orb.create_alias_tc(m_id, m_name, m_content.getValue());
		break;

	    case TCKind._tk_objref:
		result = orb.create_interface_tc(m_id, m_name);
		break;

	    case TCKind._tk_longlong:
	    case TCKind._tk_ulonglong:
	    case TCKind._tk_longdouble:
	    case TCKind._tk_wstring:
	    case TCKind._tk_wchar:
	    case TCKind._tk_fixed:
	    case TCKind._tk_except:
	    case TCKind._tk_struct:
	    case TCKind._tk_union:
		throw new RuntimeException("Unsupported type");

	    default:
		throw new RuntimeException("Unexpected type");
	    }

	    // keep it for next time
	    m_typeCode = result;
	}

	return result;
    }


    protected void setValue(TypeCode tc)
    {
	TCKind kind = tc.kind();

	// initialize members
	m_kind = kind.value();
	m_id = null;
	m_name = null;
	m_length = 0;
	m_content = null;
	m_arg = null;
	m_typeCode = tc;


	try {
	    switch (kind.value()) {
		// the primitive types
	    case TCKind._tk_null:
	    case TCKind._tk_void:
	    case TCKind._tk_short:
	    case TCKind._tk_ushort:
	    case TCKind._tk_long:
	    case TCKind._tk_ulong:
	    case TCKind._tk_float:
	    case TCKind._tk_double:
	    case TCKind._tk_boolean:
	    case TCKind._tk_char:
	    case TCKind._tk_octet:
	    case TCKind._tk_any:
	    case TCKind._tk_TypeCode:
	    case TCKind._tk_Principal:
		// nothing to do
		break;

	    case TCKind._tk_string:
		m_length = tc.length();
		break;

	    case TCKind._tk_sequence:
		m_length = tc.length();
		m_content = new TypeCodeValue(tc.content_type());
		break;

	    case TCKind._tk_array:
		m_length = tc.length();
		m_content = new TypeCodeValue(tc.content_type());
		break;

	    case TCKind._tk_alias:
		m_id = tc.id();
		m_name = tc.name();
		m_content = new TypeCodeValue(tc.content_type());
		break;

	    case TCKind._tk_objref:
		m_id = tc.id();
		m_name = tc.name();
		break;

	    case TCKind._tk_longlong:
	    case TCKind._tk_ulonglong:
	    case TCKind._tk_longdouble:
	    case TCKind._tk_wstring:
	    case TCKind._tk_wchar:
	    case TCKind._tk_fixed:
	    case TCKind._tk_except:
	    case TCKind._tk_struct:
	    case TCKind._tk_union:
		throw new RuntimeException("Unsupported type");

	    default:
		throw new RuntimeException("Unexpected type");
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    throw new RuntimeException(e.getMessage());
	}
    }


    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	m_typeCode = null;
    }


    /*********************** comment out to enable main()

			     public static void main(String[] args)
			     {
			     ORB orb = ORBLayer.instance().init(args);

			     // test the primitives

			     testType("null", orb.get_primitive_tc(TCKind.tk_null));
			     testType("void", orb.get_primitive_tc(TCKind.tk_void));
			     testType("short", orb.get_primitive_tc(TCKind.tk_short));
			     testType("ushort", orb.get_primitive_tc(TCKind.tk_ushort));
			     testType("long", orb.get_primitive_tc(TCKind.tk_long));
			     testType("ulong", orb.get_primitive_tc(TCKind.tk_ulong));
			     testType("float", orb.get_primitive_tc(TCKind.tk_float));
			     testType("double", orb.get_primitive_tc(TCKind.tk_double));
			     testType("boolean", orb.get_primitive_tc(TCKind.tk_boolean));
			     testType("char", orb.get_primitive_tc(TCKind.tk_char));
			     testType("octet", orb.get_primitive_tc(TCKind.tk_octet));
			     //testType("longlong", orb.get_primitive_tc(TCKind.tk_longlong));
			     //testType("ulonglong", orb.get_primitive_tc(TCKind.tk_ulonglong));
			     //testType("longdouble", orb.get_primitive_tc(TCKind.tk_longdouble));
			     //testType("wchar", orb.get_primitive_tc(TCKind.tk_wchar));
			     //testType("fixed", orb.get_primitive_tc(TCKind.tk_fixed));
			     testType("any", orb.get_primitive_tc(TCKind.tk_any));
			     testType("TypeCode", orb.get_primitive_tc(TCKind.tk_TypeCode));
			     testType("Principal", orb.get_primitive_tc(TCKind.tk_Principal));

			     TypeCode tc;

			     // test alias

			     tc = orb.create_alias_tc("ID", "Name",
			     orb.get_primitive_tc(TCKind.tk_double));
			     testType("alias", tc);

			     // test interface

			     tc = orb.create_interface_tc("ID", "Name");
			     testType("objref", tc);

			     // test string

			     tc = orb.create_string_tc(10);
			     testType("string", tc);

			     // test sequence

			     tc = orb.create_sequence_tc(20, orb.get_primitive_tc(TCKind.tk_long));
			     testType("sequence", tc);

			     // test array

			     tc = orb.create_array_tc(20, orb.get_primitive_tc(TCKind.tk_ulong));
			     testType("array", tc);
			     }


			     protected static void testType(String name, TypeCode tc)
			     {
			     // test a TypeCodeValue by writing it out, reading it back in,
			     // and comparing its TypeCode to the one we've been given

			     System.out.println("Testing " + name + "...");

			     try {
			     TypeCodeValue val = new TypeCodeValue(tc);
			     File f = new File("tctest.dat");

			     FileOutputStream fileOut = new FileOutputStream(f);
			     ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			     objOut.writeObject(val);
			     fileOut.close();

			     FileInputStream fileIn = new FileInputStream(f);
			     ObjectInputStream objIn = new ObjectInputStream(fileIn);
			     val = (TypeCodeValue)objIn.readObject();
			     fileIn.close();

			     f.delete();

			     TypeCode newTC = val.getValue();
			     if (! newTC.equal(tc)) {
			     System.out.println("Read/write mismatch");
			     System.exit(1);
			     }
			     }
			     catch (IOException e) {
			     System.err.println("I/O error: " + e.getMessage());
			     System.exit(1);
			     }
			     catch (ClassNotFoundException e) {
			     System.err.println("Class not found: " + e.getMessage());
			     System.exit(1);
			     }
			     }

			     /*********************** comment out to enable main() */
}










