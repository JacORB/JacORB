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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Andre Spiegel
 */

public class ValueDecl
    extends Value
{
    private MemberList stateMembers;
    private List operations;
    private List exports;
    private List factories;
    private ValueInheritanceSpec inheritanceSpec;

    // some flags...
    private boolean isCustomMarshalled = false;
    private boolean hasStatefulBases = false;
    private boolean hasBody = false;

    /** public c'tor, called by parser */

    public ValueDecl(int num)
    {
        super(num);
        stateMembers = new MemberList(new_num());
        operations = new ArrayList();
        exports = new ArrayList();
        factories = new ArrayList();
    }

    public void setValueElements(Definitions d)
    {
        hasBody = true;

        for(Iterator i = d.v.iterator(); i.hasNext();)
        {
            Declaration dec = ((Definition)(i.next())).get_declaration();
            dec.setPackage(name);
            if (dec instanceof StateMember)
                stateMembers.v.add(dec);
            else if (dec instanceof OpDecl)
                operations.add(dec);
            else if (dec instanceof InitDecl)
                factories.add(dec);
            else
                exports.add(dec);
        }
        stateMembers.setContainingType(this);
        stateMembers.setPackage(name);
        stateMembers.setEnclosingSymbol(this);

        for(Iterator i = operations.iterator(); i.hasNext();)
            ((OpDecl)i.next()).setEnclosingSymbol(this);

        for(Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setEnclosingSymbol(this);

        for(Iterator i = factories.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setEnclosingSymbol(this);
    }

    public void setInheritanceSpec(ValueInheritanceSpec spec)
    {
        inheritanceSpec = spec;
    }

    public ValueInheritanceSpec getInheritanceSpec()
    {
        return inheritanceSpec;
    }

    public void isCustomMarshalled(boolean flag)
    {
        this.isCustomMarshalled = flag;
    }

    public boolean isCustomMarshalled()
    {
        return this.isCustomMarshalled;
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        stateMembers.setPackage(s);

        if (inheritanceSpec != null)
            inheritanceSpec.setPackage(s);

        for(Iterator i = operations.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);

        for(Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);

        for(Iterator i = factories.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public void parse()
    {
        if (inheritanceSpec != null)
        {
            inheritanceSpec.parse();
        }

        boolean justAnotherOne = false;

        if (isCustomMarshalled() &&
            inheritanceSpec != null &&
            inheritanceSpec.truncatable != null)
        {
            parser.error("Valuetype " + typeName() +
                         " may no be BOTH custom AND truncatable", token);
        }

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());

        try
        {
            escapeName();
            ScopedName.definePseudoScope(full_name());

            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            Object forwardDeclaration = parser.get_pending (full_name ());

            if (forwardDeclaration != null)
            {
                if (! (forwardDeclaration instanceof ValueDecl))
                {
                    parser.error("Forward declaration types mismatch for "
                            + full_name()
                            + ": name already defined with another type" , token);
                }

                if (stateMembers.size () != 0)
                {
                    justAnotherOne = true;
                }
                if (! full_name().equals("org.omg.CORBA.TypeCode") &&
                    stateMembers.size () != 0)
                {
                    TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                }
            }
            else
            {
                parser.error("Valuetype " + typeName() + " already defined", token);
            }
        }

        if (hasBody)
        {
            parser.logger.log(Level.WARNING, "valueDecl.parse(): exports (but not attributes)");

            // parse exports
            Iterator iter = exports.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();

                if (! ( idlSymbol instanceof AttrDecl ) )
                {
                    idlSymbol.parse();
                }
            }

            parser.logger.log(Level.WARNING, "valueDecl.parse(): members");

            ScopedName.addRecursionScope(typeName());
            stateMembers.parse();
            ScopedName.removeRecursionScope(typeName());


            parser.logger.log(Level.WARNING, "valueDecl.parse(): operations");

            // parse operations
            iter = operations.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();
                idlSymbol.parse();
            }


            parser.logger.log(Level.WARNING, "valueDecl.parse(): exports(attributes)");

            // parser exports
            iter = exports.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();

                if (idlSymbol instanceof AttrDecl)
                {
                    idlSymbol.parse();
                    Enumeration e = ((AttrDecl)idlSymbol).getOperations();
                    while(e.hasMoreElements())
                    {
                        operations.add(e.nextElement());
                    }
                }
            }

            parser.logger.log(Level.WARNING, "valueDecl.parse(): factories");

            // parse factories
            iter = factories.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();
                idlSymbol.parse();
            }

            // check inheritance rules
            parser.logger.log(Level.WARNING, "valueDecl.parse(): check inheritance");

            if (inheritanceSpec != null)
            {
                HashSet h = new HashSet();
                for(Enumeration e = inheritanceSpec.getValueTypes();
                    e.hasMoreElements();)
                {
                    ScopedName scopedName = (ScopedName)e.nextElement();

                    ConstrTypeSpec ts = unwindTypedefs(scopedName);

                    if (ts.declaration() instanceof Value)
                    {
                        if (h.contains(ts.full_name()))
                        {
                            parser.fatal_error("Illegal inheritance spec: " +
                                               inheritanceSpec  +
                                               " (repeated inheritance not allowed).",
                                               token);
                        }
                        // else:
                        h.add(ts.full_name());
                        continue;
                    }
                    parser.logger.log(Level.SEVERE, " Declaration is " + ts.declaration().getClass());
                    parser.fatal_error("Non-value type in inheritance spec: " + Environment.NL
                                       + "\t" + inheritanceSpec, token);
                }

                for(Enumeration e = inheritanceSpec.getSupportedInterfaces();
                    e.hasMoreElements();)
                {
                    ScopedName scopedName = (ScopedName)e.nextElement();
                    ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                    if (ts.declaration() instanceof Interface)
                    {
                        continue;
                    }
                    parser.fatal_error("Non-interface type in supported interfaces list: " + Environment.NL
                                       + "\t" + inheritanceSpec, token);
                }
            }
            NameTable.parsed_interfaces.put(full_name(), "");
            parser.remove_pending(full_name());
        }
        else if (! justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }

    }

    private ConstrTypeSpec unwindTypedefs(ScopedName scopedName)
    {
        TypeSpec resolvedTSpec = scopedName.resolvedTypeSpec();
        //unwind any typedefs
        while (resolvedTSpec instanceof AliasTypeSpec )
        {
            resolvedTSpec =
                ((AliasTypeSpec)resolvedTSpec).originalType();
        }

        if (! (resolvedTSpec instanceof ConstrTypeSpec))
        {
            if (parser.logger.isLoggable(Level.ALL))
            {
                parser.logger.log(Level.ALL, "Illegal inheritance spec, not a constr. type but " +
                 resolvedTSpec.getClass() + ", name " + scopedName);
            }
            parser.fatal_error("Illegal inheritance spec (not a constr. type): " +
                               inheritanceSpec, token);
        }

        return (ConstrTypeSpec) resolvedTSpec;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() +
               " now: " + s.getClass().getName());
            throw new RuntimeException("Compiler Error: trying to reassign container for " +
                                       name);
        }

        enclosing_symbol = s;
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public boolean basic()
    {
        return true;
    }

    public String toString()
    {
        return full_name();
    }

    public String holderName()
    {
        return javaName() + "Holder";
    }

    public String helperName() {
        return javaName() + "Helper";
    }

    public String typeName()
    {
        return full_name();
    }

    public String getTypeCodeExpression()
    {
        return this.getTypeCodeExpression(new HashSet());
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        String baseType = "null";

        // Only add e.g. FooHelper.type() for those inherited non-abstract ValueTypes.
        if (hasStatefulBases &&
            inheritanceSpec != null && inheritanceSpec.v.size() > 0)
        {
            baseType = ((ScopedName)inheritanceSpec.v.get(0)).resolvedName() + "Helper.type()";
        }

        StringBuffer result = new StringBuffer
        ("org.omg.CORBA.ORB.init().create_value_tc (" +
                // id, name
                "\"" + id() + "\", " + "\"" + name + "\", " +
                // type modifier
                "(short)" +
                (this.isCustomMarshalled()
                        ? org.omg.CORBA.VM_CUSTOM.value
                        : org.omg.CORBA.VM_NONE.value
                ) + ", " +
                // concrete base type
                baseType + ", " +
                // value members
        "new org.omg.CORBA.ValueMember[] {");
        knownTypes.add(this);
        for(Iterator i = stateMembers.v.iterator(); i.hasNext();)
        {
            Set knownTypesLocal = new HashSet(knownTypes);
        	StateMember m = (StateMember)i.next();
            result.append(getValueMemberExpression(m, knownTypesLocal));
            if (i.hasNext()) result.append(", ");
        }
        knownTypes.remove(this);
        result.append("})");
        return result.toString();
    }

    private String getValueMemberExpression(StateMember m, Set knownTypes)
    {
        TypeSpec typeSpec = m.typeSpec();
        //if the type is not a basic type and is in the typeMap
        //use the typeSpec saved within the TypeMap
        if (typeSpec.full_name() != null &&
        		!typeSpec.full_name().equals("IDL:*primitive*:1.0")
        		&&
        		TypeMap.typemap.containsKey(typeSpec.full_name()))
        	typeSpec =  TypeMap.map(typeSpec.full_name());

        String memberTypeExpression = typeSpec.getTypeCodeExpression(knownTypes);
        short access = m.isPublic
            ? org.omg.CORBA.PUBLIC_MEMBER.value
            : org.omg.CORBA.PRIVATE_MEMBER.value;

        return "new org.omg.CORBA.ValueMember (" +
            "\"" + m.name + "\", \"" + typeSpec.id() +
            "\", \"" + name + "\", \"1.0\", " +
            memberTypeExpression + ", null, " +
            "(short)" + access + ")";
    }

    public void print(PrintWriter ps)
    {
        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return;
        }

        //no code generation for forward declarations (bug #539)
        if (!hasBody)
        {
            return;
        }

        try
        {
            String path = parser.out_dir
                + fileSeparator
                + pack_name.replace('.', fileSeparator);

            File dir = new File(path);

            if (!dir.exists())
            {
                if (!dir.mkdirs())
                    org.jacorb.idl.parser.fatal_error
                        ("Unable to create " + path, null);
            }

            printClass(dir);
            printFactory(dir);
            printHelper(dir);
            printHolder(dir);

            // print class files for exports definitions
            for (Iterator i = exports.iterator(); i.hasNext();) {
                ((IdlSymbol)i.next()).print(null);
            }
        }
        catch (IOException e)
        {
            org.jacorb.idl.parser.fatal_error
                ("I/O error writing " + javaName() + ": " + e, null);
        }
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        // pass in null repository id to prevent CDROutputStream
        // to resolve the RMI repository ID

        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
            + ".write_value (" + var_name + ", (String)null);";
    }

    public String printReadExpression(String streamname)
    {
        return "(" + javaName() + ")"
            + "((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ")"
            + ".read_value (\"" + id() + "\")";
    }

    public String printReadStatement(String var_name, String streamname)
    {
        return var_name + " = " + printReadExpression(streamname);
    }

    /**
     * Prints the abstract Java class to which this valuetype is mapped.
     */

    private void printClass(File dir)
        throws IOException
    {
        File outfile = new File(dir, name + ".java");

        // If we have a body (i.e. we've defined any pending_interface) and the 'more
        // recent check' is ok then write the file.
        if (hasBody && GlobalInputStream.isMoreRecentThan(outfile))
        {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));

            if (pack_name.length() > 0)
            {
                out.println("package " + pack_name + ";" + Environment.NL);
            }

            printClassComment("valuetype", name, out);
            out.println("public abstract class " + name);

            // set up extends and implements clauses

            StringBuffer extendsBuffer = new StringBuffer("extends ");
            StringBuffer implementsBuffer = new StringBuffer("implements ");

            if (this.isCustomMarshalled())
                implementsBuffer.append("org.omg.CORBA.portable.CustomValue");
            else
                implementsBuffer.append("org.omg.CORBA.portable.StreamableValue");

            if (inheritanceSpec != null)
            {
                boolean first = true;

                // go through ancestor value types
                Enumeration e = inheritanceSpec.getValueTypes();
                if (e.hasMoreElements() || inheritanceSpec.truncatable != null)
                {
                    if (e.hasMoreElements())
                    {
                        ScopedName scopedName = (ScopedName)e.nextElement();
                        ConstrTypeSpec ts = unwindTypedefs(scopedName);
                            //(ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();

                        // abstract base valuetypes are mapped to interfaces, so
                        // we "implement"
                        if (ts.c_type_spec instanceof ValueAbsDecl)
                        {
                            implementsBuffer.append(", " + ts.toString());
                        }
                        else
                        {
                            // stateful base valuetypes are mapped to classes, so
                            // we  "extend"
                            first = false;
                            extendsBuffer.append(ts.toString());
                        }
                    }

                    for(; e.hasMoreElements();)
                    {
                        ScopedName scopedName = (ScopedName)e.nextElement();
                        ConstrTypeSpec ts =
                            (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();

                        // abstract base valuetypes are mapped to interfaces, so
                        // we "implement"
                        if (ts.c_type_spec instanceof ValueAbsDecl)
                        {
                            implementsBuffer.append(", " + scopedName.toString());
                        }
                        else
                        {
                            // stateful base valuetypes are mapped to classes, so
                            // we "extend"
                            //
                            // applied patch by Thomas Leineweber for bug #492
                            //
                            if (first)
                            {
                                extendsBuffer.append(scopedName.toString());
                                first = false;
                            }
                            else
                            {
                                extendsBuffer.append(", " + scopedName.toString());
                            }
                        }
                    }

                    // also check for the presence of a stateful base value type
                    // that we can be truncated to
                    if (inheritanceSpec.truncatable != null)
                    {
                        extendsBuffer.append
                            (
                             (first ? "" : ", ") +
                             inheritanceSpec.truncatable.scopedName
                             );
                    }
                }

                // go through supported interfaces
                Enumeration enumeration = inheritanceSpec.getSupportedInterfaces();
                if (enumeration.hasMoreElements())
                {
                    for(; enumeration.hasMoreElements();)
                    {
                        ScopedName sne = (ScopedName)enumeration.nextElement();
                        implementsBuffer.append (", " + sne);
                        if (Interface.abstractInterfaces == null ||
                            !Interface.abstractInterfaces.contains (sne.toString()))
                        {
                            implementsBuffer.append ("Operations");
                        }
                    }
                }

            }

            if (extendsBuffer.length() > 8)
            {
                hasStatefulBases = true;
                out.println("\t" + extendsBuffer.toString());
            }

            out.println("\t" + implementsBuffer.toString());

            out.println("{");

            printSerialVersionUID(out);

            // collect and print repository ids that this value type can
            // truncated to.

            out.print("\tprivate String[] _truncatable_ids = {\"" + id() + "\"");
            StringBuffer sb = new StringBuffer();

            if (inheritanceSpec != null)
            {
                Truncatable trunc = inheritanceSpec.truncatable;

                if (trunc != null)
                {
                    sb.append(", \"" + trunc.getId() + "\"");
                    ScopedName scopedName = trunc.scopedName;
                    while(scopedName != null)
                    {
                        ValueDecl v  =
                            (ValueDecl)((ConstrTypeSpec)scopedName.resolvedTypeSpec()).c_type_spec;

                        if (v.inheritanceSpec == null)
                        {
                            break;
                        }
                        Truncatable t = v.inheritanceSpec.truncatable;
                        if (t != null)
                        {
                            sb.append(", \"" + t.getId() + "\"");
                            scopedName = t.scopedName;
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
            out.println(sb.toString() +  "};");

            for(Iterator i = stateMembers.v.iterator(); i.hasNext();)
            {
                ((StateMember)i.next()).print(out);
                out.println();
            }

            for(Iterator i = operations.iterator(); i.hasNext();)
            {
                ((Operation)i.next()).printSignature(out, true);
                out.println();
            }

            if (!this.isCustomMarshalled())
            {
                printWriteMethod(out);
                printReadMethod(out);
            }

            out.println("\tpublic String[] _truncatable_ids()");
            out.println("\t{");
            out.println("\t\treturn _truncatable_ids;");  // FIXME
            out.println("\t}");

            out.println("\tpublic org.omg.CORBA.TypeCode _type()");
            out.println("\t{");
            out.println("\t\treturn " + javaName() + "Helper.type();");
            out.println("\t}");

            out.println("}");
            out.close();
        }
    }

    /**
     * Prints the Factory interface for this valuetype if any
     * factories were defined.
     */

    private void printFactory(File dir)
        throws IOException
    {
        if (factories.size() == 0)
        {
            return;
        }

        File outfile = new File(dir, name + "ValueFactory.java");

        // If we have a body (i.e. we've defined any pending_interface) and the 'more
        // recent check' is ok then write the file.
        if (hasBody && GlobalInputStream.isMoreRecentThan(outfile))
        {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));

            if (pack_name.length() > 0)
            {
                out.println("package " + pack_name + ";" + Environment.NL);
            }

            printClassComment("valuetype", name, out);

            out.println("public interface  " + name + "ValueFactory");
            out.println("\textends org.omg.CORBA.portable.ValueFactory");
            out.println("{");

            for(Iterator i = factories.iterator(); i.hasNext();)
            {
                ((InitDecl)i.next()).print(out, name);
            }

            out.println("}");
            out.close();
        }
    }


    /**
     * Prints the _write() method required by
     * org.omg.CORBA.portable.StreamableValue.
     */
    private void printWriteMethod(PrintWriter out)
    {
        out.println("\tpublic void _write " +
                    "(org.omg.CORBA.portable.OutputStream os)");
        out.println("\t{");

        if (hasStatefulBases)
        {
            out.println("\t\tsuper._write(os);");
        }

        for(Iterator i = stateMembers.v.iterator(); i.hasNext();)
        {
            out.println("\t\t" + ((StateMember)i.next()).writeStatement("os"));
        }
        out.println("\t}" + Environment.NL);
    }

    /**
     * Prints the _read() method required by
     * org.omg.CORBA.portable.StreamableValue.
     */

    private void printReadMethod(PrintWriter out)
    {
        out.println("\tpublic void _read " +
                    "(final org.omg.CORBA.portable.InputStream os)");
        out.println("\t{");

        if (hasStatefulBases)
        {
            out.println("\t\tsuper._read(os);");
        }

        for(Iterator i = stateMembers.v.iterator(); i.hasNext();)
        {
            out.println("\t\t" + ((StateMember)i.next()).readStatement("os"));
        }
        out.println("\t}" + Environment.NL);
    }

    private void printHelper(File dir)
        throws IOException
    {
        File outfile = new File(dir, name + "Helper.java");

        // If we have a body (i.e. we've defined any pending_interface) and the 'more
        // recent check' is ok then write the file.
        if (hasBody && GlobalInputStream.isMoreRecentThan(outfile))
        {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));

            if (pack_name.length() > 0)
            {
                out.println("package " + pack_name + ";" + Environment.NL);
            }

            printClassComment("valuetype", name, out);

            out.println("public abstract class " + name + "Helper");
            out.println("{");

            out.println("\tprivate volatile static org.omg.CORBA.TypeCode _type = null;");

            // insert() / extract()

            out.println("\tpublic static void insert " +
                        "(org.omg.CORBA.Any a, " + javaName() + " v)");
            out.println("\t{");
            out.println("\t\ta.insert_Value (v, v._type());");
            out.println("\t}");
            out.println("\tpublic static " + javaName() + " extract " +
                        "(org.omg.CORBA.Any a)");
            out.println("\t{");
            out.println("\t\treturn (" + javaName() + ")a.extract_Value();");
            out.println("\t}");

            // type() / id()
            out.println("\tpublic static org.omg.CORBA.TypeCode type()");
            out.println("\t{");
            out.println("\t\tif (_type == null)");
            out.println("\t\t{");
            out.println("\t\t\tsynchronized(" + name + "Helper.class)");
            out.println("\t\t\t{");
            out.println("\t\t\t\tif (_type == null)");
            out.println("\t\t\t\t{");
            out.println("\t\t\t\t\t_type = " + getTypeCodeExpression() + ";");
            out.println("\t\t\t\t}");
            out.println("\t\t\t}");
            out.println("\t\t}");
            out.println("\t\treturn _type;");
            out.println("\t}" + Environment.NL);

            out.println("\tpublic static String id()");
            out.println("\t{");
            out.println("\t\treturn \"" + id() + "\";");
            out.println("\t}");

            // read() / write()

            out.println("\tpublic static " + javaName() + " read " +
                        "(org.omg.CORBA.portable.InputStream is)");
            out.println("\t{");
            out.println("\t\treturn (" + javaName() + ")((org.omg.CORBA_2_3.portable.InputStream)is).read_value (\"" + id() + "\");");
            out.println("\t}");

            out.println("\tpublic static void write " +
                        "(org.omg.CORBA.portable.OutputStream os, " +
                        javaName() + " val)");
            out.println("\t{");
            out.println("\t\t((org.omg.CORBA_2_3.portable.OutputStream)os)" +
                        ".write_value (val, \"" + id() + "\");");
            out.println("\t}");

            // factory methods

            for (Iterator i = factories.iterator(); i.hasNext();)
            {
                InitDecl d = (InitDecl)i.next();
                d.printHelperMethod (out, name);
            }

            out.println("}");
            out.close();
        }
    }

    private void printHolder(File dir) throws IOException
    {
        File outfile = new File(dir, name + "Holder.java");

        // If we have a body (i.e. we've defined any pending_interface) and the 'more
        // recent check' is ok then write the file.
        if (hasBody && GlobalInputStream.isMoreRecentThan(outfile))
        {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));

            if (pack_name.length() > 0)
            {
                out.println("package " + pack_name + ";" + Environment.NL);
            }

            printClassComment("valuetype", name, out);

            out.println("public" + parser.getFinalString() + " class " + name + "Holder");
            out.println("\timplements org.omg.CORBA.portable.Streamable");
            out.println("{");
            out.println("\tpublic " + javaName() + " value;");
            out.println("\tpublic " + name + "Holder () {}");
            out.println("\tpublic " + name + "Holder (final "
                        + javaName() + " initial)");
            out.println("\t{");
            out.println("\t\tvalue = initial;");
            out.println("\t}");
            out.println("\tpublic void _read " +
                        "(final org.omg.CORBA.portable.InputStream is)");
            out.println("\t{");
            out.println("\t\tvalue = " + javaName() + "Helper.read (is);");
            out.println("\t}");
            out.println("\tpublic void _write " +
                        "(final org.omg.CORBA.portable.OutputStream os)");
            out.println("\t{");
            out.println("\t\t" + javaName() + "Helper.write (os, value);");
            out.println("\t}");
            out.println("\tpublic org.omg.CORBA.TypeCode _type ()");
            out.println("\t{");
            out.println("\t\treturn value._type ();");
            out.println("\t}");
            out.println("}");
            out.close();
        }
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname) {
        ps.println( "\t\t" + anyname + ".insert_Value(" + varname + ", "+ varname +"._type());");
    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = (" + resulttype + ")" + anyname + ".extract_Value();");
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitValue(this);
    }
}
