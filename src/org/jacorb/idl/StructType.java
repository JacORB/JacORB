package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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


import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class StructType
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
        StructType st = new StructType(new_num());
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
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
    }

    /**
     * get this types's mapped Java name
     */

    public String getJavaTypeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
    }


    /**
     * get this symbol's IDL type name
     */

    public String getIDLTypeName()
    {
        return getJavaTypeName(); // TODO
    }


    public boolean basic()
    {
        return false;
    }

    public void set_memberlist(MemberList m)
    {
        m.setContainingType(this);
        memberlist = m;
        memberlist.setPackage(name);
        if (memberlist != null)
            memberlist.setEnclosingSymbol(this);
    }

    public void set_included(boolean i)
    {
        included = i;
    }


    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        if (memberlist != null)
            memberlist.setPackage(s);
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
        {
            System.err.println("was " + enclosing_symbol.getClass().getName() +
                                " now: " + s.getClass().getName());
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        }
        enclosing_symbol = s;
        if (memberlist != null)
            memberlist.setEnclosingSymbol(this);
    }

    public String toString()
    {
        return typeName();
    }

    public void parse()
    {
        boolean justAnotherOne = false;

        if (parsed)
        {
            // there are occasions where the compiler may try to parse
            // a struct type spec for a second time, viz if the struct is
            // referred to through a scoped name in another struct member.
            // that's not a problem, but we have to skip parsing again!
            // (Gerald: introduced together with the fix for bug #84).
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("Parsing Struct " + name);

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            // important: typeName must be set _before_ a new scope is introduced,
            // otherwise the typeName for this struct class will be the same
            // as the package name for the new pseudo scope!

            ScopedName.definePseudoScope(full_name());

            ctspec.c_type_spec = this;

            NameTable.define(full_name(), "type-struct");
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            if (exc)
            {
                parser.error("Struct " + getJavaTypeName() + " already defined", token);
            }
            else
            {
                if (parser.get_pending (full_name ()) != null)
                {
                    if (memberlist != null)
                    {
                        justAnotherOne = true;
                    }

                    if (!full_name().equals("org.omg.CORBA.TypeCode") && memberlist != null)
                    {
                        TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                    }
                }
                else
                {
                    parser.error("Struct " + getJavaTypeName() + " already defined", token);
                }
            }
        }
        if (memberlist != null)
        {
            ScopedName.addRecursionScope(getJavaTypeName());
            memberlist.parse();
            ScopedName.removeRecursionScope(getJavaTypeName());

            if (exc == false)
            {
                NameTable.parsed_interfaces.put(full_name(), "");
                parser.remove_pending(full_name());
            }
        }
        else if (!justAnotherOne && exc == false)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name());
        }

        parsed = true;
    }

    public String className()
    {
        String fullName = getJavaTypeName();
        if (fullName.indexOf('.') > 0)
        {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        else
        {
            return fullName;
        }
    }

    public String printReadExpression(String Streamname)
    {
        return toString() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return toString() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return getJavaTypeName() + "Holder";
    }

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     */

    public String getTypeCodeExpression()
    {
        return full_name() + "Helper.type()";
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }
        else
        {
            return this.getTypeCodeExpression();
        }
    }


    private void printClassComment(String className, PrintWriter ps)
    {
        ps.println("/**");
        ps.println(" *\tGenerated from IDL definition of " +
                (exc ? "exception " : "struct ") + "\"" +
                className + "\"");
        ps.println(" *\t@author JacORB IDL compiler ");
        ps.println(" */\n");
    }

    private void printHolderClass(String className, PrintWriter ps)
    {
        if (Environment.JAVA14 && pack_name.equals(""))
            lexer.emit_warn
                ("No package defined for " + className + " - illegal in JDK1.4", token);
        if (!pack_name.equals(""))
            ps.println("package " + pack_name + ";");

        printImport(ps);

        printClassComment(className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Holder");
        ps.println("\timplements org.omg.CORBA.portable.Streamable");
        ps.println("{");

        ps.println("\tpublic " + getJavaTypeName() + " value;\n");

        ps.println("\tpublic " + className + "Holder ()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + className + "Holder(final " + getJavaTypeName() + " initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type ()");
        ps.println("\t{");
        ps.println("\t\treturn " + getJavaTypeName() + "Helper.type ();");
        ps.println("\t}");

        ps.println("\tpublic void _read(final org.omg.CORBA.portable.InputStream _in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + getJavaTypeName() + "Helper.read(_in);");
        ps.println("\t}");

        ps.println("\tpublic void _write(final org.omg.CORBA.portable.OutputStream _out)");
        ps.println("\t{");
        ps.println("\t\t" + getJavaTypeName() + "Helper.write(_out, value);");
        ps.println("\t}");

        ps.println("}");
    }


    private void printHelperClass(String className, PrintWriter ps)
    {
        if (Environment.JAVA14 && pack_name.equals(""))
            lexer.emit_warn
                ("No package defined for " + className + " - illegal in JDK1.4", token);
        if (!pack_name.equals(""))
            ps.println("package " + pack_name + ";\n");

        printImport(ps);

        printClassComment(className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Helper");
        ps.println("{");
        ps.println("\tprivate static org.omg.CORBA.TypeCode _type = null;");

        /* type() method */
        ps.println("\tpublic static org.omg.CORBA.TypeCode type ()");
        ps.println("\t{");
        ps.println("\t\tif (_type == null)");
        ps.println("\t\t{");

        StringBuffer sb = new StringBuffer();
        sb.append("org.omg.CORBA.ORB.init().create_" +
                (exc ? "exception" : "struct") + "_tc(" +
                getJavaTypeName() + "Helper.id(),\"" + className() + "\",");

        if (memberlist != null)
        {
            sb.append("new org.omg.CORBA.StructMember[]{");
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                sb.append("new org.omg.CORBA.StructMember(\"" + d.name() + "\", ");
                sb.append(m.type_spec.typeSpec().getTypeCodeExpression());
                sb.append(", null)");
                if (e.hasMoreElements())
                    sb.append(",");
            }
            sb.append("}");
        }
        else
        {
            sb.append("new org.omg.CORBA.StructMember[0]");
        }
        sb.append(")");

        ps.println("\t\t\t_type = " + sb.toString() + ";");

        ps.println("\t\t}");
        ps.println("\t\treturn _type;");
        ps.println("\t}\n");

        String type = getJavaTypeName();
        TypeSpec.printInsertExtractMethods(ps, type);

        printIdMethod(ps); // inherited from IdlSymbol

        /* read */
        ps.println("\tpublic static " + type +
                    " read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\t" + type + " result = new " + type + "();");
        if (exc)
        {
            ps.println("\t\tif (!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL(\"wrong id\");");
        }
        if (memberlist != null)
        {
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                ps.println("\t\t" + m.type_spec.typeSpec().printReadStatement("result." + d.name(), "in"));
            }
        }
        ps.println("\t\treturn result;");
        ps.println("\t}");

        /* write */
        ps.println("\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, final " + type + " s)");
        ps.println("\t{");

        if (exc)
        {
            ps.println("\t\tout.write_string(id());");
        }

        if (memberlist != null)
        {
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                ps.println("\t\t" + m.type_spec.typeSpec().printWriteStatement("s." + d.name(), "out"));
            }
        }
        ps.println("\t}");
        ps.println("}");
    }

    private void printStructClass(String className, PrintWriter ps)
    {
        if (Environment.JAVA14 && pack_name.equals(""))
            lexer.emit_warn
                ("No package defined for " + className + " - illegal in JDK1.4", token);
        String fullClassName = className;

        if (!pack_name.equals(""))
        {
            fullClassName = pack_name + "." + className;

            ps.println("package " + pack_name + ";");
        }

        printImport(ps);

        printClassComment(className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className);
        if (exc)
            ps.println("\textends org.omg.CORBA.UserException");
        else
            ps.println("\timplements org.omg.CORBA.portable.IDLEntity");

        ps.println("{");

        // print an empty constructor

        if (exc)
        {
            ps.println("\tpublic " + className + "()");
            ps.println("\t{");
            ps.println("\t\tsuper(" + fullClassName + "Helper.id());");
            ps.println("\t}");
            ps.println();
            if (memberlist == null)
            {
                ps.println("\tpublic " + className + "(String value)");
                ps.println("\t{");
                ps.println("\t\tsuper(value);");
                ps.println("\t}");
            }
        }
        else
        {
            ps.println("\tpublic " + className + "(){}");
        }

        if (memberlist != null)
        {
            // print member declarations

            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                ((Member)e.nextElement()).member_print(ps, "\tpublic ");
                ps.println();
            }

            // print a constructor for class member initialization

            if (exc)
            {
                // print a constructor for class member initialization with additional first string parameter

                ps.print("\tpublic " + className + "(");
                ps.print("java.lang.String _reason,");
                for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
                {
                    Member m = (Member)e.nextElement();
                    Declarator d = m.declarator;
                    ps.print(m.type_spec.toString() + " " + d.toString());
                    if (e.hasMoreElements())
                        ps.print(", ");
                }
                ps.println(")");

                ps.println("\t{");
                ps.println("\t\tsuper(" + fullClassName + "Helper.id()+\"\"+_reason);");
                for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
                {
                    Member m = (Member)e.nextElement();
                    Declarator d = m.declarator;
                    ps.print("\t\tthis.");
                    ps.print(d.name());
                    ps.print(" = ");
                    ps.println(d.name() + ";");
                }
                ps.println("\t}");
            }
            ps.print("\tpublic " + className + "(");
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                ps.print(m.type_spec.toString() + " " + d.name());
                if (e.hasMoreElements())
                    ps.print(", ");
            }
            ps.println(")");


            ps.println("\t{");
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                ps.print("\t\tthis.");
                ps.print(d.name());
                ps.print(" = ");
                ps.println(d.name() + ";");
            }
            ps.println("\t}");

        }
        ps.println("}");
    }

    /**
     * Generates code from this AST class
     *
     * @param ps not used, the necessary output streams to classes
     * that receive code (e.g., helper and holder classes for the
     * IDL/Java mapping, are created inside this method.
     */

    public void print(PrintWriter ps)
    {
        setPrintPhaseNames();

        if (!parsed)
        {
            throw new ParseException ("Unparsed Struct!");
        }

        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return;
        }

        // only generate code once

        if (!written)
        {
            // guard against recursive entries, which can happen due to
            // containments, e.g., an alias within an interface that refers
            // back to the interface
            written = true;

            try
            {
                String className = className();

                String path = parser.out_dir + fileSeparator +
                    pack_name.replace('.', fileSeparator);

                File dir = new File(path);
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        org.jacorb.idl.parser.fatal_error("Unable to create " + path, null);
                    }
                }

                String fname = className + ".java";
                File f = new File(dir, fname);

                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped java class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printStructClass(className, printWriter);
                    printWriter.close();
                }

                fname = className + "Holder.java";
                f = new File(dir, fname);

                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped holder class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printHolderClass(className, printWriter);
                    printWriter.close();
                }

                fname = className + "Helper.java";
                f = new File(dir, fname);

                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped helper class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printHelperClass(className, printWriter);
                    printWriter.close();
                }

                // written = true;
            }
            catch (java.io.IOException i)
            {
                System.err.println("File IO error");
                i.printStackTrace();
            }
        }
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitStruct(this);
    }



}
