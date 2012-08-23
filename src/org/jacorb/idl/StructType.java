package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gerald Brose
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
    private boolean forwardDecl;
    private static final HashSet<String> systemExceptionNames;

    static
    {
        systemExceptionNames = new HashSet();

        systemExceptionNames.add( "UNKNOWN" ) ;
        systemExceptionNames.add( "BAD_PARAM" ) ;
        systemExceptionNames.add( "NO_MEMORY" ) ;
        systemExceptionNames.add( "IMP_LIMIT" ) ;
        systemExceptionNames.add( "COMM_FAILURE" ) ;
        systemExceptionNames.add( "INV_OBJREF" ) ;
        systemExceptionNames.add( "NO_PERMISSION" ) ;
        systemExceptionNames.add( "INTERNAL" ) ;
        systemExceptionNames.add( "MARSHAL" ) ;
        systemExceptionNames.add( "INITIALIZE" ) ;
        systemExceptionNames.add( "NO_IMPLEMENT" ) ;
        systemExceptionNames.add( "BAD_TYPECODE" ) ;
        systemExceptionNames.add( "BAD_OPERATION" ) ;
        systemExceptionNames.add( "NO_RESOURCES" ) ;
        systemExceptionNames.add( "NO_RESPONSE" ) ;
        systemExceptionNames.add( "PERSIST_STORE" ) ;
        systemExceptionNames.add( "BAD_INV_ORDER" ) ;
        systemExceptionNames.add( "TRANSIENT" ) ;
        systemExceptionNames.add( "FREE_MEM" ) ;
        systemExceptionNames.add( "INV_IDENT" ) ;
        systemExceptionNames.add( "INV_FLAG" ) ;
        systemExceptionNames.add( "INTF_REPOS" ) ;
        systemExceptionNames.add( "BAD_CONTEXT" ) ;
        systemExceptionNames.add( "OBJ_ADAPTER" ) ;
        systemExceptionNames.add( "DATA_CONVERSION" ) ;
        systemExceptionNames.add( "OBJECT_NOT_EXIST" ) ;
        systemExceptionNames.add( "TRANSACTION_REQUIRED" ) ;
        systemExceptionNames.add( "TRANSACTION_ROLLEDBACK" ) ;
        systemExceptionNames.add( "INVALID_TRANSACTION" ) ;
        systemExceptionNames.add( "INV_POLICY" ) ;
        systemExceptionNames.add( "CODESET_INCOMPATIBLE" ) ;
        systemExceptionNames.add( "REBIND" ) ;
        systemExceptionNames.add( "TIMEOUT" ) ;
        systemExceptionNames.add( "TRANSACTION_UNAVAILABLE" ) ;
        systemExceptionNames.add( "TRANSACTION_MODE" ) ;
        systemExceptionNames.add( "BAD_QOS" ) ;
        systemExceptionNames.add( "INVALID_ACTIVITY" ) ;
        systemExceptionNames.add( "ACTIVITY_COMPLETED" ) ;
        systemExceptionNames.add( "ACTIVITY_REQUIRED" ) ;
    }

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
    }

    public String typeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
    }

    public int getTCKind()
    {
    	return org.omg.CORBA.TCKind._tk_struct;
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
            logger.error("was " + enclosing_symbol.getClass().getName() +
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

            NameTable.define(full_name(), IDLTypes.TYPE_STRUCT);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            if (exc)
            {
                parser.error("Struct " + typeName() + " already defined", token);
            }
            else
            {
                Object forwardDeclaration = parser.get_pending (full_name());

                if (forwardDeclaration != null)
                {
                    if (! (forwardDeclaration instanceof StructType))
                    {
                        parser.error("Forward declaration types mismatch for "
                                     + full_name()
                                     + ": name already defined with another type" , token);
                    }

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
                    parser.error("Struct " + typeName() + " already defined", token);
                }
            }
        }
        if (memberlist != null)
        {
            ScopedName.addRecursionScope(typeName());
            memberlist.parse();
            ScopedName.removeRecursionScope(typeName());

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
            parser.set_pending(full_name(), this);
            forwardDecl = true;
        }
        parsed = true;
    }

    public String className()
    {
        String fullName = typeName();
        if (fullName.indexOf('.') > 0)
        {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }

        return fullName;
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
        return typeName() + "Holder";
    }

    public String helperName()
    {
        return typeName() + "Helper";
    }

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     */

    public String getTypeCodeExpression()
    {
        return getTypeCodeExpression(new HashSet());
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        knownTypes.add(this);

        StringBuffer sb = new StringBuffer();
        sb.append("org.omg.CORBA.ORB.init().create_" +
                (exc ? "exception" : "struct") + "_tc(" +
                typeName() + "Helper.id(),\"" + className() + "\",");

        if (memberlist != null)
        {
            sb.append("new org.omg.CORBA.StructMember[]{");
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Set knownTypesLocal = new HashSet(knownTypes);
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                sb.append("new org.omg.CORBA.StructMember(\"" + d.name() + "\", ");
                sb.append(m.type_spec.typeSpec().getTypeCodeExpression(knownTypesLocal));
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

        return sb.toString();
    }


    private void printHolderClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
        {
            ps.println("package " + pack_name + ";");
        }

        printImport(ps);

        printClassComment((exc ? "exception" : "struct"), className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Holder");
        ps.println("\timplements org.omg.CORBA.portable.Streamable");
        ps.println("{");

        ps.println("\tpublic " + typeName() + " value;" + Environment.NL);

        ps.println("\tpublic " + className + "Holder ()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + className + "Holder(final " + typeName() + " initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type ()");
        ps.println("\t{");
        ps.println("\t\treturn " + typeName() + "Helper.type ();");
        ps.println("\t}");

        ps.println("\tpublic void _read(final org.omg.CORBA.portable.InputStream _in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + typeName() + "Helper.read(_in);");
        ps.println("\t}");

        ps.println("\tpublic void _write(final org.omg.CORBA.portable.OutputStream _out)");
        ps.println("\t{");
        ps.println("\t\t" + typeName() + "Helper.write(_out, value);");
        ps.println("\t}");

        ps.println("}");
    }


    private void printHelperClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
        {
            ps.println("package " + pack_name + ";" + Environment.NL);
        }

        printImport(ps);

        printClassComment((exc ? "exception" : "struct"), className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Helper");
        ps.println("{");
        ps.println("\tprivate volatile static org.omg.CORBA.TypeCode _type;");

        /* type() method */
        ps.println("\tpublic static org.omg.CORBA.TypeCode type ()");
        ps.println("\t{");
        ps.println("\t\tif (_type == null)");
        ps.println("\t\t{");
        ps.println("\t\t\tsynchronized(" + name + "Helper.class)");
        ps.println("\t\t\t{");
        ps.println("\t\t\t\tif (_type == null)");
        ps.println("\t\t\t\t{");
        ps.println("\t\t\t\t\t_type = " + getTypeCodeExpression() + ";");
        ps.println("\t\t\t\t}");
        ps.println("\t\t\t}");
        ps.println("\t\t}");
        ps.println("\t\treturn _type;");
        ps.println("\t}" + Environment.NL);

        String type = typeName();
        TypeSpec.printInsertExtractMethods(ps, type);

        printIdMethod(ps); // inherited from IdlSymbol

        /* read */
        ps.println("\tpublic static " + type +
                    " read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");

        if (parser.hasObjectCachePlugin())
        {
            ps.println("\t\t// ObjectCachePlugin BEGIN");
            parser.getObjectCachePlugin().printCheckout(ps, type, "result");
            parser.getObjectCachePlugin().printPreMemberRead(ps, this);
            ps.println("\t\t// ObjectCachePlugin END");
        }

        if (exc)
        {
            ps.println("\t\tString id = in.read_string();");
            ps.println("\t\tif (!id.equals(id())) throw new org.omg.CORBA.MARSHAL(\"wrong id: \" + id);");

            if (parser.generatedHelperPortability == parser.HELPER_JACORB)
            {
                ps.println("\t\tif (in instanceof org.jacorb.orb.giop.ReplyInputStream)");
                ps.println("\t\t{");
                ps.println("\t\t\torg.jacorb.orb.giop.ReplyInputStream reply = (org.jacorb.orb.giop.ReplyInputStream) in;");
                ps.println("\t\t\torg.omg.IOP.ServiceContext context = null;");
                ps.println("\t\t\tif ( (context = reply.getServiceContext(org.omg.IOP.ExceptionDetailMessage.value)) != null)");
                ps.println("\t\t\t{");
                ps.println("\t\t\t\torg.jacorb.orb.CDRInputStream details = new org.jacorb.orb.CDRInputStream(context.context_data);");
                ps.println("\t\t\t\tdetails.openEncapsulatedArray();");
                ps.println("\t\t\t\tid = details.read_wstring();");
                ps.println("\t\t\t}");
                ps.println("\t\t}");
            }
            if (!parser.hasObjectCachePlugin())
            {
                if (memberlist != null)
                {
                    int x = 0;
                    for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
                    {
                        Member member = (Member)e.nextElement();
                        TypeSpec typeSpec = member.type_spec.typeSpec();

                        String var = "x" + x++;
                        ps.println("\t\t" + typeSpec.typeName() + " " + var + ";");
                        ps.println("\t\t" + typeSpec.printReadStatement(var, "in"));
                    }
                }
            }

            if (parser.hasObjectCachePlugin())
            {
                if (memberlist != null)
                {
                    for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
                    {
                        Member m = (Member)e.nextElement();
                        Declarator d = m.declarator;
                        ps.println("\t\t" + m.type_spec.typeSpec().printReadStatement("result." + d.name(), "in"));
                    }
                }
            }
            else
            {
                ps.print("\t\tfinal " + type + " result = new " + type + "(");
                ps.print("id");

                if (memberlist != null)
                {
                    int x=0;
                    for (Enumeration e = memberlist.v.elements(); e.hasMoreElements(); e.nextElement())
                    {
                        ps.print(", x" + x++);
                    }
                }

                ps.println(");");
            }
        }
        else
        {
            if (!parser.hasObjectCachePlugin())
            {
                ps.println("\t\t" + type + " result = new " + type + "();");
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
        }

        if (parser.hasObjectCachePlugin())
        {
            parser.getObjectCachePlugin().printPostMemberRead(ps, this, "result");
        }

        ps.println("\t\treturn result;");
        ps.println("\t}");

        if (parser.hasObjectCachePlugin())
        {
            parser.getObjectCachePlugin().printCheckinHelper(ps, this);
        }

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
        String fullClassName = className;

        if (!pack_name.equals(""))
        {
            fullClassName = pack_name + "." + className;

            ps.println("package " + pack_name + ";");
        }

        printImport(ps);

        printClassComment((exc ? "exception" : "struct"), className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className);
        if (exc)
        {
            if( isSystemException( className ) )
            {
               // It's a system exception, so inherit from SystemException.
               //
               ps.println("\textends org.omg.CORBA.SystemException");
            }
            else
            {
               // Not a system exception, so inherit from UserException.
               //
               ps.println("\textends org.omg.CORBA.UserException");
            }
        }
        else
        {
            ps.println("\timplements org.omg.CORBA.portable.IDLEntity");
        }

        ps.println("{");

        printSerialVersionUID(ps);

        if (exc)
        {
            if( isSystemException( className ) )
            {
               // Generate system exception constructors.
               //
               ps.println("\tpublic " + className + "()");
               ps.println("\t{");
               ps.print("\t\tsuper(" ) ;
               ps.print( " \"\"" ) ;
               ps.print( ", 0" ) ;
               ps.print( " ,org.omg.CORBA.CompletionStatus.COMPLETED_NO" ) ;
               ps.println( " ) ;" ) ;
               ps.println("\t}");
               ps.println();

               ps.println("\tpublic " + className + "( String reason )");
               ps.println("\t{");
               ps.print("\t\tsuper(" ) ;
               ps.print( " reason" ) ;
               ps.print( ", 0" ) ;
               ps.print( " ,org.omg.CORBA.CompletionStatus.COMPLETED_NO" ) ;
               ps.println( " ) ;" );
               ps.println("\t}") ;
               ps.println();

               ps.print("\tpublic " + className + "(" ) ;
               ps.print( "int minor" ) ;
               ps.print( ", org.omg.CORBA.CompletionStatus completed" ) ;
               ps.println( " )" ) ;
               ps.println("\t{");
               ps.print("\t\tsuper(" ) ;
               ps.print( " \"\"" ) ;
               ps.print( ", minor" ) ;
               ps.print( ", completed" ) ;
               ps.println( " ) ;" ) ;
               ps.println("\t}");
               ps.println();

               ps.print("\tpublic " + className + "(" ) ;
               ps.print( "String reason" ) ;
               ps.print( ", int minor" ) ;
               ps.print( ", org.omg.CORBA.CompletionStatus completed" ) ;
               ps.println( " )" ) ;
               ps.println("\t{");
               ps.print("\t\tsuper(" ) ;
               ps.print( " reason" ) ;
               ps.print( ", minor" ) ;
               ps.print( ", completed" ) ;
               ps.println( " ) ;" ) ;
               ps.println("\t}");
               ps.println();
            }
            else
            {
               // Generate empty user exception constructors.
               //
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
        }
        else
        {
            ps.println("\tpublic " + className + "(){}");
        }

        if (memberlist != null)
        {
            // print member declarations

            if ( ! isSystemException( className ) )
            {
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                ((Member)e.nextElement()).member_print(ps, "\tpublic ");
                ps.println();
            }
            }


            if (parser.generateEnhanced)
            {
                printToString(fullClassName, ps);
                printEquals(fullClassName, ps);
            }

            // print a constructor for class member initialization, unless
            // this is a system exception
            if (exc && ( ! isSystemException( className ) ))
            {
                // print a constructor for class member initialization
                // with additional first string parameter

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
                ps.println("\t\tsuper(_reason);");
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


            // If this is a system exception, then we don't need this
            // member initialisation constructor.
            //
            if( ! isSystemException( className ) )
            {
                ps.print("\tpublic " + className + "(");
                for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
                {
                    Member m = (Member)e.nextElement();
                    Declarator d = m.declarator;
                    ps.print(m.type_spec.toString() + " " + d.name());
                    if (e.hasMoreElements())
                    {
                        ps.print(", ");
                    }
                }
                ps.println(")");

                ps.println("\t{");

                if (exc) // fixes #462
                {
                    ps.println("\t\tsuper(" + fullClassName + "Helper.id());");
                }

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
            lexer.restorePosition(myPosition);
            parser.fatal_error ("Unparsed Struct!", token);
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

                if (!forwardDecl && GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped java class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printStructClass(className, printWriter);
                    printWriter.close();
                }

                fname = className + "Holder.java";
                f = new File(dir, fname);

                if (!forwardDecl  && GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped holder class unless it is for
                    // a system exception
                    if( ! isSystemException( className ) )
                    {
                        PrintWriter printWriter = new PrintWriter
                            (new java.io.FileWriter(f));
                        printHolderClass(className, printWriter);
                        printWriter.close();
                    }
                }

                fname = className + "Helper.java";
                f = new File(dir, fname);

                if (!forwardDecl && GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the mapped helper class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printHelperClass(className, printWriter);
                    printWriter.close();
                }
            }
            catch (java.io.IOException i)
            {
                throw new RuntimeException("File IO error" + i);
            }
        }
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        ps.println("\t\t" + pack_name + "." + className() + "Helper.insert(" + anyname + ", " + varname + ");");
    }



    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = " + pack_name + "." + className() + "Helper.extract(" + anyname + ");");
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitStruct(this);
    }

    private void printEquals(String s, PrintWriter printwriter)
    {
        printwriter.println("\tpublic boolean equals(java.lang.Object o) ");
        printwriter.println("\t{ ");

        StringBuffer buffer = new StringBuffer("\t\tif (this == o) return true;");
        buffer.append(Environment.NL);
        buffer.append("\t\tif (o == null) return false;");
        buffer.append(Environment.NL);
        buffer.append(Environment.NL);
        buffer.append("\t\tif (o instanceof " + s + " )" + Environment.NL + "\t\t{");
        buffer.append(Environment.NL);
        buffer.append("\t\t\tfinal " + s + " obj = ( " + s + " )o;");
        buffer.append(Environment.NL);
        buffer.append("\t\t\tboolean res = true; ");
        buffer.append(Environment.NL);
        buffer.append("\t\t\tdo { ");
        buffer.append(Environment.NL);
        for(Enumeration enumeration = memberlist.v.elements(); enumeration.hasMoreElements();)
        {
            Member member = (Member)enumeration.nextElement();
            if(BaseType.isBasicName(member.type_spec.toString()) && !member.type_spec.toString().equals("String") && !member.type_spec.toString().equals("java.lang.String") && member.type_spec.toString().indexOf("[") < 0)
            {
                buffer.append("\t\t\t\tres = (this." + member.declarator.toString() + " == obj." + member.declarator.toString() + ");");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t\tif (!res) break;" + Environment.NL + Environment.NL);
            }
            else
            {
                if(member.type_spec.toString().indexOf("[") >= 0)
                {
                    buffer.append("\t\t\t\tres = (this." + member.declarator.toString() + " == obj."
                                  + member.declarator.toString() + ") || (this." + member.declarator.toString()
                                  + " != null && obj." + member.declarator.toString() + " != null && this."
                                  + member.declarator.toString() + ".length == obj." + member.declarator.toString()
                                  + ".length);" + Environment.NL);

                    buffer.append("\t\t\t\tif (res)" + Environment.NL + Environment.NL
                                  + "\t\t\t\t{" + Environment.NL);
                    buffer.append("\t\t\t\t\tres = java.util.Arrays.equals(this." + member.declarator.toString()
                                  + ", obj." + member.declarator.toString() + ");" + Environment.NL);
                    buffer.append("\t\t\t\t}" + Environment.NL);
                    buffer.append("\t\t\t\tif(!res) break;" + Environment.NL + Environment.NL);
                }
                else
                {
                    buffer.append("\t\t\t\tres = (this." + member.declarator.toString() + " == obj."
                                  + member.declarator.toString() + ") || (this." + member.declarator.toString()
                                  + " != null && obj." + member.declarator.toString() + " != null && this."
                                  + member.declarator.toString() + ".equals (obj." + member.declarator.toString()
                                  + "));" + Environment.NL);
                    buffer.append("\t\t\t\tif (!res) break;" + Environment.NL + Environment.NL);
                }
            }
        }

        buffer.append("\t\t\t}");
        buffer.append(Environment.NL);
        buffer.append("\t\t\twhile(false);");
        buffer.append(Environment.NL);
        buffer.append("\t\t\treturn res;");
        buffer.append(Environment.NL);
        buffer.append("\t\t}");
        buffer.append(Environment.NL);
        buffer.append("\t\telse" + Environment.NL + "\t\t{" + Environment.NL);
        buffer.append("\t\t\treturn false;");
        buffer.append(Environment.NL);
        buffer.append("\t\t}");
        buffer.append(Environment.NL);
        buffer.append("\t}");
        buffer.append(Environment.NL);

        printwriter.println(buffer.toString());
        printwriter.println();
    }

    private void printToString(String s, PrintWriter printwriter)
    {
        printwriter.println("\tpublic String toString() ");
        printwriter.println("\t{ ");

        StringBuffer buffer = new StringBuffer("\t\tfinal java.lang.StringBuffer _ret  =  new java.lang.StringBuffer(\"struct " + s + " {\"); ");
        buffer.append(Environment.NL);
        buffer.append("\t\t_ret.append(\"\\n\"); ");
        for(Enumeration enumeration = memberlist.v.elements(); enumeration.hasMoreElements();)
        {
            Member member = (Member)enumeration.nextElement();
            buffer.append(Environment.NL);
            buffer.append("\t\t_ret.append(\"" + member.type_spec.toString() + " "
                          + member.declarator.toString() + "=\");");
            buffer.append(Environment.NL);
            if(member.type_spec.toString().indexOf("[") < 0)
            {
                buffer.append("\t\t_ret.append(" + member.declarator.toString() + ");");
            }
            else
            {
                buffer.append("\t\t_ret.append(\"{\");");
                buffer.append(Environment.NL);
                buffer.append("\t\tif(" + member.declarator.toString() + "== null){");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t_ret.append(" + member.declarator.toString() + ");");
                buffer.append(Environment.NL);
                buffer.append("\t\t}else { ");
                buffer.append(Environment.NL);
                buffer.append("\t\t\tfor(int $counter =0; $counter < " + member.declarator.toString() + ".length; $counter++){ ");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t\t_ret.append(" + member.declarator + "[$counter]);");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t\tif($counter < " + member.declarator.toString() + ".length-1) { ");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t\t\t_ret.append(\",\");");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t\t} ");
                buffer.append(Environment.NL);
                buffer.append("\t\t\t}");
                buffer.append(Environment.NL);
                buffer.append("\t\t} ");
                buffer.append(Environment.NL);
                buffer.append("\t\t_ret.append(\"}\");");
                buffer.append(Environment.NL);
            }
            buffer.append(Environment.NL);
            if(enumeration.hasMoreElements())
            {
                buffer.append("\t\t_ret.append(\",\\n\");");
            } else
            {
                buffer.append("\t\t_ret.append(\"\\n\");");
            }
        }

        buffer.append(Environment.NL);
        buffer.append("\t\t_ret.append(\"}\");" + Environment.NL);
        buffer.append("\t\treturn _ret.toString();");

        printwriter.println(buffer);
        printwriter.println("\t} ");
        printwriter.println();
    }

    /**
     * Decides if a class name is a CORBA System Exception name,
     * ignoring case.
     *
     * @param className a string containing the name to test
     *
     * @return true if the name is a system exception, false if not.
     */
    private boolean isSystemException( String className )
    {
        String ucClassName = className.toUpperCase();

        return systemExceptionNames.contains(ucClassName);
    }
}
