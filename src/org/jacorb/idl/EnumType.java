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

package org.jacorb.idl;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gerald Brose
 */

public class EnumType
    extends TypeDeclaration
    implements SwitchTypeSpec
{
    public SymbolList enumlist;
    int const_counter = 0;
    private boolean written = false;
    private boolean parsed = false;

    public EnumType(int num)
    {
        super(num);
        pack_name = "";
    }

    public Object clone()
    {
        EnumType et = new EnumType(new_num());
        et.enumlist = this.enumlist;
        et.typeName = this.typeName;
        et.pack_name = this.pack_name;
        et.name = this.name;
        et.token = this.token;
        et.included = this.included;
        et.enclosing_symbol = this.enclosing_symbol;
        et.parsed = this.parsed;
        return et;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public int size()
    {
        return enumlist.v.size();
    }

    /**
     */

    public void set_included(boolean i)
    {
        included = i;
    }

    public String typeName()
    {
        if (typeName == null)
        {
            setPrintPhaseNames();
        }
        return typeName;
    }

    public boolean basic()
    {
        return true;
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
    }

    public void parse()
    {
        if (parsed)
        {
            return ;
        }
        parsed = true;

        escapeName();

        try
        {
            ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
            ctspec.c_type_spec = this;
            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
            String enum_ident = null;

            // we have to get the scoping right: enums do not
            // define scopes, but their element identifiers are scoped.
            // for the Java mapping, we need to get the enum type name
            // back as it defines the class name where the constants
            // are defined. Therefore, an additional mapping in
            // ScopedName is required.

            String prefix = (pack_name.length() > 0 ?
                              full_name().substring(0, full_name().lastIndexOf('.') + 1) :
                              "");

            for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
            {
                enum_ident = (String) e.nextElement();
                try
                {
                    NameTable.define(prefix + enum_ident, IDLTypes.ENUM_LABEL);
                    ScopedName.enumMap(prefix + enum_ident, full_name() +
                                        "." + enum_ident);
                }
                catch (NameAlreadyDefined p)
                {
                    parser.error("Identifier " + enum_ident +
                                  " already defined in immediate scope", token);
                }
            }
        }
        catch (NameAlreadyDefined p)
        {
            parser.error("Enum " + full_name() + " already defined", token);
        }
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

    public String printReadExpression(String streamname)
    {
        return toString() + "Helper.read(" + streamname + ")";
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

        StringBuffer sb = new StringBuffer();
        sb.append("org.omg.CORBA.ORB.init().create_enum_tc(" +
        		typeName() + "Helper.id(),\"" + className() + "\",");

        sb.append("new String[]{");

        for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
        {
        	sb.append("\"" + (String) e.nextElement() + "\"");
        	if (e.hasMoreElements())
        		sb.append(",");
        }
        sb.append("})");

        return sb.toString();
    }


    private void printHolderClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
            ps.println("package " + pack_name + ";");

        printClassComment("enum", className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Holder");
        ps.println("\timplements org.omg.CORBA.portable.Streamable");
        ps.println("{");

        ps.println("\tpublic " + className + " value;" + Environment.NL);

        ps.println("\tpublic " + className + "Holder ()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + className + "Holder (final " + className + " initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type ()");
        ps.println("\t{");
        ps.println("\t\treturn " + className + "Helper.type ();");
        ps.println("\t}");

        ps.println("\tpublic void _read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + className + "Helper.read (in);");
        ps.println("\t}");

        ps.println("\tpublic void _write (final org.omg.CORBA.portable.OutputStream out)");
        ps.println("\t{");
        ps.println("\t\t" + className + "Helper.write (out,value);");
        ps.println("\t}");

        ps.println("}");
    }

    private void printHelperClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
            ps.println("package " + pack_name + ";");

        printClassComment("enum", className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Helper");
        ps.println("{");

        ps.println("\tprivate volatile static org.omg.CORBA.TypeCode _type;");

        /* type() method */
        ps.println("\tpublic static org.omg.CORBA.TypeCode type ()");
        ps.println("\t{");
        ps.println("\t\tif (_type == null)");
        ps.println("\t\t{");
        ps.println("\t\t\tsynchronized(" + className + "Helper.class)");
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
        printIdMethod(ps);

        ps.println("\tpublic static " + className + " read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\treturn " + className + ".from_int(in.read_long());");
        ps.println("\t}" + Environment.NL);

        ps.println("\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, final " + className + " s)");
        ps.println("\t{");
        ps.println("\t\tout.write_long(s.value());");
        ps.println("\t}");
        ps.println("}");
    }

    /** print the class that maps the enum */

    private void printEnumClass(String className, PrintWriter pw)
    {
        if (!pack_name.equals(""))
            pw.println("package " + pack_name + ";");

        printClassComment("enum", className, pw);

        pw.println("public" + parser.getFinalString() + " class " + className);
        pw.println("\timplements org.omg.CORBA.portable.IDLEntity" + Environment.NL + "{");

        printSerialVersionUID(pw);

        pw.println("\tprivate int value = -1;");

        for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
        {
            String label = (String) e.nextElement();
            pw.println("\tpublic static final int _" + label + " = " + (const_counter++) + ";");
            pw.println("\tpublic static final " + name + " " + label + " = new " + name + "(_" + label + ");");
        }
        pw.println("\tpublic int value()");
        pw.println("\t{");
        pw.println("\t\treturn value;");
        pw.println("\t}");

        pw.println("\tpublic static " + name + " from_int(int value)");
        pw.println("\t{");
        pw.println("\t\tswitch (value) {");

        for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
        {
            String label = (String) e.nextElement();
            pw.println("\t\t\tcase _" + label + ": return " + label + ";");
        }
        pw.println("\t\t\tdefault: throw new org.omg.CORBA.BAD_PARAM();");
        pw.println("\t\t}");
        pw.println("\t}");

        pw.println("\tpublic String toString()");
        pw.println("\t{");
        pw.println("\t\tswitch (value) {");
        for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
        {
            String label = (String) e.nextElement();
            pw.println("\t\t\tcase _" + label + ": return \"" + label + "\";");
        }
        pw.println("\t\t\tdefault: throw new org.omg.CORBA.BAD_PARAM();");
        pw.println("\t\t}");
        pw.println("\t}");

        pw.println("\tprotected " + name + "(int i)");
        pw.println("\t{");
        pw.println("\t\tvalue = i;");
        pw.println("\t}");

        pw.println("\t/**");
        pw.println("\t * Designate replacement object when deserialized from stream. See");
        pw.println("\t * http://www.omg.org/docs/ptc/02-01-03.htm#Issue4271");
        pw.println("\t *");
        pw.println("\t * @throws java.io.ObjectStreamException");
        pw.println("\t */");
        pw.println("\tjava.lang.Object readResolve()");
        if (!parser.cldc10 )
            pw.println("\tthrows java.io.ObjectStreamException");
        pw.println("\t{");
        pw.println("\t\treturn from_int(value());");
        pw.println("\t}");
        pw.println("}");
    }


    /** generate required classes */

    public void print(PrintWriter ps)
    {
        setPrintPhaseNames();

        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return ;
        }

        // only write once

        if (!written)
        {
            try
            {
                String className = className();
                String path =
                    parser.out_dir + fileSeparator +
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
                    printEnumClass(className, printWriter);
                    printWriter.close();
                }

                fname = className + "Holder.java";
                f = new File(dir, fname);
                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the holder class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printHolderClass(className, printWriter);
                    printWriter.close();
                }

                fname = className + "Helper.java";
                f = new File(dir, fname);
                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    // print the helper class
                    PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                    printHelperClass(className, printWriter);
                    printWriter.close();
                }

                written = true;
            }
            catch (java.io.IOException i)
            {
                throw new RuntimeException("File IO error" + i);
            }
        }
    }

    public String toString()
    {
        return typeName();
    }

    public boolean isSwitchable()
    {
        return true;
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


    /**
     */

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitEnum(this);
    }
}
