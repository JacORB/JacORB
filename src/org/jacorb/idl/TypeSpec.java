/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

public class TypeSpec
    extends IdlSymbol
{
    protected String alias = null;
    public TypeSpec type_spec;

    public TypeSpec(int num) 
    {
	super(num);
    }

    public Object clone()
    { 
	TypeSpec ts = new TypeSpec(new_num());
	ts.type_spec = (TypeSpec)type_spec.clone();
	return ts;
    }


    public String typeName()
    {
	return type_spec.typeName();
    }

    public String signature()
    {
	return type_spec.signature();
    }

    public TypeSpec typeSpec()
    {
	return type_spec.typeSpec();
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	type_spec.setPackage(s);
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	enclosing_symbol = s;
	type_spec.setEnclosingSymbol(s);
    }

    public boolean basic()
    {
	// debug: System.out.println("TypeSpec class is: " + this.getClass().getName() );
	return type_spec.basic();
    } 

    public void set_constr(TypeDeclaration td)
    {
	ConstrTypeSpec c = new ConstrTypeSpec( new_num());
	c.c_type_spec = td;
	type_spec = c;
    }

    public void parse()
        throws ParseException
    {
	type_spec.parse();
    }

    public String toString()
    {
	try
	{
	    return type_spec.toString();
	} 
	catch ( NullPointerException np) 
	{
	    np.printStackTrace();
	    System.err.println("Compiler Error for " + type_spec + " " + typeName() );
	    System.exit(1);
	}
	return null;
    }

    /**
     * @returns a string for an expression of type TypeCode 
     * 			that describes this type
     */

    public String getTypeCodeExpression()
    {
	return type_spec.getTypeCodeExpression();
    }

    public void print(PrintWriter ps)
    {
	if( !included )
	    type_spec.print(ps);
    }

    public String holderName()
    {
	return type_spec.holderName();
    }

    /* helpers are not generated for base types, so
       there is no equivalent method to return helper
       names here. Such an operation is really only
       necessary for sequence types as a sequence's
       helper is named according to the sequence's 
       element type
    */

    public String printReadExpression(String streamname)
    {
	return type_spec.printReadExpression( streamname);
    }

    public String printReadStatement(String var_name, String streamname)
    {
	return var_name + "=" + printReadExpression(streamname) + ";";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
	return type_spec.printWriteStatement(var_name, streamname);
    }

    public String printInsertExpression()
    {
	return type_spec.printInsertExpression();
    }

    public String printExtractExpression()
    {
	return type_spec.printExtractExpression();
    }

    /** 
	for use by subclasses when generating helper classes. Writes common
	methods for all helpers to the helper class file. Must becalled after 
	beginning th class definition itself
    */

    public static void printHelperClassMethods(String className, PrintWriter ps, String type)
    {
	ps.println("\tpublic static void insert(org.omg.CORBA.Any any, " + type + " s)");
	ps.println("\t{");
	ps.println("\t\tany.type(type());");
	ps.println("\t\twrite( any.create_output_stream(),s);");
	ps.println("\t}");

	ps.println("\tpublic static " + type + " extract(org.omg.CORBA.Any any)");
	ps.println("\t{");
	ps.println("\t\treturn read(any.create_input_stream());");
	ps.println("\t}");

	ps.println("\tpublic static org.omg.CORBA.TypeCode type()");
	ps.println("\t{");
	ps.println("\t\treturn _type;");
	ps.println("\t}");
	
	ps.println("\tpublic String get_id()");
	ps.println("\t{");
	ps.println("\t\treturn id();");
	ps.println("\t}");

	ps.println("\tpublic org.omg.CORBA.TypeCode get_type()");
	ps.println("\t{");
	ps.println("\t\treturn type();");
	ps.println("\t}");

	// what are these good for??

	ps.println("\tpublic void write_Object(org.omg.CORBA.portable.OutputStream out, java.lang.Object obj)");
	ps.println("\t{");
	ps.println("\t\t throw new RuntimeException(\" not implemented\");");
	ps.println("\t}");
	
	ps.println("\tpublic java.lang.Object read_Object(org.omg.CORBA.portable.InputStream in)");
	ps.println("\t{");
	ps.println("\t\t throw new RuntimeException(\" not implemented\");");
	ps.println("\t}");
    }

}














