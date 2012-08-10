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

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class Interface
    extends TypeDeclaration
    implements Scope
{
    public InterfaceBody body = null;
    public SymbolList inheritanceSpec = null;

    private String[] ids = null;
    private boolean is_local = false;
    private boolean is_abstract = false;
    private ScopeData scopeData;
    private boolean parsed = false;

    private ReplyHandler replyHandler = null;

    /** IR information that would otherwise be lost */
    private Hashtable irInfoTable = new Hashtable();

    /** <code>abstractInterfaces</code> is to keep a record of those interfaces
     * that are abstract so any inheriting interface know what to inherit from.
     */
    protected static HashSet abstractInterfaces;


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

    public void setPackage(String s)
    {
        if (logger.isDebugEnabled())
            logger.debug("Interface setPackage " + s);

        s = parser.pack_replace(s);

        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        if (body != null)  // could've been a forward declaration)
            body.setPackage(s); // a new scope!

        if (inheritanceSpec != null)
            inheritanceSpec.setPackage(s);
    }

    public void set_abstract()
    {
        is_abstract = true;
    }


    /** override methods from superclass TypeDeclaration */

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
            if (logger.isDebugEnabled())
            {
                logger.debug("Illegal inheritance spec in Interface.unwindTypeDefs, not a constr. type but " +
                             resolvedTSpec.getClass() + ", name " + scopedName );
            }
            parser.fatal_error("Illegal inheritance spec in Interface.unwindTypeDefs (not a constr. type): " +
                               inheritanceSpec, token);
        }

        return (ConstrTypeSpec) resolvedTSpec;
    }


    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);

        enclosing_symbol = s;

        if (inheritanceSpec != null)
            inheritanceSpec.setEnclosingSymbol(s);
    }

    /**
     * set by the parser after creating this object depending
     * on the presence of the "local" modifier.
     */

    public void set_locality(boolean local)
    {
        this.is_local = local;
    }


    /**
     * <code>getTypeCodeExpression</code> produces a string for an expression
     * of type TypeCode that describes this type.
     * @return a string value.
     */

    public String getTypeCodeExpression()
    {
        if ((parser.generatedHelperPortability != parser.HELPER_DEPRECATED) && is_local)
        {
            return
            (
             "org.omg.CORBA.ORB.init().create_local_interface_tc(\"" +
             id() +
             "\", \"" +
             name +
             "\")"
             );
        }
        else if (is_abstract)
        {
            return
                (
                 "org.omg.CORBA.ORB.init().create_abstract_interface_tc(\"" +
                 id() +
                 "\", \"" +
                 name +
                 "\")"
                 );
        }
        return
        (
                "org.omg.CORBA.ORB.init().create_interface_tc(\"" +
                id() +
                "\", \"" +
                name +
                "\")"
        );
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        return this.getTypeCodeExpression();
    }


    public boolean basic()
    {
        return true;
    }


    public String holderName()
    {
        return toString() + "Holder";
    }

    public String helperName()
    {
        return toString() + "Helper";
    }

    public String toString()
    {
        return getFullName(typeName());
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public String printReadExpression(String Streamname)
    {
        return javaName() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement(String var_name, String Streamname)
    {
        return javaName() + "Helper.write(" + Streamname + "," + var_name + ");";
    }

    public void parse()
    {
        boolean justAnotherOne = false;

        if (parsed)
        {
            // there are occasions where the compiler may try to parse
            // an Interface type spec for a second time, viz if it is
            // referred to through a scoped name in another struct member.
            // that's not a problem, but we have to skip parsing again!
            // Fixes bug #629, copied from fix for bug #84
            return;
        }

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());

        if (is_abstract)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug
                    ("Adding " + full_name() + " to abstract interface list");
            }

            if (abstractInterfaces == null)
            {
                abstractInterfaces = new HashSet();
            }

            abstractInterfaces.add(full_name());
        }

        try
        {
            ScopedName.definePseudoScope(full_name());
            ctspec.c_type_spec = this;

            if (is_pseudo)
                NameTable.define(full_name(), IDLTypes.PSEUDO_INTERFACE);
            else
                NameTable.define(full_name(), IDLTypes.INTERFACE);

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
            // unless this is yet another forward declaration

            Object forwardDeclaration = parser.get_pending (full_name());

            if (forwardDeclaration != null)
            {
                if (! (forwardDeclaration instanceof Interface))
                {
                    parser.error("Forward declaration types mismatch for "
                                 + full_name()
                                 + ": name already defined with another type" , token);
                }

                if (body == null)
                {
                    justAnotherOne = true;
                }

                // else actual definition

                if ((!(full_name().equals("CORBA.TypeCode") ||
                        full_name().equals("org.omg.CORBA.TypeCode")))&&
                     body != null)
                {
                    TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                }
            }
            else
            {
                // this is another forward declaration, ignore
            }
        }

        if (body != null)
        {
            if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Checking inheritanceSpec of " + full_name());

                HashSet h = new HashSet();

                for (Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                {
                    ScopedName name = (ScopedName) e.nextElement();

                    ConstrTypeSpec ts = unwindTypedefs(name);

                    if (ts.declaration() instanceof Interface)
                    {
                        if (h.contains(ts.full_name()))
                        {
                            parser.fatal_error("Illegal inheritance spec: " +
                                               inheritanceSpec +
                                               " (repeated inheritance not allowed).",
                                               token);
                        }

                        // else:
                        h.add(ts.full_name());

                        continue;
                    }

                    // else:
                    parser.fatal_error("Illegal inheritance spec: " +
                                       inheritanceSpec + " (ancestor " +
                                       ts.full_name() + " not an interface)",
                                       token);
                }

                body.set_ancestors(inheritanceSpec);
            }

            body.parse();
            NameTable.parsed_interfaces.put(full_name(), "");

            if (parser.generate_ami_callback)
            {
                replyHandler = new ReplyHandler (this);
                replyHandler.parse();
            }

        }
        else if (!justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }

        parsed = true;
    }


    InterfaceBody getBody()
    {
        if (parser.get_pending(full_name()) != null)
        {
            parser.fatal_error(full_name() +
                               " is forward declared and still pending!",
                               token);
        }
        else if (body == null)
        {
            if (((Interface) ((ConstrTypeSpec) TypeMap.map(full_name())).c_type_spec) != this)
                body = ((Interface) ((ConstrTypeSpec) TypeMap.map(full_name())).c_type_spec).getBody();

            if (body == null)
                parser.fatal_error(full_name() + " still has an empty body!", token);
        }

        return body;
    }

    /**
     *  Open a PrintWriter to write to the .java file for typeName.
     *  @return null, if the output file already exists and is more
     *  recent than the input IDL file.
     */

    protected PrintWriter openOutput(String typeName)
    {
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

        try
        {
            final File f = new File(dir, typeName + ".java");
            if (GlobalInputStream.isMoreRecentThan(f))
            {
                PrintWriter ps = new PrintWriter(new java.io.FileWriter(f));
                return ps;
            }

            // no need to open file for printing, existing file is more
            // recent than IDL file.

            return null;
        }
        catch (IOException e)
        {
            throw new RuntimeException ("Could not open output file for "
                                        + typeName + " (" + e + ")");
        }
    }

    protected void printPackage (PrintWriter ps)
    {
        if (!pack_name.equals (""))
        {
            ps.println ("package " + pack_name + ";" + Environment.NL);
        }
    }


    /**
     *  If this interface inherits from classes in the unnamed package,
     *  generate explicit import statements for them.
     */
    protected void printSuperclassImports(PrintWriter ps)
    {
        if (inheritanceSpec.v.isEmpty())
        {
            return;
        }

        if ("".equals(pack_name))
        {
            return;
        }

        for (final Iterator i = inheritanceSpec.v.iterator(); i.hasNext();)
        {
            final ScopedName sn = (ScopedName) i.next();

            if (sn.resolvedName().indexOf('.') < 0)
            {
                ps.print("import ");
                ps.print(sn.toString());
                ps.println(';');
            }
        }
    }

    /**
     *  generate the signature interface
     */

    protected void printInterface()
    {
        PrintWriter ps = openOutput(name);
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printSuperclassImports(ps);
        printClassComment("interface", name, ps);

        if (is_pseudo)
        {
            ps.println("public abstract class " + name);

            if (inheritanceSpec.v.size() > 0)
            {
                StringBuffer pseudo_bases = new StringBuffer();
                StringBuffer regular_bases = new StringBuffer();
                String comma = " ";

                for (Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                {
                    ScopedName sn = ((ScopedName) e.nextElement());
                    String name = sn.resolvedName();

                    if (sn.is_pseudo())
                    {
                        pseudo_bases.append(comma + name);
                    }
                    else
                    {
                        regular_bases.append(comma + name);
                    }

                    if (inheritanceSpec.v.size() > 1)
                        comma = ",";
                }

                if (pseudo_bases.length() > 0)
                    ps.println("\textends " + pseudo_bases.toString());

                if (regular_bases.length() > 0)
                    ps.println("\timplements " + regular_bases.toString());

            }
        }
        else
        {
            ps.println("public interface " + name);

            if (is_abstract)
            {
                ps.print("\textends org.omg.CORBA.portable.IDLEntity");
            }
            else
            {
                ps.print("\textends " + name + "Operations");

                if (is_local)
                {
                    // Looking at RTF work it
                    // seems a new interface 'LocalInterface' will be used for this purpose.

                    ps.print(", org.omg.CORBA.LocalInterface, org.omg.CORBA.portable.IDLEntity");
                }
                else
                {
                    ps.print(", org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity");
                }
            }

            if (inheritanceSpec.v.size() > 0)
            {
                Enumeration e = inheritanceSpec.v.elements();

                while (e.hasMoreElements())
                {
                    ScopedName sne = (ScopedName) e.nextElement();
                    if (sne.resolvedTypeSpec() instanceof ReplyHandlerTypeSpec && parser.generate_ami_callback)
                    {
                        ps.print(", " + sne);
                    }
                    else
                    {
                        ConstrTypeSpec ts = unwindTypedefs(sne);
                        ps.print(", " + ts);
                    }
                }
            }
        }

        ps.println(Environment.NL + "{");

        if(is_pseudo)
        {
            printSerialVersionUID(ps);
        }

        // body can be null for forward declaration
        if (body != null)
        {
            body.printInterfaceMethods(ps);

            // for an abstract interface, the generated abstract class contains
            // the operation signatures since there is no separate signature
            // interface
            if (is_abstract)
            {
                body.printConstants(ps);
                body.printOperationSignatures(ps);
            }
        }

        ps.println("}");
        ps.close();
    }

    /**
     * generate the operations Java interface (not for pseudo interfaces)
     */

    protected void printOperations()
    {
        PrintWriter ps = openOutput(name + "Operations");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printSuperclassImports(ps);
        printImport(ps);
        printClassComment("interface", name, ps);

        ps.println("public interface " + name + "Operations");

        if (inheritanceSpec.v.size() > 0)
        {
            ps.print("\textends ");
            Enumeration e = inheritanceSpec.v.elements();

            do
            {
                ScopedName sne = (ScopedName) e.nextElement();

                // See description of abstractInterfaces for logic here.
                if (abstractInterfaces != null &&
                    abstractInterfaces.contains(sne.toString()))
                {
                    ps.print(sne);
                }
                else
                {
                    if (sne.resolvedTypeSpec() instanceof ReplyHandlerTypeSpec && parser.generate_ami_callback)
                    {
                        ps.print(sne + "Operations");
                    }
                    else
                    {
                        ConstrTypeSpec ts = unwindTypedefs(sne);
                        ps.print(ts + "Operations");
                    }
                }

                if (e.hasMoreElements())
                {
                    ps.print(" , ");
                }
            }
            while (e.hasMoreElements());

            ps.print(Environment.NL);
        }

        ps.println("{");

        if (body != null)
        {
            // forward declaration
            body.printConstants(ps);
            body.printOperationSignatures(ps);
        }

        ps.println("}");
        ps.close();
    }


    /**
     * Print the holder class for the interface.
     */
    protected void printHolder()
    {
        PrintWriter ps = openOutput(name + "Holder");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printClassComment("interface", name, ps);

        ps.print("public" + parser.getFinalString() + " class " + name + "Holder");
        ps.print("\timplements org.omg.CORBA.portable.Streamable");

        ps.println("{");
        ps.println("\t public " + name + " value;");

        ps.println("\tpublic " + name + "Holder()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + name + "Holder (final " + name + " initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type()");
        ps.println("\t{");
        ps.println("\t\treturn " + name + "Helper.type();");
        ps.println("\t}");

        ps.println("\tpublic void _read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + name + "Helper.read (in);");
        ps.println("\t}");

        ps.println("\tpublic void _write (final org.omg.CORBA.portable.OutputStream _out)");
        ps.println("\t{");
        ps.println("\t\t" + name + "Helper.write (_out,value);");
        ps.println("\t}");

        ps.println("}");
        ps.close();
    }

    /**
     * Generates a narrow method for the Helper class.
     * @param ps the PrintWriter to which the method will be written
     * @param checked determines whether an ordinary narrow() method or an
     *                unchecked_narrow() method should be generated
     * @param forCorbaObject determines whether the parameter type of the
     *                       narrow method is org.omg.CORBA.Object or
     *                       java.lang.Object
     */
    protected void printNarrow (PrintWriter ps,
                                boolean checked,
                                boolean forCorbaObject)
    {
        ps.print("\tpublic static " + typeName());
        ps.print(checked ? " narrow" : " unchecked_narrow");
        ps.println(forCorbaObject ? "(final org.omg.CORBA.Object obj)"
                                  : "(final java.lang.Object obj)");
        ps.println("\t{");
        ps.println("\t\tif (obj == null)");
        ps.println("\t\t{");
        ps.println("\t\t\treturn null;");
        ps.println("\t\t}");
        ps.println("\t\telse if (obj instanceof " + typeName() + ")");
        ps.println("\t\t{");
        ps.println("\t\t\treturn (" + typeName() + ")obj;");
        ps.println("\t\t}");

        if (parser.generate_stubs && !is_local)
        {
            if (checked && forCorbaObject)
            {
                ps.println("\t\telse if (obj._is_a(\"" + id() + "\"))");
                printStubInterposition(ps);
                printElseNarrowFailed(ps);
            }
            else if (!checked && forCorbaObject)
            {
                ps.println("\t\telse");
                printStubInterposition(ps);
            }
            else if (checked && !forCorbaObject)
            {
                ps.println("\t\telse if (obj instanceof org.omg.CORBA.Object &&");
                ps.println("\t\t         ((org.omg.CORBA.Object)obj)._is_a(\"" + id() + "\"))");
                printStubInterposition(ps);
                printElseNarrowFailed(ps);
            }
            else if (!checked && !forCorbaObject)
            {
                ps.println("\t\tif (obj instanceof org.omg.CORBA.Object)");
                printStubInterposition(ps);
                printElseNarrowFailed(ps);
            }
        }
        else
        {
            printElseNarrowFailed (ps);
        }
        ps.println("\t}");
    }

    /**
     * Generates the code for a narrow method with which a stub is inserted
     * between an object implementation and the client.
     */
    protected void printStubInterposition (PrintWriter ps)
    {
        final String stub_name = stubName (typeName());
        ps.println("\t\t{");
        ps.println("\t\t\t" + stub_name + " stub;");
        ps.println("\t\t\tstub = new " + stub_name + "();");
        ps.println("\t\t\tstub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
        ps.println("\t\t\treturn stub;");
        ps.println("\t\t}");
    }

    /**
     * Prints the else clause of a narrow method that signals general failure.
     */
    protected void printElseNarrowFailed (PrintWriter ps)
    {
        ps.println("\t\telse");
        ps.println("\t\t{");
        ps.println("\t\t\tthrow new org.omg.CORBA.BAD_PARAM(\"Narrow failed\");");
        ps.println("\t\t}");
    }

    /**
     * Generate the helper class for an interface
     */
    protected void printHelper()
    {
        PrintWriter ps = openOutput(name + "Helper");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printImport(ps);

        printClassComment("interface", name, ps);

        ps.println("public" + parser.getFinalString() + " class " + name + "Helper");
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

        // Generate insert (handle either CORBA.Object or Serializable case)
        ps.println("\tpublic static void insert (final org.omg.CORBA.Any any, final " + typeName() + " s)");
        ps.println("\t{");

        if (is_abstract)
        {
            ps.println("\t\tif (s instanceof org.omg.CORBA.Object)");
            ps.println("\t\t{");
            ps.println("\t\t\tany.insert_Object((org.omg.CORBA.Object)s);");
            ps.println("\t\t}");
            ps.println("\t\telse if (s instanceof java.io.Serializable)");
            ps.println("\t\t{");
            ps.println("\t\t\tany.insert_Value((java.io.Serializable)s);");
            ps.println("\t\t}");
            ps.println("\t\telse");
            ps.println("\t\t{");
            ps.println("\t\t\tthrow new org.omg.CORBA.BAD_PARAM(\"Failed to insert in helper\");");
            ps.println("\t\t}");
        }
        else
        {
            ps.println("\t\t\tany.insert_Object(s);");
        }

        ps.println("\t}");

        // Generate extract
        ps.println("\tpublic static " + typeName() + " extract(final org.omg.CORBA.Any any)");
        ps.println("\t{");

        if (is_abstract)
        {
            ps.println("\t\ttry");
            ps.println("\t\t{");
            ps.println("\t\torg.omg.CORBA.Object __o = any.extract_Object();" + Environment.NL);
            ps.println("\t\t" + name + " __r = narrow(__o);" + Environment.NL);
            ps.println("\t\tif (__o != null && __o != __r)");
            ps.println("\t\t{");
            ps.println("\t\t\t((org.omg.CORBA.portable.ObjectImpl)__o)._set_delegate(null);" + Environment.NL);
            ps.println("\t\t}");
            ps.println("\t\treturn __r;");
            ps.println("\t\t}");
            ps.println("\t\tcatch (org.omg.CORBA.BAD_OPERATION ex)");
            ps.println("\t\t{");
            ps.println("\t\t\ttry");
            ps.println("\t\t\t{");
            ps.println("\t\t\t\treturn (" + typeName() + ")any.extract_Value();");
            ps.println("\t\t\t}");
            ps.println("\t\t\tcatch (ClassCastException e)");
            ps.println("\t\t\t{");
            ps.println("\t\t\t\tthrow new org.omg.CORBA.MARSHAL(e.getMessage());");
            ps.println("\t\t\t}");
            ps.println("\t\t}");
        }
        else
        {
            if( parser.useUncheckedNarrow )
            {
                ps.println( "\t\treturn unchecked_narrow(any.extract_Object());");
            }
            else
            {
                ps.println("\t\treturn narrow(any.extract_Object()) ;");
            }
        }

        ps.println("\t}");

        printIdMethod(ps);

        // Generate the read
        ps.println("\tpublic static " + name + " read(final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");

        if (is_local)
        {
            ps.println("\t\tthrow new org.omg.CORBA.MARSHAL();");
        }
        else
        {
            if (is_abstract)
            {
                ps.println("\t\treturn narrow(((org.omg.CORBA_2_3.portable.InputStream)in).read_abstract_interface());");
            }
            else
            {
                final String stubClass =
                    parser.generate_stubs ? stubName(typeName()) + ".class" : "";
                if( parser.useUncheckedNarrow )
                {
                    ps.println( "\t\treturn unchecked_narrow(in.read_Object(" + stubClass + "));" );
                }
                else
                {
                    ps.println("\t\treturn narrow(in.read_Object(" + stubClass + "));");
                }
            }
        }

        ps.println("\t}");

        // Generate the write
        ps.println("\tpublic static void write(final org.omg.CORBA.portable.OutputStream _out, final " + typeName() + " s)");
        ps.println("\t{");

        if (is_local)
        {
            ps.println("\t\tthrow new org.omg.CORBA.MARSHAL();");
        }
        else
        {
            if (is_abstract)
            {
                ps.println("\t\t((org.omg.CORBA_2_3.portable.OutputStream)_out).write_abstract_interface(s);");
            }
            else
            {
                ps.println("\t\t_out.write_Object(s);");
            }
        }

        ps.println("\t}");

        // Generate narrow methods (cf. Java Mapping 1.2, sect. 1.5.2)
        if (is_abstract)
        {
            printNarrow (ps, true,  false); // checked, java.lang.Object
            printNarrow (ps, false, false); // unchecked, java.lang.Object
        }
        else if (hasAbstractBase())
        {
            printNarrow (ps, true,  true);  // checked, CORBA Object
            printNarrow (ps, true,  false); // checked, java.lang.Object
            printNarrow (ps, false, true);  // unchecked, CORBA Object
            printNarrow (ps, false, false); // unchecked, java.lang.Object
        }
        else
        {
            printNarrow (ps, true,  true);  // checked, CORBA Object
            printNarrow (ps, false, true);  // unchecked, CORBA Object
        }

        ps.println("}");
        ps.close();
    }

    public String[] get_ids()
    {
        if (ids == null)
        {
            Set base_ids = new HashSet();

            if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
            {
                for (Iterator i = inheritanceSpec.v.iterator(); i.hasNext();)
                {
                    TypeSpec ts = ((ScopedName) i.next()).resolvedTypeSpec();

                    if (ts instanceof ConstrTypeSpec)
                    {
                        Interface base = (Interface) ((ConstrTypeSpec) ts).c_type_spec;
                        base_ids.addAll (Arrays.asList (base.get_ids()));
                    }
                    else if (ts instanceof ReplyHandlerTypeSpec)
                    {
                        base_ids.add("IDL:omg.org/Messaging/ReplyHandler:1.0");
                    }
                }
            }

            ids = new String[ base_ids.size() + 1 ];
            ids[ 0 ] = id();
            int i = 1;

            for (Iterator j = base_ids.iterator(); j.hasNext(); i++)
            {
                ids[ i ] = (String) j.next();
            }
        }

        return ids;
    }

    /**
     * Returns true if this interface has at least one abstract base type.
     */
    protected boolean hasAbstractBase()
    {
        if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
        {
            for (Iterator i = inheritanceSpec.v.iterator(); i.hasNext();)
            {
                TypeSpec ts = ((ScopedName) i.next()).resolvedTypeSpec();
                if (ts instanceof ConstrTypeSpec)
                {
                    Interface base = (Interface) ((ConstrTypeSpec) ts).c_type_spec;
                    if (base.is_abstract || base.hasAbstractBase())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generates a stub class for this Interface
     */

    protected void printStub()
    {
        PrintWriter ps = openOutput("_" + name + "Stub");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printImport(ps);
        printClassComment("interface", name, ps);

        ps.println("public class _" + name + "Stub");
        ps.println("\textends org.omg.CORBA.portable.ObjectImpl");

        ps.println("\timplements " + javaName());
        ps.println("{");

        printSerialVersionUID(ps);

        ps.print("\tprivate String[] ids = {");
        String[] ids = get_ids();
        for (int i = 0; i < ids.length - 1; i++)
            ps.print("\"" + ids[ i ] + "\",");

        ps.println("\"" + ids[ ids.length - 1 ] + "\"};");
        ps.println("\tpublic String[] _ids()");
        ps.println("\t{");
        ps.println("\t\treturn ids;");
        ps.println("\t}" + Environment.NL);

        if (!parser.cldc10)
        {
            ps.print("\tpublic final static java.lang.Class _opsClass = ");
            if (!pack_name.equals(""))
            {
                ps.print(pack_name + ".");
            }
            if (is_abstract)
            {
                ps.println(name + ".class;");
            }
            else
            {
                ps.println(name + "Operations.class;");
            }
        }
        else
        {
            // code supplied byte Nokia for CLDC10 compatibility
            // avoids use of the static .class variable.
            String fullName = null;
            if(!pack_name.equals(""))
            {
                fullName = pack_name + "." + name;
            }
            else
            {
                fullName = name;
            }

            ps.println( "\tpublic static final java.lang.Class _opsClass;" );
            ps.println("\tstatic");
            ps.println("\t{");
            ps.println("\t\ttry");  //try
            ps.println("\t\t{");    //{

            ps.print( "\t\t\t_opsClass = Class.forName(\"" );
            if( !pack_name.equals( "" ) ) ps.print( pack_name + "." );
            ps.println( name + "Operations\");" );

            ps.println("\t\t}");    //}
            ps.println("\t\tcatch(ClassNotFoundException cnfe)");
            ps.println("\t\t{");    //{
            ps.println("\t\t\tthrow new RuntimeException(\"Class " + fullName + " was not found.\");");
            ps.println("\t\t}");    //}
            ps.println("\t}" + Environment.NL);
        }

        body.printStubMethods(ps, name, is_local, is_abstract);

        ps.println("}");
        ps.close();
    }

    protected void printImplSkeleton()
    {
        PrintWriter ps = openOutput(name + "POA");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printImport(ps);
        printClassComment("interface", name, ps);

        ps.print("public abstract class " + name + "POA" + Environment.NL);
        ps.println("\textends org.omg.PortableServer.Servant");
        ps.println("\timplements org.omg.CORBA.portable.InvokeHandler, " + javaName() + "Operations");
        ps.println("{");

        body.printOperationsHash(ps);

        ps.print("\tprivate String[] ids = {");
        String[] ids = get_ids();

        for (int i = 0; i < ids.length - 1; i++)
        {
            ps.print("\"" + ids[ i ] + "\",");
        }

        ps.println("\"" + ids[ ids.length - 1 ] + "\"};");


        ps.println("\tpublic " + javaName() + " _this()");

        ps.println("\t{");

        ps.println("\t\t" + "org.omg.CORBA.Object __o = _this_object() ;" ) ;
        ps.println("\t\t" + javaName() + " __r = " + javaName() + "Helper.narrow(__o);");
        ps.println("\t\tif (__o != null && __o != __r)");
        ps.println("\t\t{");
        ps.println("\t\t\t((org.omg.CORBA.portable.ObjectImpl)__o)._set_delegate(null);\n");
        ps.println("\t\t}");
        ps.println("\t\treturn __r;");

        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this(org.omg.CORBA.ORB orb)");

        ps.println("\t{");

        ps.println("\t\t" + "org.omg.CORBA.Object __o = _this_object(orb) ;" ) ;
        ps.println("\t\t" + javaName() + " __r = " + javaName() + "Helper.narrow(__o);");
        ps.println("\t\tif (__o != null && __o != __r)");
        ps.println("\t\t{");
        ps.println("\t\t\t((org.omg.CORBA.portable.ObjectImpl)__o)._set_delegate(null);\n");
        ps.println("\t\t}");
        ps.println("\t\treturn __r;");

        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)");

        ps.println("\t\tthrows org.omg.CORBA.SystemException");

        ps.println("\t{");

        if (body.getMethods().length > 0)
        {
            ps.println("\t\torg.omg.CORBA.portable.OutputStream _out = null;");

            ps.println("\t\t// do something");
        }

        body.printSkelInvocations(ps);

        ps.println("\t}" + Environment.NL);

        ps.println("\tpublic String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)");

        ps.println("\t{");

        ps.println("\t\treturn ids;");

        ps.println("\t}");

        ps.println("}");

        ps.close();
    }

    /**
     * print the stream-based skeleton class
     */

    protected void printTieSkeleton()
    {
        PrintWriter ps = openOutput(name + "POATie");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        ps.println("import org.omg.PortableServer.POA;");
        printImport(ps);

        printClassComment("interface", name, ps);

        ps.println("public class " + name + "POATie");
        ps.println("\textends " + name + "POA");
        ps.println("{");

        ps.println("\tprivate " + name + "Operations _delegate;" + Environment.NL);
        ps.println("\tprivate POA _poa;");

        ps.println("\tpublic " + name + "POATie(" + name + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        ps.println("\tpublic " + name + "POATie(" + name + "Operations delegate, POA poa)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t\t_poa = poa;");
        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this()");
        ps.println("\t{");
        ps.println("\t\t" + "org.omg.CORBA.Object __o = _this_object() ;" ) ;
        ps.println("\t\t" + javaName() + " __r = " + javaName() + "Helper.narrow(__o);");
        ps.println("\t\tif (__o != null && __o != __r)");
        ps.println("\t\t{");
        ps.println("\t\t\t((org.omg.CORBA.portable.ObjectImpl)__o)._set_delegate(null);\n");
        ps.println("\t\t}");
        ps.println("\t\treturn __r;");
        ps.println("\t}");

        ps.println("\tpublic " + javaName() + " _this(org.omg.CORBA.ORB orb)");
        ps.println("\t{");
        ps.println("\t\t" + "org.omg.CORBA.Object __o = _this_object(orb) ;" ) ;
        ps.println("\t\t" + javaName() + " __r = " + javaName() + "Helper.narrow(__o);");
        ps.println("\t\tif (__o != null && __o != __r)");
        ps.println("\t\t{");
        ps.println("\t\t\t((org.omg.CORBA.portable.ObjectImpl)__o)._set_delegate(null);\n");
        ps.println("\t\t}");
        ps.println("\t\treturn __r;");
        ps.println("\t}");

        ps.println("\tpublic " + name + "Operations _delegate()");
        ps.println("\t{");
        ps.println("\t\treturn _delegate;");
        ps.println("\t}");

        ps.println("\tpublic void _delegate(" + name + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        ps.println("\tpublic POA _default_POA()");
        ps.println("\t{");
        ps.println("\t\tif (_poa != null)");
        ps.println("\t\t{");
        ps.println("\t\t\treturn _poa;");
        ps.println("\t\t}");
        ps.println("\t\treturn super._default_POA();");
        ps.println("\t}");

        body.printDelegatedMethods(ps);
        ps.println("}");
        ps.close();
    }

    protected void printIRHelper()
    {
        PrintWriter ps = openOutput(name + "IRHelper");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        ps.println(Environment.NL + "/**");
        ps.println(" * This class contains generated Interface Repository information.");
        ps.println(" * @author JacORB IDL compiler.");
        ps.println(" */" + Environment.NL);

        ps.println("public class " + name + "IRHelper");
        ps.println("{");

        String HASHTABLE = System.getProperty ("java.version").startsWith ("1.1")
            ? "com.sun.java.util.collections.Hashtable"
            : "java.util.Hashtable";

        ps.println("\tpublic static " + HASHTABLE
                   + " irInfo = new " + HASHTABLE + "();");
        ps.println("\tstatic");
        ps.println("\t{");
        body.getIRInfo(irInfoTable);

        for (Enumeration e = irInfoTable.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            ps.println("\t\tirInfo.put(\"" + key + "\", \"" + (String) irInfoTable.get(key) + "\");");
        }

        ps.println("\t}");
        ps.println("}");
        ps.close();
    }

    protected void printLocalBase()
    {
        PrintWriter ps = openOutput("_" + name + "LocalBase");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        ps.println(Environment.NL +"/**");
        ps.println(" * Abstract base class for implementations of local interface " + name);
        ps.println(" * @author JacORB IDL compiler.");
        ps.println(" */" + Environment.NL);

        ps.println("public abstract class _" + name + "LocalBase");
        ps.println("\textends org.omg.CORBA.LocalObject");
        ps.println("\timplements " + name);
        ps.println("{");

        printSerialVersionUID(ps);

        ps.print("\tprivate String[] _type_ids = {");
        String[] ids = get_ids();

        for (int i = 0; i < ids.length - 1; i++)
            ps.print("\"" + ids[ i ] + "\",");

        ps.println("\"" + ids[ ids.length - 1 ] + "\"};");

        ps.print("\tpublic String[] _ids()");

        ps.println("\t{");

        ps.println("\t\treturn(String[])_type_ids.clone();");

        ps.println("\t}");

        ps.println("}");

        ps.close();
    }


    protected void printLocalTie()
    {
        PrintWriter ps = openOutput(name + "LocalTie");
        if (ps == null)
        {
            return;
        }

        printPackage(ps);
        printImport(ps);

        printClassComment("interface", name, ps);

        ps.println("public class " + name + "LocalTie");
        ps.println("\textends _" + name + "LocalBase");
        ps.println("{");

        printSerialVersionUID(ps);

        ps.println("\tprivate " + name + "Operations _delegate;" + Environment.NL);

        ps.println("\tpublic " + name + "LocalTie(" + name + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        ps.println("\tpublic " + name + "Operations _delegate()");
        ps.println("\t{");
        ps.println("\t\treturn _delegate;");
        ps.println("\t}");

        ps.println("\tpublic void _delegate(" + name + "Operations delegate)");
        ps.println("\t{");
        ps.println("\t\t_delegate = delegate;");
        ps.println("\t}");

        body.printDelegatedMethods(ps);
        ps.println("}");
        ps.close();
    }



    public void print(PrintWriter _ps)
    {
        if (included && !generateIncluded())
            return ;

        // divert output into individual .java files
        if (body != null)  // forward declaration
        {
            printInterface();

            if (!is_pseudo)
            {
                if (!is_abstract)
                {
                    printOperations();
                }

                printHelper();
                printHolder();

                if (parser.generate_stubs && !is_local)
                {
                    printStub();
                }

                if (parser.generate_skeletons && !is_local && !is_abstract)
                {
                    printImplSkeleton();
                    printTieSkeleton();
                }

                if (parser.generateIR)
                {
                    printIRHelper();
                }

                if (is_local)
                {
                    printLocalBase();
                    printLocalTie();
                }
            }

            // print class files for interface local definitions
            body.print(null);

            if (replyHandler != null)
                replyHandler.print (_ps);
        }
    }


    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        if (is_abstract)
        {
            throw new RuntimeException("DII stubs not yet implemented for abstract interfaces");
            /*
            ps.println("\t\tif (s instanceof org.omg.CORBA.Object)");
            ps.println("\t\t{");
            ps.println("\t\t\tany.insert_Object((org.omg.CORBA.Object)s);");
            ps.println("\t\t}");
            ps.println("\t\telse if (s instanceof java.io.Serializable)");
            ps.println("\t\t{");
            ps.println("\t\t\tany.insert_Value((java.io.Serializable)s);");
            ps.println("\t\t}");
            ps.println("\t\telse");
            ps.println("\t\t{");
            ps.println("\t\t\tthrow new org.omg.CORBA.BAD_PARAM(\"Failed to insert in helper\");");
            ps.println("\t\t}"); */
        }

        ps.println( "\t\t" + anyname + ".insert_Object(" + varname + ");");
    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype) {
        ps.println("\t\t" + resultname + " = " + helperName() + ".extract(" + anyname + ");" );
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitInterface(this);
    }

    private static final String stubName(final String typeName)
    {
        final String stub_name;
        if (typeName.indexOf('.') > -1)
        {
            stub_name = typeName.substring(0, typeName.lastIndexOf('.')) +
                "._" + typeName.substring(typeName.lastIndexOf('.') + 1) +
                "Stub";
        }
        else
        {
            stub_name = "_" + typeName + "Stub";
        }

        return stub_name;
    }
}
