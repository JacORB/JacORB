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

import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class FixedPointType 
    extends TemplateTypeSpec  
{
    public ConstExpr digit_expr = null;
    public ConstExpr scale_expr = null;
    private int digits = 0;
    private int scale = 0;

    public FixedPointType(int num)
    {
	super(num);
    }

    public Object clone()
    {
	FixedPointType ft = new FixedPointType(new_num());
	ft.name = this.name;
	ft.pack_name = this.pack_name;
	ft.digit_expr = this.digit_expr;
	ft.digits = this.digits;
	ft.scale_expr = this.scale_expr;
	ft.scale = this.scale;
	ft.included = this.included;
	ft.typedefd = this.typedefd;
	ft.set_token( this.get_token());
	ft.setEnclosingSymbol( this.getEnclosingSymbol());
	return ft;
    }

    private String helperName()
    {
	if( pack_name.length() > 0 )
	{
	    String s = ScopedName.unPseudoName( pack_name+"." + name );
	    if(! s.startsWith("org.omg") )
	    {	
		return omg_package_prefix + s;
	    }
	    else
		return s;       
	}
	else
	    return ScopedName.unPseudoName( name ) ;
    }

    public String typeName()
    {
	return "java.math.BigDecimal";
    }

    public TypeSpec typeSpec()
    {
	return this;
    }


    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	enclosing_symbol = s;
    }


    private void printHelperClass(String className, PrintWriter ps)
    {
	if( !pack_name.equals(""))
	    ps.println("package " + pack_name + ";" );

	ps.println("public class " + className + "Helper");
	ps.println("{");
	ps.println("\tprivate static org.omg.CORBA.TypeCode _type = " + getTypeCodeExpression() + ";");

	ps.println("\tpublic " + className + "Helper ()");
	ps.println("\t{");
	ps.println("\t}");

	ps.println("\tpublic static void insert(org.omg.CORBA.Any any, java.math.BigDecimal s)");
	ps.println("\t{");
	ps.println("\t\tany.insert_fixed(s, type());");
	ps.println("\t}");

	ps.println("\tpublic static java.math.BigDecimal extract(org.omg.CORBA.Any any)");
	ps.println("\t{");
	ps.println("\t\treturn any.extract_fixed();");
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

	printIdMethod(ps); // from IdlSymbol

	/** read */

	ps.println("\tpublic static java.math.BigDecimal read(org.omg.CORBA.portable.InputStream in)");
	ps.println("\t{");
	ps.println("\t\tjava.math.BigDecimal result = in.read_fixed();");
	ps.println("\t\treturn result.movePointLeft(" + scale + ");");
	ps.println("\t}");

	/** write */

	ps.println("\tpublic static void write(org.omg.CORBA.portable.OutputStream out, java.math.BigDecimal s)");
	ps.println("\t{");

	StringBuffer mb = new StringBuffer("1");
	for( int m = 0; m < digits-scale; m++ )
	    mb.append("0");

	ps.println("\t\tif( s.scale() != " + scale + ")");
	ps.println("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION();");	

	ps.println("\t\tjava.math.BigDecimal max = new java.math.BigDecimal(\"" + mb.toString() +"\");");
	ps.println("\t\tif( s.compareTo(max) != -1)");
	ps.println("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION();");
	ps.println("\t\tout.write_fixed(s);");
	ps.println("\t}");

	ps.println("}");
    }

    public void print(java.io.PrintWriter pw)
    {
	try 
	{
	    // write helper file
		
	    String fullName = helperName();
	    String className;
	    if( fullName.indexOf('.') > 0 )
	    {
		pack_name = fullName.substring( 0, fullName.lastIndexOf('.'));
		className = fullName.substring( fullName.lastIndexOf('.') + 1 );
	    } 
	    else 
	    {
		pack_name = "";
		className  = fullName;
	    }
	    
	    String path = parser.out_dir+fileSeparator+pack_name.replace('.',fileSeparator);
	    File dir = new File( path );
	    if( !dir.exists() )
	    {
		if( !dir.mkdirs())
		{
		    System.err.println("Unable to create " + path );
		    System.exit(1);
		}
	    }       
	    
	    String fname = className + "Helper.java";
	    PrintWriter ps = new PrintWriter(new java.io.FileWriter(new File(dir,fname)));
	    printHelperClass( className, ps );
	    ps.close();
	    
	}
	catch ( java.io.IOException i )
	{
	    System.err.println("File IO error");
	    i.printStackTrace();
	}
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	if( pack_name.length() > 0 )
	    pack_name = new String( s + "." + pack_name );
	else
	    pack_name = s;
    }

    public String toString()
    {
	return typeName();
    }

    public String holderName()
    {
	return "org.omg.CORBA.FixedHolder";
    }

    public String getTypeCodeExpression()
    {
	return "org.omg.CORBA.ORB.init().create_fixed_tc((short)" + digits + ",(short)" + scale+")";
    }


    public String printReadExpression(String strname)
    {
	return helperName() + "Helper.read(" + strname + ")";
    }

    public String printReadStatement(String var_name, String strname)
    {
	String fixedName = null;
	if( hashCode() > 0 )	    
	    fixedName = "_fixed" + hashCode();
	else
	    fixedName= "_fixed" + (-1 * hashCode());

	StringBuffer sb = new StringBuffer();
	
	sb.append("\t\tjava.math.BigDecimal " + fixedName +"="+strname+".read_fixed();\n");
	sb.append( "\t\t"+var_name + " = " + fixedName + ".movePointLeft(" + scale + ");\n");
	return sb.toString();
    }

    public String printWriteStatement(String var_name, String strname)
    {
	StringBuffer mb = new StringBuffer("1");
	for( int m = 0; m < digits-scale; m++ )
	    mb.append("0");

	StringBuffer sb = new StringBuffer();
	sb.append("\n\t\tif(" + var_name + ".scale() != " + scale + ")\n");
	sb.append("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION(\"wrong scale in fixed point value, expecting " + scale + ", got \" + " + var_name + ".scale() );\n");
	
	String max = null;
	if( hashCode() > 0 )	    
	    max = "_max" + hashCode();
	else
	    max = "_max" + ( -1 * hashCode());
	
	sb.append("\t\tjava.math.BigDecimal " + max +"= new java.math.BigDecimal(\"" + mb.toString() +"\");\n");
	sb.append("\t\tif( "+ var_name+ ".compareTo(" + max + " ) != -1)\n");
	sb.append("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION(\"more than " + digits + " digits in fixed point value\");\n");
	sb.append("\t\t" +strname + ".write_fixed(" +var_name + ");\n");
	
	return sb.toString();
    }

    public void parse() 	 
    {
	digits = digit_expr.pos_int_const();
	scale = scale_expr.pos_int_const();
    }
}















