/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */


class StructType 
    extends TypeDeclaration 
    implements Scope
{
    private boolean written = false;
    public boolean exc;
    public MemberList memberlist = null;
    private boolean parsed = false;
    private ScopeData scopeData;

    public StructType(int num)
    {
	super(num);
	pack_name = "";
    }


    public void setScopeData(ScopeData data)
    {
        scopeData = data;
    }

    public ScopeData getScopeData()
    {
        return scopeData;
    }

    /**
     * @return true if this struct represents an IDL exception
     */

    public boolean isException()
    {
        return exc;
    }
  
    public Object clone()
    {
	StructType st = new StructType( new_num());
	st.pack_name = this.pack_name;
	st.name = this.name;
	st.memberlist = this.memberlist;
	st.included = this.included;
	st.token = this.token;
	st.exc = this.exc;
	st.scopeData = this.scopeData;
	st.enclosing_symbol = this.enclosing_symbol;
	return st;
    }

    public TypeDeclaration declaration()
    {
	return this;
    };

    public String typeName()
    {
        if( typeName == null )
            setPrintPhaseNames();
        return typeName;
    }

    public String signature()
    {
	return "L" + typeName() + ";";
    }

    public boolean basic()
    {
	return false;
    } 

    public void set_memberlist( MemberList m )
    {
	m.setStruct(this);
	memberlist = m;
	memberlist.setPackage( name );
	if( memberlist != null )
	    memberlist.setEnclosingSymbol(this);    
    }

    public void set_included(boolean i)
    {
	included = i;
    }


    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	if( pack_name.length() > 0 )
	    pack_name = new String( s + "." + pack_name );
	else
	    pack_name = s;

	if( memberlist != null )
	    memberlist.setPackage( s);
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	{
	    System.err.println("was " + enclosing_symbol.getClass().getName() + " now: " + s.getClass().getName());
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	}
	enclosing_symbol = s;	
    }

    public String toString()
    {
	return typeName();
    }

    public void parse() 	
    {
	if( parsed )
	    throw new RuntimeException("Compiler error: Struct already parsed!");
        escapeName();

	try
	{
	    // important: typeName must be set _before_ a new scope is introduced,
	    // otherwise the typeName for this struct class will be the same
	    // as the package name for the new pseudo scope!

	    ScopedName.definePseudoScope( full_name());
	    ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
	    ctspec.c_type_spec = this;

	    NameTable.define( full_name(), "type" );
	    TypeMap.typedef( full_name(), ctspec );
	} 
	catch ( NameAlreadyDefined nad )
	{
	    parser.error("Struct " + typeName() + " already defined", token);
	}
	if( memberlist != null )
	{
	    memberlist.parse();
	}

	parsed = true;
    }

    public String className()
    {
	String fullName = typeName();
	if( fullName.indexOf('.') > 0 )
	{
	    return fullName.substring( fullName.lastIndexOf('.') + 1 );
	} 
	else 
	{
	    return fullName;
	}
    }

    public String printReadExpression(String Streamname)
    {
	//	return typeName() + "Helper.read(" + Streamname +")" ;
	return toString() + "Helper.read(" + Streamname +")" ;
    }

    public String printWriteStatement(String var_name, String streamname)
    {
	//return typeName()+"Helper.write(" + streamname +"," + var_name +");";
	return toString()+"Helper.write(" + streamname +"," + var_name +");";
    }

    public String holderName()
    {
	return typeName() + "Holder";
    }

    /**
     * @returns a string for an expression of type TypeCode that describes this type
     */

    public String getTypeCodeExpression()
    {
	StringBuffer sb = new StringBuffer();
	sb.append("org.omg.CORBA.ORB.init().create_struct_tc(" + 
                  typeName() + "Helper.id(),\"" + className()+ "\",");

	if( memberlist != null )
	{
	    sb.append("new org.omg.CORBA.StructMember[]{");
	    for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
	    {
		Member m = (Member)e.nextElement();
		Declarator d = m.declarator;
		sb.append("new org.omg.CORBA.StructMember(\"" + d.name() + "\",");
		sb.append(m.type_spec.typeSpec().getTypeCodeExpression() + ",null)");
		if( e.hasMoreElements() )
		    sb.append(",");
	    }
	    sb.append("}");
	}
	else
	{
	    sb.append("new org.omg.CORBA.StructMember[0]");
	}
	sb.append(")");

	return  sb.toString(); 
    }

    private void printHolderClass(String className, PrintWriter ps)
    {
	if( !pack_name.equals(""))
	    ps.println("package " + pack_name + ";" );

	ps.println("final public class " + className + "Holder");
	ps.println("\timplements org.omg.CORBA.portable.Streamable");
	ps.println("{");

	ps.println("\tpublic " + typeName() + " value;\n");

	ps.println("\tpublic " + className + "Holder ()");
	ps.println("\t{");
	ps.println("\t}");

	ps.println("\tpublic " + className + "Holder (" + typeName() + " initial)");
	ps.println("\t{");
	ps.println("\t\tvalue = initial;");
	ps.println("\t}");

	ps.println("\tpublic org.omg.CORBA.TypeCode _type()");
	ps.println("\t{");
	ps.println("\t\treturn " + typeName() + "Helper.type();");
	ps.println("\t}");

	ps.println("\tpublic void _read(org.omg.CORBA.portable.InputStream _in)");
	ps.println("\t{");
	ps.println("\t\tvalue = " + typeName() + "Helper.read(_in);");
	ps.println("\t}");

	ps.println("\tpublic void _write(org.omg.CORBA.portable.OutputStream _out)");
	ps.println("\t{");
	ps.println("\t\t" + typeName() + "Helper.write(_out,value);");
	ps.println("\t}");

	ps.println("}");
    }


    private void printHelperClass(String className, PrintWriter ps)
    {
	if( !pack_name.equals(""))
	    ps.println("package " + pack_name + ";" );

	ps.println("public class " + className + "Helper");
	ps.println("{");
	ps.println("\tprivate static org.omg.CORBA.TypeCode _type = "+getTypeCodeExpression()+";");

	String type = typeName();

	ps.println("\tpublic " + className + "Helper ()");
	ps.println("\t{");
	ps.println("\t}");

	TypeSpec.printHelperClassMethods(className, ps, type);

	printIdMethod( ps ); // inherited from IdlSymbol

	/* read */
	ps.println("\tpublic static " +type+ " read(org.omg.CORBA.portable.InputStream in)");
	ps.println("\t{");

	ps.println("\t\t" + type+ " result = new " + type + "();");
	if( exc )
	{
	    ps.println("\t\tif(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL(\"wrong id\");");
	}
	if( memberlist != null )
	{
	    for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
	    {
		Member m = (Member)e.nextElement();
		Declarator d = m.declarator;
		ps.println("\t\t" + m.type_spec.typeSpec().printReadStatement("result." + d.name(), "in"));
	    }
	}
	ps.println("\t\treturn result;");
	ps.println("\t}");

	/* write */
	ps.println("\tpublic static void write(org.omg.CORBA.portable.OutputStream out, " + type + " s)");
	ps.println("\t{");

	if( exc )
	{
	    ps.println("\t\tout.write_string(id());");
	}

	if( memberlist != null )
	{
	    for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
	    {
		Member m = (Member)e.nextElement();
		Declarator d = m.declarator;
		ps.println( m.type_spec.typeSpec().printWriteStatement("s." + d.name(), "out") );
	    }
	}
	ps.println("\t}");
	ps.println("}");
    }


    private void printStructClass(String className, PrintWriter ps)
    {
	String fullClassName = className;

	if( !pack_name.equals(""))
	{
	    fullClassName = pack_name + "." + className;

	    ps.println("package " + pack_name + ";" );
	}

	ps.println("public final class " + className );

	if( exc )
	    ps.println("\textends org.omg.CORBA.UserException");
	else
	    ps.println("\timplements org.omg.CORBA.portable.IDLEntity");
	ps.println("{");

	if( memberlist != null )
	{
	    // print member declarations

	    for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
	    {
		((Member)e.nextElement()).member_print(ps, "\tpublic ");
		ps.println();
	    }

	    // print an empty constructor

	    if( exc )
	    {
		ps.println("\tpublic " + className + "()");
		ps.println("\t{");
		ps.println("\t\tsuper(" + fullClassName + "Helper.id());");
		ps.println("\t}");
	    }
	    else
	    {
		ps.println("\tpublic " + className + "(){}");
	    }

	    // print a constructor for class member initialization

	    if ( exc ) 
	    {
		// print a constructor for class member initialization with additional first string parameter

		ps.print("\tpublic " + className + "(");
		ps.print("java.lang.String _reason,");
		for ( Enumeration e = memberlist.v.elements(); e.hasMoreElements();) 
		{
		    Member m = (Member)e.nextElement();
		    Declarator d = m.declarator;
		    ps.print( m.type_spec.toString() + " " + d.toString() );
		    if ( e.hasMoreElements() )
			ps.print(", ");
		}
		ps.println(")");

		ps.println("\t{");
		ps.println("\t\tsuper("+fullClassName+"Helper.id()+\"\"+_reason );");
		for ( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
		{
		    Member m = (Member)e.nextElement();
		    Declarator d = m.declarator;
		    ps.print("\t\tthis.");
		    ps.print( d.name() );
		    ps.print(" = ");
		    ps.println( d.name() + ";");
		}
		ps.println("\t}");
	    }
		ps.print("\tpublic " + className + "(");
		for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
		{
		    Member m = (Member)e.nextElement();
		    Declarator d = m.declarator;
		    ps.print( m.type_spec.toString() + " " + d.toString() ); 
		    if( e.hasMoreElements() )
			ps.print(", ");
		}
		ps.println(")");


		ps.println("\t{");
		for( Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
		{
		    Member m = (Member)e.nextElement();
		    Declarator d = m.declarator;
		    ps.print("\t\tthis.");
		    ps.print( d.name() );
		    ps.print(" = ");
		    ps.println( d.name() + ";");
		}
		ps.println("\t}");

	}
	ps.println("}");
    }

    /** generate required classes */

    public void print(PrintWriter ps)
    {
	setPrintPhaseNames();

	/** no code generation for included definitions */
	if( included && !generateIncluded() )
	    return;

	/** only write once */

	if( written )
	    return;

	written = true;

	try
	{
	    String className = className();

	    String path = parser.out_dir + fileSeparator + 
		pack_name.replace('.', fileSeparator );

	    File dir = new File( path );
	    if( !dir.exists() )
		if( !dir.mkdirs())
		{
		    System.err.println("Unable to create " + path );
		    System.exit(1);
		}

	    /** print the mapped java class */

	    String fname = className + ".java";
	    PrintWriter decl_ps = new PrintWriter(new java.io.FileWriter(new File(dir,fname)));
	    printStructClass( className, decl_ps );
	    decl_ps.close();

	    /** print the holder class */

	    fname = className + "Holder.java";
	    decl_ps = new PrintWriter(new java.io.FileWriter(new File(dir,fname)));
	    printHolderClass( className, decl_ps );
	    decl_ps.close();

	    /** print the helper class */

	    fname = className + "Helper.java";
	    decl_ps = new PrintWriter(new java.io.FileWriter(new File(dir,fname)));
	    printHelperClass( className, decl_ps );
	    decl_ps.close();

	    written = true;
	} 
	catch ( java.io.IOException i )
	{
	    System.err.println("File IO error");
	    i.printStackTrace();
	}
    }
}














