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

import java.util.*;
import java.io.*;

class OpDecl 
    extends Declaration
    implements Operation
{
    public int opAttribute; // 0 means normal, 1 means oneway semantics
    public TypeSpec opTypeSpec;
    public Vector paramDecls;
    public RaisesExpr raisesExpr;

    public OpDecl(int num)
    {
	super(num);
	paramDecls = new Vector();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace(s);

	if( pack_name.length() > 0 )
	    pack_name = new String( s + "." + pack_name );
	else
	    pack_name = s;
	opTypeSpec.setPackage(s );

	for( Enumeration e = paramDecls.elements(); 
             e.hasMoreElements();
             ((ParamDecl)e.nextElement()).setPackage(s)
             )
	    ;
	raisesExpr.setPackage(s);
    }

    public void parse()
    {
        //        escapeName();
        if( opAttribute == 1  && !raisesExpr.empty())
            parser.error("Oneway operation "+full_name()+
                         " may not define a raises clases.", token);

	try
	{
	    NameTable.define( full_name(), "operation" );
	} 
	catch ( NameAlreadyDefined nad )
	{
	    parser.error("Operation "+full_name()+" already defined", token);
	}

	for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            param.parse();
            try
            {
                NameTable.define( full_name() + "."+
                                  param.simple_declarator.name(), 
                                  "argument" );
            } 
            catch ( NameAlreadyDefined nad )
            {
                parser.error("Argument "+ param.simple_declarator.name()  + 
                             " already defined in operation " + full_name(), 
                             token);
            }
        }

	if( opTypeSpec.typeSpec() instanceof ScopedName )
	{
	    TypeSpec ts = 
		((ScopedName)opTypeSpec.typeSpec()).resolvedTypeSpec();

	    if( ts != null ) 
		opTypeSpec = ts;
	}

	if( (! NameTable.defined( opTypeSpec.typeName(), "type" )) &&
	    (! NameTable.defined( opTypeSpec.typeName(), "interface" ))) 
	{
	    //parser.error("Not a type: "+opTypeSpec.typeName(), token );
	}

        raisesExpr.parse(); 
    }


    public void print(PrintWriter ps)
    {	
	if( is_pseudo )
	    ps.print("\tpublic abstract " + opTypeSpec.toString());
	else
	    ps.print("\t" + opTypeSpec.toString());
	ps.print(" ");
	ps.print(name);

	ps.print("(");
	Enumeration e = paramDecls.elements();
	if(e.hasMoreElements())
	    ((ParamDecl)e.nextElement()).print(ps);

	for(; e.hasMoreElements();)
	{
	    ps.print(", ");
	    ((ParamDecl)e.nextElement()).print(ps);
	}
	ps.print(")");
	raisesExpr.print(ps);
	ps.println(";");
    }


    public void printMethod( PrintWriter ps, 
                             String classname, 
                             boolean locality_constraint)
    {
	/* in some cases generated name have an underscore prepended for the
	   mapped java name. On the wire, we must use the original name */

	String idl_name = ( name.startsWith("_") ? name.substring(1) : name );

	ps.print("\tpublic " + opTypeSpec.toString() + " " + name + "(");

	Enumeration e = paramDecls.elements();
	if(e.hasMoreElements())
	    ((ParamDecl)e.nextElement()).print(ps);

	for(; e.hasMoreElements();)
	{
	    ps.print(", ");
	    ((ParamDecl)e.nextElement()).print(ps);
	}

	ps.print(")");
	raisesExpr.print(ps);
	ps.println("\n\t{");
	ps.println("\t\twhile(true)");
	ps.println("\t\t{");
	// remote part, not for locality constrained objects
	// 
	if( ! locality_constraint )
	{
	    ps.println("\t\tif(! this._is_local())");
	    ps.println("\t\t{");
	    ps.println("\t\t\torg.omg.CORBA.portable.InputStream _is = null;");
	    ps.println("\t\t\ttry");
	    ps.println("\t\t\t{");
	    ps.print("\t\t\t\torg.omg.CORBA.portable.OutputStream _os = _request( \"" + idl_name + "\",");
	    
	    if( opAttribute == 0 )
		ps.println(" true);");
	    else
		ps.println(" false);");

	    //  arguments..
	    
	    for(e = paramDecls.elements(); e.hasMoreElements();)
	    {
		ParamDecl p = ((ParamDecl)e.nextElement());
		if( p.paramAttribute != 2 ) // i.e. if in or inout
		    ps.println("\t\t\t\t" + p.printWriteStatement("_os") );
	    }
	
	    ps.println("\t\t\t\t_is = _invoke(_os);");
	    
	    if( opAttribute == 0 && 
                !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	    {
		ps.println("\t\t\t\t" + opTypeSpec.toString() + " _result = " + 
			   opTypeSpec.typeSpec().printReadExpression("_is") + ";");
	    }
	    
	    for( Enumeration e2 = paramDecls.elements(); e2.hasMoreElements();) 
	    {
		ParamDecl p = (ParamDecl)e2.nextElement();
		if( p.paramAttribute > 1 ) // out or inout
		{
		    ps.println("\t\t\t\t" + p.simple_declarator + "._read(_is);");
		}
	    }
	    
	    if( opAttribute == 0 && 
                !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	    {
		ps.println("\t\t\t\treturn _result;");
	    }
	    else
		ps.println("\t\t\t\treturn;");

	    /* catch exceptions */

	    ps.println("\t\t\t}");
	    ps.println("\t\t\tcatch( org.omg.CORBA.portable.RemarshalException _rx ){}");
	    ps.println("\t\t\tcatch( org.omg.CORBA.portable.ApplicationException _ax )");
	    ps.println("\t\t\t{");
	    ps.println("\t\t\t\tString _id = _ax.getId();");
	    
	    if( !raisesExpr.empty() )
	    {	
		String [] exceptIds = raisesExpr.getExceptionIds();
		String [] classNames = raisesExpr.getExceptionClassNames();
		ps.print("\t\t\t\t");
		for( int i = 0; i < exceptIds.length; i++)
		{
		    ps.println("if( _id.equals(\""+ exceptIds[i] +"\"))");
		    ps.println("\t\t\t\t{");
		    ps.println("\t\t\t\t\tthrow " + classNames[i] + "Helper.read(_ax.getInputStream());");
		    ps.println("\t\t\t\t}");
		    ps.print("\t\t\t\telse ");
		}
		ps.print("\n\t");
	    }
	    ps.println("\t\t\t\tthrow new RuntimeException(\"Unexpected exception \" + _id );");
	    ps.println("\t\t\t}");
	    ps.println("\t\t\tfinally");
	    ps.println("\t\t\t{");
	    ps.println("\t\t\t\tthis._releaseReply(_is);");
	    ps.println("\t\t\t}");

	    ps.println("\t\t}");
	    // local part
	    ps.println("\t\telse");
	    ps.println("\t\t{");
	}

	ps.println("\t\t\torg.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( \"" + idl_name + "\", _opsClass );");

	ps.println("\t\t\tif( _so == null )");
	ps.println("\t\t\t\tthrow new org.omg.CORBA.UNKNOWN(\"local invocations not supported!\");");

	ps.println("\t\t\t" + classname + "Operations _localServant = (" + 
                   classname + "Operations)_so.servant;");

	if( opAttribute == 0 && 
            !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	{
	    ps.print("\t\t\t" + opTypeSpec.toString() + " _result;");
	}

	ps.println("\t\t\ttry");
	ps.println("\t\t\t{");

	if( opAttribute == 0 && 
            !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	{
	    ps.print("\t\t\t_result = ");
	}
	else
	    ps.print("\t\t\t");

	ps.print("_localServant." + name + "(");

	for(e = paramDecls.elements(); e.hasMoreElements();)
	{
	    ParamDecl p = ((ParamDecl)e.nextElement());
	    ps.print( p.simple_declarator.toString() );
	    if( e.hasMoreElements() )
		ps.print(",");
	}
	ps.println(");");

	ps.println("\t\t\t}");	
	ps.println("\t\t\tfinally");
	ps.println("\t\t\t{");
	ps.println("\t\t\t\t_servant_postinvoke(_so);");
	ps.println("\t\t\t}");

	if( opAttribute == 0 && !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	{
	    ps.println("\t\t\treturn _result;");
	}
	else
	    ps.println("\t\t\treturn;");


	if( ! locality_constraint )	ps.println("\t\t}\n");

	ps.println("\t\t}\n"); // end while
	ps.println("\t}\n"); // end method
    }


    public void printDelegatedMethod(PrintWriter ps)
    {
	ps.print("\tpublic " + opTypeSpec.toString() + " " + name + "(");

	Enumeration e = paramDecls.elements();
	if(e.hasMoreElements())
	    ((ParamDecl)e.nextElement()).print(ps);

	for(; e.hasMoreElements();)
	{
	    ps.print(", ");
	    ((ParamDecl)e.nextElement()).print(ps);
	}

	ps.print(")");
	raisesExpr.print(ps);
	ps.println("\n\t{");


	if( opAttribute == 0 && !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	{
	    ps.print("\t\treturn ");
	}

	ps.print("_delegate." + name  + "(");
	e = paramDecls.elements();
	if(e.hasMoreElements())
	    ps.print( ((ParamDecl)e.nextElement()).simple_declarator );

	for(; e.hasMoreElements();)
	{
	    ps.print(",");
	    ps.print( ((ParamDecl)e.nextElement()).simple_declarator );
	}
	ps.println(");");
	ps.println("\t}\n");
    }

    public void printInvocation(PrintWriter ps)
    {
	if( !raisesExpr.empty() )
	{	
	    ps.println("\t\t\ttry");
	    ps.println("\t\t\t{");
	}

	/* read args */

	int argc = 0;
	boolean holders = false;

	for( Enumeration e = paramDecls.elements(); e.hasMoreElements();)
	{
	    ParamDecl p = (ParamDecl)e.nextElement();
	    TypeSpec ts = p.paramTypeSpec;
	    if( p.paramAttribute == 1 ) // in params
	    {
		ps.println("\t\t\t\t" + ts.toString() + " _arg" + (argc++) + "=" + 
                           ts.printReadExpression("_input") + ";");
	    }
	    else
	    {
		holders = true;
		ps.println("\t\t\t\t" + ts.holderName() + " _arg" + (argc++) + "= new " + 
                           ts.holderName() + "();");
		if( p.paramAttribute == 3 ) // inout
		{
		    ps.println("\t\t\t\t_arg" + (argc-1) + "._read(_input);");
		}
	    }
	}


	boolean complex = ( opTypeSpec.typeSpec() instanceof ArrayTypeSpec ) ||
	    (opTypeSpec.typeSpec() instanceof FixedPointType );

	String write_str = null;
	//	if( (!(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))    || holders )
	//	{
	    ps.println("\t\t\t\t_out = handler.createReply();");
	    if( !(opTypeSpec.typeSpec() instanceof VoidTypeSpec ) && !complex )
	    {
		write_str = opTypeSpec.typeSpec().printWriteStatement("**","_out");
		ps.print("\t\t\t\t" + write_str.substring(0,write_str.indexOf("**"))  );
	    }
	    else
		ps.print("\t\t\t\t");	  
	    //	}
 

	if(complex)
	    ps.print(opTypeSpec.typeSpec().typeName() + " _result = ");

	ps.print(name + "(");

	for( int i = 0; i < argc; i ++ )
	{
	    ps.print("_arg" + i );
	    if( i < argc-1 )
		ps.print(",");
	}

	/*

	  Enumeration e = paramDecls.elements();
	  if(e.hasMoreElements())
	  {
	  TypeSpec ts = ((ParamDecl)e.nextElement()).paramTypeSpec;
	  ps.print(ts.printReadExpression("input"));
	  }

	  for(; e.hasMoreElements();)
	  {
	  TypeSpec ts = ((ParamDecl)e.nextElement()).paramTypeSpec;
	  ps.print("," + ts.printReadExpression("input"));
	  }
	*/

	if(!(opTypeSpec.typeSpec() instanceof VoidTypeSpec ))
	    ps.print(")");

	if( !complex )
	    ps.println(");");
	else
	{
	    ps.println(";");	    
	    ps.println( opTypeSpec.typeSpec().printWriteStatement("_result","_out"));
	}

	/* write holder values */

	argc = 0;
	for( Enumeration e = paramDecls.elements(); e.hasMoreElements();)
	{
	    ParamDecl p = (ParamDecl)e.nextElement();
	    TypeSpec ts = p.paramTypeSpec;
	    if( p.paramAttribute > 1 ) // out or inout
	    {
		ps.println("\t\t\t\t_arg" + (argc) + "._write(_out);");
	    }
	    argc++;
	}

	if( !raisesExpr.empty() )
	{	
	    ps.println("\t\t\t}");
	    String [] excepts = raisesExpr.getExceptionNames();
	    String [] classNames = raisesExpr.getExceptionClassNames();
	    for( int i = 0; i < excepts.length; i++)
	    {
		ps.println("\t\t\tcatch(" + excepts[i] + " _ex" + i + ")");
		ps.println("\t\t\t{");
		ps.println("\t\t\t\t_out = handler.createExceptionReply();");
		ps.println("\t\t\t\t" + classNames[i] + "Helper.write(_out, _ex" + i + ");");
		ps.println("\t\t\t}");
	    }
	}

    }

    public String signature()
    {
	StringBuffer sb = new StringBuffer();
	sb.append(name+"(");

	Enumeration e = paramDecls.elements();
	if(e.hasMoreElements())
	    sb.append( ((ParamDecl)e.nextElement()).paramTypeSpec.toString() );

	for(; e.hasMoreElements();)
	{
	    sb.append( ","+((ParamDecl)e.nextElement()).paramTypeSpec.toString() );
	}
	sb.append(")");
	return sb.toString();
    }


    public String name()
    {
	return name;
    }


    public String opName()
    {
	return name();
    }

    public void printSignature(PrintWriter ps)
    {
	ps.print("\tpublic " + opTypeSpec.toString() + " " + name + "(");

	Enumeration e = paramDecls.elements();
	if(e.hasMoreElements())
	    ((ParamDecl)e.nextElement()).print(ps);

	for(; e.hasMoreElements();)
	{
	    ps.print(", ");
	    ((ParamDecl)e.nextElement()).print(ps);
	}

	ps.print(")");
	raisesExpr.print(ps);
	ps.println(";");
    }


    /** collect Interface Repository information in the argument hashtable */
    public void getIRInfo(Hashtable irInfoTable )
    {
        StringBuffer sb = new StringBuffer();
        boolean enter = false;

        TypeSpec ts = opTypeSpec.typeSpec();

        if( ts instanceof AliasTypeSpec )
        {
 //             if( ((AliasTypeSpec)ts).originalType.typeSpec() instanceof FixedPointType )
//              {
                sb.append( ts.full_name() );
                enter = true;     
 //             }
        }
        sb.append("(");

	for(Enumeration e = paramDecls.elements(); e.hasMoreElements();)
	{
            ParamDecl param = (ParamDecl)e.nextElement();
            if( param.paramAttribute == 3 )
            {
                sb.append("inout ");
                enter = true;
            }
            else if( param.paramAttribute == 2 )
            {
                sb.append("out ");
                enter = true;
            }
            else
                sb.append("in ");

            ts = param.paramTypeSpec.typeSpec();

            if( ts instanceof AliasTypeSpec )
            {
 //                 if( ((AliasTypeSpec)ts).originalType.typeSpec() instanceof FixedPointType )
//                  {
                sb.append( ts.full_name() );
                enter = true;     
					//                }
            }            

            sb.append(",");
	}

        if( paramDecls.size() > 0)
        {
            // remove extra trailing ","
            //sb.deleteCharAt( sb.length()-1);
            // ugly workaround for non exisitng delete in jdk1.1
            sb = new StringBuffer( sb.toString().substring( 0, sb.length() - 1 ));
        }
        sb.append(")");

        if( opAttribute == 1)
            sb.append("-oneway");

        if( enter )
            irInfoTable.put( name, sb.toString());
    }


}




