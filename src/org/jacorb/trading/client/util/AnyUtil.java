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

package org.jacorb.trading.client.util;

import java.io.*;
import org.omg.CORBA.*;
import org.omg.DynamicAny.*;

public class AnyUtil
{
    private AnyUtil()
    {
    }


    public static void print(org.omg.CORBA.ORB _orb, PrintWriter pw, Any val)
    {
	org.omg.CORBA.ORB orb = _orb;
	try {
	    TypeCode tc = val.type();

	    while (tc.kind() == TCKind.tk_alias)
		tc = tc.content_type();

	    TCKind kind = tc.kind();
	    if (kind == TCKind.tk_sequence)
		printSequence(_orb,pw, val);

	    switch (kind.value()) {
	    case TCKind._tk_short: {
		int s = val.extract_short();
		pw.print(s);
	    }
	    break;

	    case TCKind._tk_long: {
		int l = val.extract_long();
		pw.print(l);
	    }
	    break;

	    case TCKind._tk_ushort: {
		int i = val.extract_ushort();
		pw.print(i);
	    }
	    break;

	    case TCKind._tk_ulong: {
		long l = val.extract_ulong();
		pw.print(l);
	    }
	    break;

	    case TCKind._tk_float: {
		float f = val.extract_float();
		pw.print(f);
	    }
	    break;

	    case TCKind._tk_double: {
		double d = val.extract_double();
		pw.print(d);
	    }
	    break;

	    case TCKind._tk_boolean: {
		boolean b = val.extract_boolean();
		pw.print(b);
	    }
	    break;

	    case TCKind._tk_char: {
		char c = val.extract_char();
		pw.print(c);
	    }
	    break;

	    case TCKind._tk_string: {
		String s = val.extract_string();
		pw.print("'" + s + "'");
	    }
	    break;

	    case TCKind._tk_octet: {
		byte b = val.extract_octet();
		pw.print((int)b);
	    }
	    break;

	    case TCKind._tk_objref: {
		org.omg.CORBA.Object obj = val.extract_Object();
		if (obj == null)
		    pw.print("nil");
		else
		{
		    _orb.object_to_string(obj);
		}
	    }
	    break;
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    e.printStackTrace();
	}
	catch (BAD_OPERATION e) {
	    e.printStackTrace();
	}
    }


    public static void print(PrintWriter pw, TypeCode tc)
    {
	try {
	    TCKind kind = tc.kind();

	    switch (kind.value()) {
	    case TCKind._tk_objref:
		pw.print("interface " + tc.name());
		break;

	    case TCKind._tk_alias: {
		pw.print("typedef ");
		TypeCode content = tc.content_type();
		printName(pw, content);
		pw.print(" " + tc.name());
	    }
	    break;

	    case TCKind._tk_string: {
		pw.print("string");
		int len = tc.length();
		if (len != 0)
		    pw.print("<" + len + ">");
	    }
	    break;

	    case TCKind._tk_sequence: {
		pw.print("sequence<");
		TypeCode content = tc.content_type();
		printName(pw, content);
		int len = tc.length();
		if (len != 0)
		    pw.print(", " + len);
		pw.print(">");
	    }
	    break;

	    case TCKind._tk_array: {
		TypeCode content = tc.content_type();
		printName(pw, content);
		int len = tc.length();
		pw.print("[" + len + "]");
	    }
	    break;

	    case TCKind._tk_union:
	    case TCKind._tk_enum:
	    case TCKind._tk_struct:
	    case TCKind._tk_except:
	    default:
		printKind(pw, kind);
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    e.printStackTrace();
	}
    }


    protected static void printSequence(org.omg.CORBA.ORB orb, PrintWriter pw, Any val)
    {
	try {
	    TypeCode tc = val.type();

	    while (tc.kind() == TCKind.tk_alias)
		tc = tc.content_type();

	    TypeCode contentTC = tc.content_type();
	    TCKind kind = contentTC.kind();

	    DynAnyFactory factory = DynAnyFactoryHelper.narrow(orb.resolve_initial_references("DynAnyFactory"));
	    org.omg.DynamicAny.DynAny da =  factory.create_dyn_any(val);
	    org.omg.DynamicAny.DynSequence ds = org.omg.DynamicAny.DynSequenceHelper.narrow(da);
	    int len = ds.get_length();

	    switch (kind.value()) {
	    case TCKind._tk_short: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_short());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_long: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_long());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_ushort: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_ushort());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_ulong: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_ulong());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_float: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_float());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_double: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_double());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_boolean: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_boolean());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_char: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print(ds.current_component().get_char());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_string: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print("'" + ds.current_component().get_string() + "'");
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_octet: {
		for (int i = 0; i < len; i++, ds.next()) {
		    pw.print((int)ds.current_component().get_octet());
		    if (i < len - 1)
			pw.print(", ");
		}
	    }
	    break;

	    case TCKind._tk_objref: {
		for (int i = 0; i < len; i++, ds.next()) {
		    org.omg.CORBA.Object obj = ds.current_component().get_reference();
		    if (obj == null)
			pw.println("nil");
		    else
		    {
			orb.object_to_string(obj);
		    }

		    if (i < len - 1)
			pw.println();
		}
	    }
	    break;
	    }

	    da.destroy();
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    e.printStackTrace();
	}
	// GB:
	catch (org.omg.CORBA.ORBPackage.InvalidName e) {
	    e.printStackTrace();
	}
	catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch e) {
	    e.printStackTrace();
	}
	catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue e) {
	    e.printStackTrace();
	}
	catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e) {
	    e.printStackTrace();
	}
	catch (BAD_OPERATION e) {
	    e.printStackTrace();
	}
    }


    protected static void printName(PrintWriter pw, TypeCode tc)
    {
	try {
	    TCKind kind = tc.kind();
	    switch (kind.value()) {
	    case TCKind._tk_objref:
	    case TCKind._tk_union:
	    case TCKind._tk_enum:
	    case TCKind._tk_struct:
	    case TCKind._tk_except:
	    case TCKind._tk_alias:
		pw.print(tc.name());
		break;

	    case TCKind._tk_string: {
		pw.print("string");
		int len = tc.length();
		if (len != 0)
		    pw.print("<" + len + ">");
	    }
	    break;

	    case TCKind._tk_sequence: {
		pw.print("sequence<");
		TypeCode content = tc.content_type();
		printName(pw, content);
		int len = tc.length();
		if (len != 0)
		    pw.print(", " + len);
		pw.print(">");
	    }
	    break;

	    case TCKind._tk_array: {
		TypeCode content = tc.content_type();
		printName(pw, content);
		int len = tc.length();
		pw.print("[" + len + "]");
	    }

	    default:
		printKind(pw, kind);
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    e.printStackTrace();
	}
    }


    protected static void printKind(PrintWriter pw, TCKind kind)
    {
	switch (kind.value()) {
	case TCKind._tk_null:
	    pw.print("null");
	    break;

	case TCKind._tk_void:
	    pw.print("void");
	    break;

	case TCKind._tk_short:
	    pw.print("short");
	    break;

	case TCKind._tk_long:
	    pw.print("long");
	    break;

	case TCKind._tk_ushort:
	    pw.print("unsigned short");
	    break;

	case TCKind._tk_ulong:
	    pw.print("unsigned long");
	    break;

	case TCKind._tk_float:
	    pw.print("float");
	    break;

	case TCKind._tk_double:
	    pw.print("double");
	    break;

	case TCKind._tk_boolean:
	    pw.print("boolean");
	    break;

	case TCKind._tk_char:
	    pw.print("char");
	    break;

	case TCKind._tk_octet:
	    pw.print("octet");
	    break;

	case TCKind._tk_any:
	    pw.print("any");
	    break;

	case TCKind._tk_TypeCode:
	    pw.print("TypeCode");
	    break;

	case TCKind._tk_Principal:
	    pw.print("Principal");
	    break;

	case TCKind._tk_objref:
	    pw.print("Object");
	    break;

	case TCKind._tk_struct:
	    pw.print("struct");
	    break;

	case TCKind._tk_union:
	    pw.print("union");
	    break;

	case TCKind._tk_enum:
	    pw.print("enum");
	    break;

	case TCKind._tk_string:
	    pw.print("string");
	    break;

	case TCKind._tk_sequence:
	    pw.print("sequence");
	    break;

	case TCKind._tk_array:
	    pw.print("array");
	    break;

	case TCKind._tk_alias:
	    pw.print("alias");
	    break;

	case TCKind._tk_except:
	    pw.print("exception");
	    break;

	default:
	    pw.print("(unknown)");
	    break;
	}
    }
}




