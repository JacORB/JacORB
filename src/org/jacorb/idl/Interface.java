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
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

import java.util.*;
import java.io.*;

class Interface 
    extends TypeDeclaration
    implements  Scope
{ 
    public InterfaceBody body = null;
    public SymbolList inheritanceSpec = null;
    private String [] ids = null;
    private boolean locality_constraint = false;
    private boolean is_abstract = false;
    private ScopeData scopeData;

    /* IR information that would otherwise be lost */
    private Hashtable irInfoTable = new Hashtable();

    public Interface(int num)
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

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        if( body != null ) // could've been a forward declaration)
            body.setPackage( s); // a new scope!

        if( inheritanceSpec != null )
            inheritanceSpec.setPackage(s);
    }

    public void set_abstract()
    {
        is_abstract = true;
    }


    /* override methods from superclass TyepDeclaration */

    public TypeDeclaration declaration()
    {
        return this;
    };

    public String typeName()
    {
        return full_name();
    }

    public Object clone()
    {
        throw new RuntimeException("Don't clone me, i am an interface!");
        // return null;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }


    public void set_locality( boolean locality_constraint )
    {
        this.locality_constraint = locality_constraint;
    }


    /**
     * @returns a string for an expression of type TypeCode 
     *                  that describes this type
     */

    public String getTypeCodeExpression()
    {
        return "org.omg.CORBA.ORB.init().create_interface_tc( \"" + 
            id() + "\", \"" + name + "\")";
    }

    public boolean basic()
    {
        return true;
    } 


    public String holderName()
    {
        return toString() + "Holder";
        //      return typeName() + "Holder";
    }

    public String toString()
    {
        String n = typeName();
        if( ! n.startsWith( "org.omg"))
        {
            return omgPrefix() + n;
        } 
        else
            return n;
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public String printReadExpression(String Streamname)
    {
        return javaName() + "Helper.read(" + Streamname +")" ;
    }

    public String printWriteStatement(String var_name, String Streamname)
    {
        return javaName() + "Helper.write(" + Streamname +"," + var_name +");" ;
    }

    public void parse()          
    {
        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
        try
        {
            ScopedName.definePseudoScope( full_name());
            ctspec.c_type_spec = this;
            if( is_pseudo )
                NameTable.define( full_name(), "pseudo interface" );
            else
                NameTable.define( full_name(), "interface" );

            TypeMap.typedef( full_name(), ctspec );
        } 
        catch ( IllegalRedefinition ill )
        {
            parser.fatal_error("Illegal Redefinition of  " + 
                               ill.oldDef + " in nested scope as " + ill.newDef, token);
        }
        catch ( NameAlreadyDefined nad )
        {
            // if we get here, there is already a type spec for this interface 
            // in the global type table for a forward declaration of this
            // interface. We must replace that table entry with this type spec
            // if this is not yet another forwad declaration

            if( body != null )
            {
                justAnotherOne = true;
            }

            if( !full_name().equals("org.omg.CORBA.TypeCode") && body != null)
            {
                TypeMap.replaceForwardDeclaration( full_name(), ctspec );
            }
        }

        if( body != null ) 
        {
            if( inheritanceSpec != null && inheritanceSpec.v.size() > 0)
            {
                for( Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements(); )
                {
                    try
                    {
                        ScopedName name = (ScopedName)e.nextElement();
                        ConstrTypeSpec ts = (ConstrTypeSpec)name.resolvedTypeSpec();
                        if( ts.declaration() instanceof Interface )
                        {
                            continue;
                        }
                    }
                    catch( Exception ex )
                    {
                        // ex.printStackTrace();
                    }                        
                    parser.fatal_error("Illegal inheritance spec: " + 
                                       inheritanceSpec, token );
                }
                body.set_ancestors(inheritanceSpec);
            }
            body.parse();
            NameTable.parsed_interfaces.put( full_name(),"");
        } 
        else if( !justAnotherOne )
        { 
            // i am forward declared, must set myself as 
            // pending further parsing
            parser.set_pending(full_name());
        }
    }


    InterfaceBody getBody()
    {
        if( parser.get_pending( full_name()) != null )
        {
            parser.fatal_error( full_name() + " is forward declared and still pending!", token );
        }
        else if( body == null )
        {
            if( ((Interface)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec) != this )
                body = ((Interface)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec).getBody();
            if( body == null )
                parser.fatal_error( full_name() + " still has an empty body!", token);
        }
        return body;
    }

    private void printClassComment(String className, PrintWriter ps)
    {
	ps.println("/**");
	ps.println(" *\tGenerated from IDL definition of interface " + 
                    "\"" + className + "\"" );
        ps.println(" *\t@author JacORB IDL compiler ");
        ps.println(" */\n");
    }

    /**
     *  generate the signature interface 
     */

    private void printInterface(String classname, PrintWriter ps)
    {
        // are we in the unnamed package?

        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printClassComment( classname, ps );

        // do we inherit from a class in the unnamed package?
        // if so, we have to import this class explicitly 

        if( inheritanceSpec.v.size() > 0 )
        {
            Enumeration e = inheritanceSpec.v.elements();
            for(; e.hasMoreElements();)
            {
                ScopedName sn = (ScopedName)e.nextElement();
                if( sn.resolvedName().indexOf('.') < 0 )
                {
                    ps.print("import " + sn + ";" );
                }
            }
        }
        printImport(ps);

        if( is_pseudo ) 
        {
            ps.println("public abstract class " + classname );

            if( inheritanceSpec.v.size() > 0 )
            {
                StringBuffer pseudo_bases = new StringBuffer();
                StringBuffer regular_bases = new StringBuffer();
                String comma = " ";

                for( Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements(); )
                {
                    ScopedName sn = ((ScopedName)e.nextElement());
                    String  name = sn.resolvedName();
                    if( sn.is_pseudo() )
                    {
                        pseudo_bases.append(comma + name);
                    }
                    else
                    {
                        regular_bases.append(comma + name);
                    }
                    if( inheritanceSpec.v.size() > 1 )
                        comma = ",";
                }
                if( pseudo_bases.length() > 0 )
                    ps.println("\textends " + pseudo_bases.toString());

                if( regular_bases.length() > 0 )
                    ps.println("\timplements " + regular_bases.toString());

            }
        }
        else
        { 
            ps.println("public interface " + classname );
            ps.print("\textends " + classname + 
                     "Operations, org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity");
            if( inheritanceSpec.v.size() > 0 )
            {
                Enumeration e = inheritanceSpec.v.elements();
                for(; e.hasMoreElements();)
                {
                    ps.print(", " + (ScopedName)e.nextElement());
                }
            }
        }
        ps.println("\n{");
        if( body != null )
        { 
            // forward declaration
            body.printConstants(ps);
            body.printInterfaceMethods(ps);
        }
        ps.println("}");
    }

    private String indentString(int nesting_level )
    {
        StringBuffer sb = new StringBuffer();
        for( int i =0 ; i < nesting_level; i++ )
            sb.append("   ");
        return sb.toString();
    }


    /**
     * generate the operations Java interface (not for pseudo interfaces)
     */

    private void printOperations( String classname, PrintWriter ps )
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printClassComment( classname, ps );

        // do we inherit from a class in the unnamed package?
        // if so, we have to import this class explicitly 

        if( inheritanceSpec.v.size() > 0 )
        {
            Enumeration e = inheritanceSpec.v.elements();
            for(; e.hasMoreElements();)
            {
                ScopedName sn = (ScopedName)e.nextElement();
                if( sn.resolvedName().indexOf('.') < 0 )
                {
                    ps.print("import " + sn + ";");
                }
            }
        }
        printImport(ps);

        ps.println("public interface " + classname + "Operations");
        if( inheritanceSpec.v.size() > 0 )
        {
            ps.print("\textends ");
            Enumeration e = inheritanceSpec.v.elements();
            ps.print( (ScopedName)e.nextElement() + "Operations");
            for(; e.hasMoreElements();)
            {
                ps.print(", " +  (ScopedName)e.nextElement() + "Operations");
            }
            ps.print("\n");
        }

        ps.println("{");
        if( body != null )
        { 
            // forward declaration
            body.printOperationSignatures(ps);
        }
        ps.println("}");
    }




    private void printHolder( String classname, PrintWriter ps )
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printClassComment( classname, ps );

        ps.print("public class " + classname + "Holder");
        ps.print("\timplements org.omg.CORBA.portable.Streamable");

        ps.println("{");
        ps.println("\t public " + classname + " value;");

        ps.println("\tpublic " + classname +"Holder()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + classname +"Holder("+classname+" initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type()");
        ps.println("\t{");
        ps.println("\t\treturn " + classname + "Helper.type();");
        ps.println("\t}");

        ps.println("\tpublic void _read(org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + classname + "Helper.read(in);");
        ps.println("\t}");

        ps.println("\tpublic void _write(org.omg.CORBA.portable.OutputStream _out)");
        ps.println("\t{");
        ps.println("\t\t" + classname + "Helper.write(_out,value);");
        ps.println("\t}");

        ps.println("}");
    }

    private void printHelper( String className, PrintWriter ps )
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";" );

        printImport(ps);

        printClassComment( className, ps );

        ps.println("public class " + className + "Helper");
        ps.println("{");

        ps.println("\tpublic " + className + "Helper()");
        ps.println("\t{");
        ps.println("\t}");


        ps.println("\tpublic static void insert(org.omg.CORBA.Any any, " + typeName() + " s)");
        ps.println("\t{");
        ps.println("\t\tany.insert_Object(s);");
        ps.println("\t}");

        ps.println("\tpublic static " + typeName() + " extract(org.omg.CORBA.Any any)");
        ps.println("\t{");
        ps.println("\t\treturn narrow(any.extract_Object());");
        ps.println("\t}");

        ps.println("\tpublic static org.omg.CORBA.TypeCode type()");
        ps.println("\t{");


        ps.println("\t\treturn " + getTypeCodeExpression() + ";");
        ps.println("\t}");

        printIdMethod(ps);

        ps.println("\tpublic static " + className + " read(org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\treturn narrow( in.read_Object());");
        ps.println("\t}");

        ps.println("\tpublic static void write(org.omg.CORBA.portable.OutputStream _out, " + typeName() + " s)");
        ps.println("\t{");
        ps.println("\t\t_out.write_Object(s);");
        ps.println("\t}");

        ps.println("\tpublic static " + typeName() + " narrow(org.omg.CORBA.Object obj)");
        ps.println("\t{");
        ps.println("\t\tif( obj == null )");
        ps.println("\t\t\treturn null;");

        if( parser.generate_stubs  )
        {
            ps.println("\t\ttry");
            ps.println("\t\t{");
            ps.println("\t\t\treturn (" + typeName() + ")obj;");
            ps.println("\t\t}");
            ps.println("\t\tcatch( ClassCastException c )");
            ps.println("\t\t{");
            ps.println("\t\t\tif( obj._is_a(\"" + id() + "\"))");
            ps.println("\t\t\t{");
        
            String stub_name = typeName();
            if( stub_name.indexOf('.') > -1 )
            {
                stub_name = stub_name.substring(0,typeName().lastIndexOf('.')) + 
                    "._" + stub_name.substring(stub_name.lastIndexOf('.')+1) + "Stub";
            }
            else
                stub_name = "_" + stub_name + "Stub";
            ps.println("\t\t\t\t" + stub_name + " stub;");
          
            ps.println("\t\t\t\tstub = new " + stub_name + "();");
            ps.println("\t\t\t\tstub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
            ps.println("\t\t\t\treturn stub;");
            ps.println("\t\t\t}");
            ps.println("\t\t}");
            ps.println("\t\tthrow new org.omg.CORBA.BAD_PARAM(\"Narrow failed\");");
        }
        else
        {
            ps.println("\t\tif( obj instanceof " + typeName() + " )");
            ps.println("\t\t\treturn (" + typeName() + ")obj;");
            ps.println("\t\telse");
            ps.println("\t\tthrow new org.omg.CORBA.BAD_PARAM(\"Narrow failed, not a " + typeName()+ "\");");
        }
        ps.println("\t}");
        ps.println("}");
    }

    private String [] get_ids()
    {
        if( ids == null )
        {
            Hashtable table = new Hashtable();
            if( inheritanceSpec != null && inheritanceSpec.v.size() > 0 )
            {
                for( Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                {
                    ScopedName sn = ((ScopedName)e.nextElement());
                    Interface base = null;
                    try
                    {
                        base = (Interface)((ConstrTypeSpec)sn.resolvedTypeSpec()).c_type_spec;
                    }
                    catch( Exception ex)
                    {
                        ex.printStackTrace();
                        parser.fatal_error("Cannot find base interface " + sn, token );
                    }
                    String [] base_ids = base.get_ids();
                    for( int j = 0; j < base_ids.length; j++ )  
                    {
                        if( !table.contains(base_ids[j] ) )
                        {
                            table.put( base_ids[j], "");
                        }
                    }                                        
                }
            }

            if( table.size() == 0 ) 
            {
                table.put("IDL:omg.org/CORBA/Object:1.0", "");
            }

            Enumeration o = table.keys() ;
            ids = new String[ table.size() + 1];

            ids[0] = id();

            for( int i = 1; i < ids.length; i++ )
            {
                ids[i] = (String)o.nextElement();
            }
        }
        return ids;
    }


    /**
     * generates a stub class for this Interface
     */

    private void printStub( String classname, PrintWriter ps )
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printImport(ps);

        printClassComment( classname, ps );

        ps.println("public class _" + classname + "Stub");
        ps.println("\textends org.omg.CORBA.portable.ObjectImpl");

        ps.println("\timplements " + javaName());
        ps.println("{");

        ps.print("\tprivate String[] ids = {");
        String [] ids = get_ids();
        for( int i = 0; i < ids.length-1; i++ )
            ps.print( "\"" + ids[i] + "\"," );
        ps.println( "\"" + ids[ids.length-1] + "\"};");
        
        ps.println("\tpublic String[] _ids()");
        ps.println("\t{");
        ps.println("\t\treturn ids;");
        ps.println("\t}\n");

        ps.print("\tpublic final static java.lang.Class _opsClass = " );
        if( !pack_name.equals("")) ps.print(pack_name + ".");
        ps.println( classname + "Operations.class;");

        body.printStubMethods(ps, classname, locality_constraint);

        ps.println("}");
    }

    private void printImplSkeleton( String classname, PrintWriter ps)
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printClassComment( classname, ps );

        printImport(ps);

        ps.print("public abstract class " + classname + "POA");
        ps.println("\n\textends org.omg.PortableServer.Servant");
        ps.println("\timplements org.omg.CORBA.portable.InvokeHandler, " + javaName() + "Operations");
        ps.println("{");

        body.printOperationsHash(ps);

        ps.print("\tprivate String[] ids = {");
        String [] ids = get_ids();
        for( int i = 0; i < ids.length-1; i++ )
            ps.print( "\"" + ids[i] + "\"," );
        ps.println( "\"" + ids[ids.length-1] + "\"};");


        ps.println("\tpublic " + javaName() + " _this()");
        ps.println("\t{");
        ps.println("\t\treturn " + javaName() + "Helper.narrow(_this_object());");
        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this(org.omg.CORBA.ORB orb)");
        ps.println("\t{");
        ps.println("\t\treturn " + javaName() + "Helper.narrow(_this_object(orb));");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)");
        ps.println("\t\tthrows org.omg.CORBA.SystemException");
        ps.println("\t{");
        ps.println("\t\torg.omg.CORBA.portable.OutputStream _out = null;");
        ps.println("\t\t// do something");

        body.printSkelInvocations(ps);

        ps.println("\t}\n");

        ps.println("\tpublic String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)");
        ps.println("\t{");
        ps.println("\t\treturn ids;");
        ps.println("\t}");
        ps.println("}");
    }

    /**
     * print the stream-based skeleton class
     */

    private void printTieSkeleton( String classname, PrintWriter ps)
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        ps.println("import org.omg.PortableServer.POA;");
        printImport(ps);

        printClassComment( classname, ps );

        ps.println("public class " + classname + "POATie");
        ps.println("\textends " + classname + "POA");
        ps.println("{");

        ps.println("\tprivate " + classname + "Operations _delegate;\n");
        ps.println("\tprivate POA _poa;");

        ps.println("\tpublic " + classname + "POATie(" + classname + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        ps.println("\tpublic " + classname + "POATie(" + classname + "Operations delegate, POA poa)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t\t_poa = poa;");
        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this()");
        ps.println("\t{");
        ps.println("\t\treturn " + javaName() + "Helper.narrow(_this_object());");
        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this(org.omg.CORBA.ORB orb)");
        ps.println("\t{");
        ps.println("\t\treturn " + javaName() + "Helper.narrow(_this_object(orb));");
        ps.println("\t}");

        ps.println("\tpublic " + classname + "Operations _delegate()");
        ps.println("\t{");
        ps.println("\t\treturn _delegate;");
        ps.println("\t}");

        ps.println("\tpublic void _delegate(" + classname + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        //      ps.println("\tpublic POA _default_POA()");
        //      ps.println("\t{");
        //      ps.println("\t\tif( _poa != null )");
        //      ps.println("\t\t{");
        //      ps.println("\t\t\treturn _poa;");
        //      ps.println("\t\t}");
        //      ps.println("\t\telse");
        //      ps.println("\t\t{");
        //      ps.println("\t\t\treturn super._default_POA();");
        //      ps.println("\t\t}");
        //      ps.println("\t}");

        body.printDelegatedMethods( ps);
        ps.println("}");
    }

    private void printIRHelper( String className, PrintWriter ps )
    {
        if( !pack_name.equals(""))
            ps.println("package " + pack_name + ";" );

        ps.println("\n/**");
        ps.println(" * This class contains generated Interface Repository information.");
        ps.println(" * @author JacORB IDL compiler.");
        ps.println(" */");

        ps.println("\npublic class " + className + "IRHelper");
        ps.println("{");

        ps.println("\tpublic static java.util.Hashtable irInfo = new java.util.Hashtable();");
        ps.println("\tstatic");       
        ps.println("\t{");
        body.getIRInfo( irInfoTable );
        for( Enumeration e = irInfoTable.keys(); e.hasMoreElements(); )
        {
            String key = (String)e.nextElement();
            ps.println("\t\tirInfo.put(\"" + key + "\", \"" + (String)irInfoTable.get(key) + "\");");
        }
        ps.println("\t}");
        ps.println("}");
    }



    public void print(PrintWriter _ps)
    {
        if( included && !generateIncluded() )      
            return;

        // divert output into class files 
        if( body != null ) // forward declaration
        {
            try
            {
                // Java Interface file

                String path = 
                    parser.out_dir + fileSeparator + pack_name.replace('.', fileSeparator );
                File dir = new File( path );
                if( !dir.exists() )
                {
                    if( !dir.mkdirs())
                    {
                        org.jacorb.idl.parser.fatal_error( "Unable to create " + path, null );
                    }
                }
                
                PrintWriter ps =
                    new PrintWriter(new java.io.FileWriter(new File(dir,name + ".java")));
                printInterface( name, ps );
                ps.close();

                if( !is_pseudo)
                {

                    ps = new PrintWriter(new java.io.FileWriter(new File(dir,name + 
                                                                         "Operations.java")));
                    // are we in the unnamed package?
                    printOperations( name , ps );
                    ps.close();
                    
                    // Helper

                    ps = new PrintWriter(new java.io.FileWriter(new File(dir,name + 
                                                                         "Helper.java")));
                    printHelper( name, ps);
                    ps.close();

                    // Holder file

                    ps = new PrintWriter(new java.io.FileWriter(new File(dir,name + "Holder.java")));
                    printHolder( name, ps);
                    ps.close();

                    if ( parser.generate_stubs )
                    {
                        // Stub
                        ps = new PrintWriter(new java.io.FileWriter(new File(dir,"_" +
                                                                             name + "Stub.java")));
                        printStub( name , ps );
                        ps.close();
                    }

                    if ( parser.generate_skeletons )
                    {
                        // Skeletons

                        ps = new PrintWriter( new java.io.FileWriter(new File(dir, 
                                                                              name + 
                                                                              "POA.java")));
                        printImplSkeleton ( name , ps );
                        ps.close();
                        
                        ps = new PrintWriter( new java.io.FileWriter(new File(dir, name + 
                                                                              "POATie.java")));
                        printTieSkeleton ( name , ps );
                        ps.close();
                    }

                    if( parser.generateIR )
                    {
                        ps = new PrintWriter( new java.io.FileWriter(new File(dir, name + 
                                                                              "IRHelper.java")));
                        printIRHelper( name , ps );
                        ps.close();

                    }
                }

                /* print class files for interface local definitions */

                body.print( null );

                //IRMap.enter(this);
                
            } 
            catch ( java.io.IOException i )
            {
                System.err.println("File IO error");
                i.printStackTrace();
            }
        }
    }
}




