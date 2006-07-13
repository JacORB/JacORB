/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

/**
 * IDL sequences.
 *
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class SequenceType
    extends VectorType
{
    private boolean written = false;

    /** used to generate unique name vor local variables */
    private static int idxNum = 0;

    /** markers for recursive sequences */
    private boolean recursive = false;

    public ConstExpr max = null;
    int length = 0;

    public SequenceType(int num)
    {
        super(num);
        name = null;
        typedefd = false;
    }

    public Object clone()
    {
        SequenceType copy = new SequenceType(IdlSymbol.new_num());
        copy.type_spec = this.type_spec;
        copy.max = this.max;
        copy.length = this.length;
        copy.name = this.name;
        copy.pack_name = this.pack_name;
        copy.included = this.included;
        copy.typedefd = this.typedefd;
        copy.recursive = this.recursive;
        copy.set_token(this.get_token());
        copy.setEnclosingSymbol(this.getEnclosingSymbol());
        return copy;
    }


    public void setEnclosingSymbol(IdlSymbol symbol)
    {
        if (enclosing_symbol != null && enclosing_symbol != symbol)
        {
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        }
        enclosing_symbol = symbol;
    }


    /**
     * since the sequence type's name depends on a declarator
     * given in the typedef, the name varilabe has to be set explicitly
     * by the TypeDef object before this sequence type can
     * be used.
     */

    public TypeSpec typeSpec()
    {
        return this;
    }

    public void setPackage(String pkg)
    {
        pkg = parser.pack_replace(pkg);
        if (pack_name.length() > 0)
        {
            pack_name = pkg + "." + pack_name;
        }
        else
        {
            pack_name = pkg;
        }
        type_spec.setPackage(pkg);
        if (max != null)
        {
            max.setPackage(pkg);
        }
    }

    /**
     */

    public int length()
    {
        return length;
    }



    void setRecursive()
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Sequence " + typeName +
                        " set recursive ------- this: " + this);
        }
        recursive = true;
    }

    /**
     * @return a string for an expression of type TypeCode that describes this type
     */

    public String getTypeCodeExpression()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Sequence getTypeCodeExpression " + name);
        }

        String originalType = null;

        if (recursive)
        {
            originalType = "org.omg.CORBA.ORB.init().create_sequence_tc(" +
                length + ", org.omg.CORBA.ORB.init().create_recursive_tc(\"" +
                elementTypeSpec().id() + "\"))";
        }
        else
        {
            originalType = "org.omg.CORBA.ORB.init().create_sequence_tc(" +
                length + ", " + elementTypeExpression() + ")";
        }
        return originalType;
    }

    public static int getNumber()
    {
        return idxNum++;
    }

    /**
     * We have to distinguish between sequence types that have been
     * explicitly declared as types with a typedef and those that
     * are declared as anonymous types in structs or unions. In
     * the latter case, we have to generate marshalling code in-line
     * because there are no helpers for anonymous types
     */

    public String printReadStatement(String var_name, String streamname)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Sequence printReadStatement for " + typeName());
        }

        StringBuffer buffer = new StringBuffer();
        String type = typeName();
        String lgt = "_l" + var_name.replace('.', '_');

        // if [i] is part of the name, trim that off
        if (lgt.indexOf('[') > 0)
        {
            lgt = lgt.substring(0, lgt.indexOf('[')) + "_";
        }

        // make local variable name unique
        lgt = lgt + getNumber();

        buffer.append("int " + lgt + " = " + streamname + ".read_long();\n");
        if (length != 0)
        {
            buffer.append("\t\tif (" + lgt + " > " + length + ")\n");
            buffer.append("\t\t\tthrow new org.omg.CORBA.MARSHAL(\"Sequence length incorrect!\");\n");
        }

        buffer.append("\t\ttry\n\t\t{\n" );
        buffer.append("\t\t\t int x = " + streamname + ".available();\n" );
        buffer.append("\t\t\t if ( x > 0 && " + lgt + " > x )\n" );
        buffer.append("\t\t\t\t{\n" );
        buffer.append("\t\t\t\t\tthrow new org.omg.CORBA.MARSHAL(\"Sequence length too large. Only \" + x + \" available and trying to assign \" + " + lgt + ");\n");
        buffer.append("\t\t\t\t}\n" );
        buffer.append("\t\t}\n\t\tcatch (java.io.IOException e)\n\t\t{\n\t\t}\n");


        buffer.append("\t\t" + var_name + " = new " + type.substring(0, type.indexOf('[')) +
                  "[" + lgt + "]" + type.substring(type.indexOf(']') + 1) + ";\n");

        TypeSpec elemType = elementTypeSpec();
        while (elemType instanceof AliasTypeSpec)
        {
            //get real type
            elemType = ((AliasTypeSpec) elemType).originalType();
        }

        if (elemType instanceof BaseType &&
            !(elemType instanceof AnyType))
        {
            String _tmp = elemType.printReadExpression(streamname);
            buffer.append("\t\t");
            buffer.append(_tmp.substring(0, _tmp.indexOf('(')));
            buffer.append("_array(");
            buffer.append(var_name);
            buffer.append(",0,");
            buffer.append(lgt);
            buffer.append(");");
        }
        else
        {
            char idx_variable = 'i';
            String indent = "";
            if (var_name.endsWith("]"))
            {
                idx_variable = (char)(var_name.charAt(var_name.length() - 2) + 1);
                indent = "    ";
            }
            buffer.append("\t\t" + indent + "for (int " + idx_variable + "=0;" +
                      idx_variable + "<" + var_name + ".length;" + idx_variable + "++)\n\t\t" + indent + "{\n");

            buffer.append("\t\t\t" + indent +
                      elementTypeSpec().printReadStatement(var_name +
                                                           "[" + idx_variable + "]",
                                                           streamname)
                      + "\n");

            buffer.append("\t\t" + indent + "}\n");

        }
        return buffer.toString();
    }


    public String printWriteStatement(String var_name, String streamname)
    {
        StringBuffer buffer = new StringBuffer();
        if (length != 0)
        {
            buffer.append("\t\tif (" + var_name + ".length > " + length + ")\n");
            buffer.append("\t\t\tthrow new org.omg.CORBA.MARSHAL(\"Incorrect sequence length\");");
        }
        buffer.append("\n\t\t" + streamname + ".write_long(" + var_name + ".length);\n");

        TypeSpec elemType = elementTypeSpec();
        while (elemType instanceof AliasTypeSpec)
        {
            //get real type
            elemType = ((AliasTypeSpec) elemType).originalType();
        }

        if (elemType instanceof BaseType &&
            !(elemType instanceof AnyType))
        {
            String _tmp = elemType.printWriteStatement(var_name, streamname);
            buffer.append("\t\t");
            buffer.append(_tmp.substring(0, _tmp.indexOf('(')));
            buffer.append("_array(");
            buffer.append(var_name);
            buffer.append(",0,");
            buffer.append(var_name);
            buffer.append(".length);");
        }
        else
        {
            char idx_variable = 'i';
            String indent = "";
            if (var_name.endsWith("]"))
            {
                idx_variable = (char)(var_name.charAt(var_name.length() - 2) + 1);
                indent = "    ";
            }
            buffer.append("\t\t" + indent + "for (int " + idx_variable + "=0; " +
                      idx_variable + "<" + var_name + ".length;" +
                      idx_variable + "++)\n\t\t" + indent + "{\n");

            buffer.append("\t\t\t" + indent +
                      elementTypeSpec().printWriteStatement(var_name
                                                            + "[" + idx_variable + "]",
                                                            streamname) + "\n");
            buffer.append("\t\t" + indent + "}\n");
        }
        return buffer.toString();
    }


    public String holderName()
    {
        if (!typedefd)
        {
            throw new RuntimeException("Compiler Error: should not be called (helpername on not typedef'd SequenceType " + name + ")");
        }

        String name = full_name();
        if (pack_name.length() > 0)
        {
            name = getFullName(name);
        }

        return name + "Holder";
    }


    public String helperName()
    {
        if (!typedefd)
        {
            throw new RuntimeException("Compiler Error: should not be called (helperName() on not typedef'd SequenceType)");
        }

        String name = full_name();
        if (pack_name.length() > 0)
        {
            name = getFullName(name);
        }

        return name + "Helper";
    }

    public String className()
    {
        String fullName = full_name();
        String cName;
        if (fullName.indexOf('.') > 0)
        {
            pack_name = fullName.substring(0, fullName.lastIndexOf('.'));
            cName = fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        else
        {
            pack_name = "";
            cName = fullName;
        }
        return cName;

    }

    /**
     * The parsing phase.
     */

    public void parse()
    {
        if (max != null)
        {
            max.parse();
            length = max.pos_int_const();
        }

        if (type_spec.typeSpec() instanceof ScopedName)
        {
            TypeSpec typeSpec =
                ((ScopedName)type_spec.typeSpec()).resolvedTypeSpec();
            if (typeSpec != null)
            {
                type_spec = typeSpec;
            }

            if (type_spec instanceof AliasTypeSpec)
            {
                addImportedAlias(type_spec.full_name());
            }
            else
            {
                addImportedName(type_spec.typeName());
            }

            addImportedName(type_spec.typeSpec().typeName());
        }
        try
        {
            NameTable.define(full_name(), "type");
        }
        catch (NameAlreadyDefined n)
        {
            // ignore, sequence types can be defined a number
            // of times under different names
        }
    }

    public String full_name()
    {
        if (name == null)
        {
            return "<" + pack_name + ".anon>";
        }
        if (pack_name.length() > 0)
        {
            return ScopedName.unPseudoName(pack_name + "." + name);
        }
        return ScopedName.unPseudoName(name);
    }


    private void printHolderClass(String className, PrintWriter out)
    {
        if (Environment.JAVA14 && pack_name.equals(""))
        {
            lexer.emit_warn
                ("No package defined for " + className + " - illegal in JDK1.4", token);
        }
        if (!pack_name.equals(""))
        {
            out.println("package " + pack_name + ";\n");
        }

        String type = typeName();

        printImport(out);

        printClassComment("sequence", className, out);

        out.println("public" + parser.getFinalString() + " class " + className + "Holder");
        out.println("\timplements org.omg.CORBA.portable.Streamable");

        out.println("{");
        out.println("\tpublic " + type + " value;");
        out.println("\tpublic " + className + "Holder ()");
        out.println("\t{");
        out.println("\t}");

        out.println("\tpublic " + className + "Holder (final " + type + " initial)\n\t{");
        out.println("\t\tvalue = initial;");
        out.println("\t}");

        out.println("\tpublic org.omg.CORBA.TypeCode _type ()");
        out.println("\t{");
        out.println("\t\treturn " + className + "Helper.type ();");
        out.println("\t}");

        out.println("\tpublic void _read (final org.omg.CORBA.portable.InputStream _in)");
        out.println("\t{");
        out.println("\t\tvalue = " + className + "Helper.read (_in);");
        out.println("\t}");

        out.println("\tpublic void _write (final org.omg.CORBA.portable.OutputStream _out)");
        out.println("\t{");
        out.println("\t\t" + className + "Helper.write (_out,value);");
        out.println("\t}");

        out.println("}");
    }


    private void printHelperClass(String className, PrintWriter out)
    {
        if (Environment.JAVA14 && pack_name.equals(""))
        {
            lexer.emit_warn
                ("No package defined for " + className + " - illegal in JDK1.4", token);
        }
        if (!pack_name.equals(""))
        {
            out.println("package " + pack_name + ";");
        }

        String type = typeName();
        printImport(out);

        printClassComment("sequence", className, out);

        out.println("public" + parser.getFinalString() + " class " + className + "Helper");
        out.println("{");
        out.println("\tprivate static org.omg.CORBA.TypeCode _type = " +
                   getTypeCodeExpression() + ";");

        TypeSpec.printHelperClassMethods(out, type);
        printIdMethod(out); // from IdlSymbol

        /** read */

        out.println("\tpublic static " + type +
                   " read (final org.omg.CORBA.portable.InputStream in)");

        out.println("\t{");
        out.println("\t\tint l = in.read_long();");

        if (length != 0)
        {
            out.println("\t\tif (l > " + length + ")");
            out.println("\t\t\tthrow new org.omg.CORBA.MARSHAL();");
        }

        out.println("\t\t" + type + " result = new " +
                   type.substring(0, type.indexOf('[')) + "[l]" +
                   type.substring(type.indexOf(']') + 1) + ";");

        if (elementTypeSpec() instanceof BaseType &&
            !(elementTypeSpec() instanceof AnyType))
        {
            String _tmp = elementTypeSpec().printReadExpression("in");
            out.println("\t\t" + _tmp.substring(0, _tmp.indexOf('(')) +
                       "_array(result,0,result.length);");
        }
        else
        {
            out.println("\t\tfor (int i = 0; i < l; i++)");
            out.println("\t\t{");
            out.println("\t\t\t" + elementTypeSpec().printReadStatement("result[i]", "in"));
            out.println("\t\t}");
        }

        out.println("\t\treturn result;");
        out.println("\t}");

        /* write */

        out.println("\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, "
                   + "final " + type + " s)");
        out.println("\t{");
        if (length != 0)
        {
            out.println("\t\tif (s.length > " + length + ")");
            out.println("\t\t\tthrow new org.omg.CORBA.MARSHAL();");
        }
        out.println("\t\tout.write_long(s.length);");

        if (elementTypeSpec() instanceof BaseType &&
            !(elementTypeSpec() instanceof AnyType))
        {
            String _tmp = elementTypeSpec().printWriteStatement("s", "out");
            out.println(_tmp.substring(0, _tmp.indexOf('(')) + "_array(s,0,s.length);");
        }
        else
        {
            out.println("\t\tfor (int i = 0; i < s.length; i++)");
            out.println("\t\t\t" + elementTypeSpec().printWriteStatement("s[i]", "out"));
        }

        out.println("\t}");
        out.println("}");
    }


    public void print(PrintWriter out)
    {
        try
        {
            // only generate class files for explicitly
            // defined sequence types, i.e. for typedef'd ones

            if ((!written) && typedefd)
            {
                // write holder file

                String fullName = full_name();
                String className;
                if (fullName.indexOf('.') > 0)
                {
                    pack_name = fullName.substring(0, fullName.lastIndexOf('.'));
                    className = fullName.substring(fullName.lastIndexOf('.') + 1);
                }
                else
                {
                    pack_name = "";
                    className = fullName;
                }

                String path = parser.out_dir + fileSeparator +
                    pack_name.replace('.', fileSeparator);

                File dir = new File(path);
                if (!dir.exists() && !dir.mkdirs())
                {
                    org.jacorb.idl.parser.fatal_error("Unable to create " +
                            path, null);
                }

                String fname = className + "Holder.java";
                File file = new File(dir, fname);

                if (GlobalInputStream.isMoreRecentThan(file))
                {
                    // print the mapped java class
                    PrintWriter holderOut = new PrintWriter(new java.io.FileWriter(file));
                    printHolderClass(className, holderOut);
                    holderOut.close();
                }

                fname = className + "Helper.java";
                file = new File(dir, fname);

                if (GlobalInputStream.isMoreRecentThan(file))
                {
                    // print the mapped java class
                    PrintWriter helperOut = new PrintWriter(new java.io.FileWriter(file));
                    printHelperClass(className, helperOut);
                    helperOut.close();
                }

                written = true;
            }
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException("File IO error" + e);
        }
    }

    public void printInsertIntoAny(PrintWriter out,
                                   String anyname,
                                   String varname)
    {
        out.println("\t" + helperName() + ".insert(" + anyname + ", " + varname + " );");
    }

    public void printExtractResult(PrintWriter out,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
       throw new RuntimeException("DII Stubs not yet complete for Sequence types");
    }
}
