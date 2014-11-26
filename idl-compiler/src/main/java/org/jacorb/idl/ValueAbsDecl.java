package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
import java.util.logging.Level;

/**
 * @author Andre Spiegel
 * @author Gerald Brose
 *
 * This class is basically the same as Interface.java, but we can't extend
 * that one because we have to extend Value, and delegating some parts and
 * not others is a nuisance...
 */
public class ValueAbsDecl
    extends Value
{
    ValueBody body = null;
    ValueInheritanceSpec inheritanceSpec;

    public ValueAbsDecl(int num)
    {
        super(num);
        pack_name = "";
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
        {
            pack_name = s + "." + pack_name;
        }
        else
        {
            pack_name = s;
        }

        if (body != null) // could've been a forward declaration)
        {
            body.setPackage(s); // a new scope!
        }

        if (inheritanceSpec != null)
        {
            inheritanceSpec.setPackage(s);
        }
    }

    public void setInheritanceSpec(ValueInheritanceSpec spec)
    {
        inheritanceSpec = spec;
    }

    public ValueInheritanceSpec setInheritanceSpec()
    {
        return inheritanceSpec;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public String typeName()
    {
        return full_name();
    }

    public Object clone()
    {
        throw new RuntimeException("Don't clone me, i am an interface!");
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() +
               " now: " + s.getClass().getName());
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        }
        enclosing_symbol = s;
    }

    public boolean basic()
    {
        return true;
    }

    public String holderName()
    {
        return javaName() + "Holder";
    }

    public String helperName() throws NoHelperException {
        throw new NoHelperException();
    }

    public String toString()
    {
        return getFullName(typeName());
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public void parse()
    {
        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            ScopedName.definePseudoScope(full_name());
            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (IllegalRedefinition ill)
        {
            parser.fatal_error("Cannot redefine " + token.str_val +
                    " in nested scope as " + ill.newDef, token);
        }
        catch (NameAlreadyDefined nad)
        {
            // if we get here, there is already a type spec for this interface
            // in the global type table for a forward declaration of this
            // interface. We must replace that table entry with this type spec
            // if this is not yet another forwad declaration

            if (body != null)
            {
                justAnotherOne = true;
            }

            if (!full_name().equals("org.omg.CORBA.TypeCode") && body != null)
            {
                TypeMap.replaceForwardDeclaration(full_name(), ctspec);
            }
        }

        if (body != null)
        {
            if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
            {
                if (parser.logger.isLoggable(Level.ALL))
                    parser.logger.log(Level.ALL, "Checking inheritanceSpec of " + full_name());
                for (Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                {
                    ScopedName name = (ScopedName)e.nextElement();

                    TypeSpec resolvedTSpec = name.resolvedTypeSpec();

                    // struct checking can be turned off to tolerate strange
                    // typedefs generated by rmic
                    if (parser.strict_inheritance)
                    {
                        // unwind any typedef's interface names
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
                                 resolvedTSpec.getClass() + ", name " + name);
                            }
                            parser.fatal_error("Illegal inheritance spec (not a constr. type): " +
                                           inheritanceSpec, token);
                        }

                        ConstrTypeSpec ts = (ConstrTypeSpec)resolvedTSpec;
                        if (!(ts.declaration() instanceof Interface) &&
                            !(ts.declaration() instanceof ValueAbsDecl))
                        {
                            parser.fatal_error("Illegal inheritance spec (not an intf. or abs. value type): " +
                                               inheritanceSpec, token);
                        }
                    }
                }
                body.set_ancestors(inheritanceSpec);
            }
            body.parse();
            NameTable.parsed_interfaces.put(full_name(), "");
        }
        else if (!justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }
    }

    ValueBody getBody()
    {
        if (parser.get_pending(full_name()) != null)
        {
            parser.fatal_error(full_name() + " is forward declared and still pending!", token);
        }
        else if (body == null)
        {
            if (((ValueAbsDecl)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec) != this)
            {
                body = ((ValueAbsDecl)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec).getBody();
            }
            if (body == null)
            {
                parser.fatal_error(full_name() + " still has an empty body!", token);
            }
        }
        return body;
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

        knownTypes.add(this);

        return "org.omg.CORBA.ORB.init().create_value_tc(\"" + id() +
            "\", \"" + name +
            "\", org.omg.CORBA.VM_ABSTRACT.value " +
            ", null, null)";
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

    public String printWriteStatement(String var_name, String streamname)
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
            + ".write_value (" + var_name + ");";
    }


    /**
     * generate the mapped class that extends ValueBase and has the
     * operations and attributes
     */

    public void print(PrintWriter unused)
    {
        if (included && !generateIncluded())
        {
            return;
        }

        // divert output into class files
        if (body != null) // forward declaration
        {
            try
            {
                // Java Interface file

                String path =
                    parser.out_dir + fileSeparator + pack_name.replace('.', fileSeparator);
                File dir = new File(path);
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        org.jacorb.idl.parser.fatal_error("Unable to create " + path, null);
                    }
                }

                File f = new File(dir, name + ".java");

                if (GlobalInputStream.isMoreRecentThan(f))
                {
                    PrintWriter ps = new PrintWriter(new java.io.FileWriter(f));

                    if (!pack_name.equals(""))
                        ps.println("package " + pack_name + ";" + Environment.NL);

                    printClassComment("abstract value type", name, ps);

                    // do we inherit from a class in the unnamed package?
                    // if so, we have to import this class explicitly

                    if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
                    {
                        Enumeration e = inheritanceSpec.v.elements();
                        for (; e.hasMoreElements();)
                        {
                            ScopedName sn = (ScopedName)e.nextElement();
                            if (sn.resolvedName().indexOf('.') < 0)
                            {
                                ps.println("import " + sn + "Operations;");
                            }
                        }
                    }
                    printImport(ps);

                    ps.println("public interface " + name);
                    ps.print("\textends org.omg.CORBA.portable.ValueBase ");

                    if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
                    {
                        for (Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                        {
                            ps.print(", " + e.nextElement());
                        }
                    }

                    ps.println(Environment.NL + "{");
                    if (body != null)
                    {
                        // forward declaration
                        body.printOperationSignatures(ps);
                    }
                    ps.println("}");
                    ps.close();
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
        ps.println( "\t\t" + anyname + ".insert_Value(" + varname + ", " + varname + "._type());");
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
