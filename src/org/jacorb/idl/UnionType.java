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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Set;

/**
 * @author Gerald Brose
 */
public class UnionType
    extends TypeDeclaration
    implements Scope
{
    /** the union's discriminator's type spec */
    TypeSpec switch_type_spec;

    SwitchBody switch_body;
    private boolean written = false;

    private ScopeData scopeData;

    private boolean allCasesCovered = false;
    private boolean switch_is_enum = false;
    private boolean switch_is_bool = false;
    private boolean switch_is_longlong = false;
    private boolean explicit_default_case = false;
    private boolean isParsed = false;
    private int labels;

    public UnionType(int num)
    {
        super(num);
        pack_name = "";
    }

    public Object clone()
    {
        UnionType ut = new UnionType(new_num());
        ut.switch_type_spec = this.switch_type_spec;
        ut.switch_body = switch_body;
        ut.pack_name = this.pack_name;
        ut.name = this.name;
        ut.written = this.written;
        ut.scopeData = this.scopeData;
        ut.enclosing_symbol = this.enclosing_symbol;
        ut.token = this.token;
        return ut;
    }

    public void setScopeData(ScopeData data)
    {
        scopeData = data;
    }

    public ScopeData getScopeData()
    {
        return scopeData;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
        if (switch_body != null)
        {
            switch_body.setEnclosingSymbol(s);
        }
    }

    public String typeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
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
        return typeName() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return typeName() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String helperName()
    {
        return typeName() + "Helper";
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public void setSwitchType(TypeSpec s)
    {
        switch_type_spec = s;
    }

    public void setSwitchBody(SwitchBody sb)
    {
        switch_body = sb;
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
        if (switch_type_spec != null)
        {
            switch_type_spec.setPackage (s);
        }
        if (switch_body != null)
        {
            switch_body.setPackage(s);
        }
    }

    public boolean basic()
    {
        return false;
    }

    public void parse()
    {
        if (isParsed)
        {
            // there are occasions where the compiler may try to parse
            // a union type spec for a second time, viz if the union is
            // referred to through a scoped name in another struct member.
            // that's not a problem, but we have to skip parsing again!

            return;
        }

        isParsed = true;

        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            ScopedName.definePseudoScope(full_name());
            ctspec.c_type_spec = this;
            NameTable.define(full_name(), IDLTypes.TYPE_UNION);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            Object forwardDeclaration = parser.get_pending (full_name ());
            if (forwardDeclaration != null)
            {
                if (! (forwardDeclaration instanceof UnionType))
                {
                    parser.error("Forward declaration types mismatch for "
                            + full_name()
                            + ": name already defined with another type" , token);
                }

                if (switch_type_spec == null)
                {
                    justAnotherOne = true;
                }
                // else actual definition

                if (!full_name().equals("org.omg.CORBA.TypeCode") && switch_type_spec != null)
                {
                    TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                }
            }
            else
            {
                parser.error("Union " + full_name() + " already defined", token);
            }
        }

        if (switch_type_spec != null)
        {
            // Resolve scoped names and aliases
            TypeSpec ts;
            if (switch_type_spec.type_spec instanceof ScopedName)
            {
                ts = ((ScopedName)switch_type_spec.type_spec).resolvedTypeSpec();

                while(ts instanceof ScopedName || ts instanceof AliasTypeSpec)
                {
                    if (ts instanceof ScopedName)
                    {
                        ts = ((ScopedName)ts).resolvedTypeSpec();
                    }
                    else
                    {
                        ts = ((AliasTypeSpec)ts).originalType();
                    }
                }
                addImportedName(switch_type_spec.typeName());
            }
            else
            {
                ts = switch_type_spec.type_spec;
            }

            // Check if we have a valid discriminator type

            if
                (!(
                   ((ts instanceof SwitchTypeSpec) &&
                    (((SwitchTypeSpec)ts).isSwitchable()))
                   ||
                   ((ts instanceof BaseType) &&
                    (((BaseType)ts).isSwitchType()))
                   ||
                   ((ts instanceof ConstrTypeSpec) &&
                    (((ConstrTypeSpec)ts).c_type_spec instanceof EnumType))
                   ))
            {
                parser.error("Illegal Switch Type: " + ts.typeName(), token);
            }

            switch_type_spec.parse();
            switch_body.setTypeSpec(switch_type_spec);
            switch_body.setUnion(this);

            ScopedName.addRecursionScope(typeName());
            switch_body.parse();
            ScopedName.removeRecursionScope(typeName());

            NameTable.parsed_interfaces.put(full_name(), "");
            parser.remove_pending(full_name());
        }
        else if (!justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }
    }

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     */
    public String getTypeCodeExpression()
    {
        return typeName() + "Helper.type()";
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }
        return this.getTypeCodeExpression();
    }

    private void printUnionClass(String className, PrintWriter pw)
    {
        Enumeration e;

        if (!pack_name.equals(""))
        {
            pw.println("package " + pack_name + ";");
        }

        printImport(pw);

        printClassComment("union", className, pw);

        pw.println("public" + parser.getFinalString() + " class " + className);
        pw.println("\timplements org.omg.CORBA.portable.IDLEntity");
        pw.println("{");

        TypeSpec ts = switch_type_spec.typeSpec();

        while(ts instanceof ScopedName || ts instanceof AliasTypeSpec)
        {
            if (ts instanceof ScopedName)
            {
                ts = ((ScopedName)ts).resolvedTypeSpec();
            }
            if (ts instanceof AliasTypeSpec)
            {
                ts = ((AliasTypeSpec)ts).originalType();
            }
        }

        pw.println("\tprivate " + ts.typeName() + " discriminator;");

        /* find a "default" value */

        String defaultStr = "";

        /* start by concatenating all case label lists into one list
         * (this list is used only for finding a default)
         */

        int def = 0;
        java.util.Vector allCaseLabels = new java.util.Vector ();
        java.util.Vector unusedCaseLabels = new java.util.Vector ();

        e = switch_body.caseListVector.elements();
        while (e.hasMoreElements())
        {
            Case c = (Case)e.nextElement();
            for (int i = 0; i < c.case_label_list.v.size(); i++)
            {
                labels++; // the overall number of labels is needed in a number of places...
                Object ce = c.case_label_list.v.elementAt(i);
                if (ce != null)
                {
                    if (ce instanceof ConstExpr)
                    {
                        allCaseLabels.addElement(((ConstExpr)ce).toString());
                    }
                    else
                    {
                        // this is a scoped name
                        allCaseLabels.addElement
                            (ScopedName.unPseudoName (((ScopedName)ce).resolvedName()));
                    }
                }
                else
                {
                    def = 1;
                    explicit_default_case = true;
                }
            }
        }

        /* if switch type is an enum, the default is null */

        if ((ts instanceof ConstrTypeSpec &&
             ((ConstrTypeSpec)ts).declaration() instanceof EnumType))
        {
            this.switch_is_enum = true;
            EnumType et = (EnumType)((ConstrTypeSpec)ts).declaration();

            if (allCaseLabels.size() + def > et.size())
            {
                lexer.emit_warn("Too many case labels in definition of union " +
                                full_name() + ", default cannot apply", token);
            }
            if (allCaseLabels.size() + def == et.size())
            {
                allCasesCovered = true;
            }

            for (int i = 0; i < et.size(); i++)
            {
                String qualifiedCaseLabel =
                    ts.typeName() + "." + (String)et.enumlist.v.elementAt(i);
                if (!(allCaseLabels.contains(qualifiedCaseLabel)))
                {
                    // Set default value to first unused case label
                    if (defaultStr.length () == 0)
                    {
                        defaultStr = qualifiedCaseLabel;
                    }
                    unusedCaseLabels.addElement (qualifiedCaseLabel);
                }
            }
        }
        else
        {
            if (ts instanceof BaseType)
            {
                ts = ((BaseType)ts).typeSpec();
            }

            if (ts instanceof BooleanType)
            {
                this.switch_is_bool = true;

                // find a "default" for boolean

                if (allCaseLabels.size() + def > 2)
                {
                    parser.error("Case label error: too many default labels.", token);
                    return;
                }
                else if (allCaseLabels.size() == 1)
                {
                    if (((String)allCaseLabels.elementAt(0)).equals("true"))
                    {
                        defaultStr = "false";
                    }
                    else
                    {
                        defaultStr = "true";
                    }
                }
                else
                {
                    // labels for both true and false -> no default possible
                }
            }
            else if (ts instanceof CharType)
            {
                // find a "default" for char

                Enumeration enumeration;
                String charStr;
                boolean matched = false;
                short val;

                // Iterate through values until find on that is not a case value

                for (short s = 0; s < 256; s++)
                {
                    matched = false;
                    enumeration = allCaseLabels.elements ();
                    while (enumeration.hasMoreElements ())
                    {
                        charStr = (String) enumeration.nextElement ();

                        // Remove quotes from char string 'x'

                        charStr = charStr.substring (1, charStr.length () - 1);

                        // Check if escaped value

                        if (charStr.charAt (0) == '\\')
                        {
                            charStr = charStr.substring (1);
                            val = Short.parseShort (charStr);
                        }
                        else
                        {
                            val = (short) charStr.charAt (0);
                        }

                        if (s == val)
                        {
                            matched = true;
                            break;
                        }
                        continue;
                    }

                    // Current value does not match a case value so set as default

                    if (! matched)
                    {
                        defaultStr = "(char)" + s;
                        break;
                    }
                }
            }
            else if (ts instanceof IntType)
            {
                int maxint = 65536; // 2^16, max short
                if (ts instanceof LongType)
                {
                    maxint = 2147483647; // -2^31,  max long
                }

                for (int i = 0; i < maxint; i++)
                {
                    if (!(allCaseLabels.contains(String.valueOf(i))))
                    {
                        defaultStr = Integer.toString(i);
                        break;
                    }
                }
                if (ts instanceof LongLongType)
                {
                    this.switch_is_longlong = true;
                }
            }
            else
            {
                logger.error("Something went wrong in UnionType, "
                             + "could not identify switch type "
                             + switch_type_spec.type_spec);
            }
        }

        /* print members */

        e = switch_body.caseListVector.elements();
        while (e.hasMoreElements())
        {
            Case c = (Case)e.nextElement();
            pw.println("\tprivate " + c.element_spec.typeSpec.typeName()
                       + " " + c.element_spec.declarator.name() + ";");
        }

        /*
         * print a constructor for class member initialization
         */

        pw.println("\n\tpublic " + className + " ()");
        pw.println("\t{");
        pw.println("\t}\n");

        /*
         * print an accessor method for the discriminator
         */

        pw.println("\tpublic " + ts.typeName() + " discriminator ()");
        pw.println("\t{");
        pw.println("\t\treturn discriminator;");
        pw.println("\t}\n");

        /*
         * print accessor and modifiers for each case label and branch
         */

        e = switch_body.caseListVector.elements();
        while (e.hasMoreElements ())
        {
            Case c = (Case) e.nextElement();
            boolean thisCaseIsDefault = false;

            int caseLabelNum = c.case_label_list.v.size();

            String label[] = new String[ caseLabelNum ];

            /* make case labels available as strings */

            for (int i = 0; i < caseLabelNum; i++)
            {
                Object o = c.case_label_list.v.elementAt(i);
                if (o == null) // null means "default"
                {
                    label[ i ] = null;
                    thisCaseIsDefault = true;
                }
                else if (o instanceof ConstExpr)
                {
                    label[ i ] = ((ConstExpr)o).toString();
                }
                else if (o instanceof ScopedName)
                {
                    label[ i ] = ((ScopedName)o).typeName();
                }
            }

            // accessors

            pw.println("\tpublic " + c.element_spec.typeSpec.typeName()
                       + " " + c.element_spec.declarator.name() + " ()");
            pw.println("\t{");
            pw.print("\t\tif (discriminator != ");

            boolean defaultFound = false;
            for (int i = 0; i < caseLabelNum; i++)
            {
                if (label[ i ] == null)
                {
                    defaultFound = true;
                    pw.print (defaultStr);
                }
                else
                {
                    pw.print (label[ i ]);
                }

                if (i < caseLabelNum - 1)
                {
                    pw.print(" && discriminator != ");
                }
            }

            // Add checks for any unused case labels for default

            if (defaultFound)
            {
                for (int i = 0; i < unusedCaseLabels.size (); i++)
                {
                    String lab = (String) unusedCaseLabels.elementAt (i);
                    if (! lab.equals (defaultStr))
                    {
                        pw.print (" && discriminator != " + lab);
                    }
                }
            }

            pw.println(")\n\t\t\tthrow new org.omg.CORBA.BAD_OPERATION();");
            pw.println("\t\treturn " + c.element_spec.declarator.name() + ";");
            pw.println("\t}\n");

            // modifiers

            pw.println("\tpublic void " + c.element_spec.declarator.name() +
                       " (" + c.element_spec.typeSpec.typeName() + " _x)");
            pw.println("\t{");

            pw.print("\t\tdiscriminator = ");

            if (label[ 0 ] == null)
            {
                pw.println(defaultStr + ";");
            }
            else
            {
                pw.println(label[ 0 ] + ";");
            }
            pw.println("\t\t" + c.element_spec.declarator.name() + " = _x;");
            pw.println("\t}\n");

            if (caseLabelNum > 1 || thisCaseIsDefault)
            {
                pw.println("\tpublic void " + c.element_spec.declarator.name() + " (" +
                           ts.typeName() + " _discriminator, " +
                           c.element_spec.typeSpec.typeName() + " _x)");
                pw.println("\t{");
                pw.print("\t\tif (_discriminator != ");

                defaultFound = false;
                for (int i = 0; i < caseLabelNum; i++)
                {
                    if (label[ i ] == null)
                    {
                        defaultFound = true;
                        pw.print(defaultStr);
                    }
                    else
                    {
                        pw.print(label[ i ]);
                    }

                    if (i < caseLabelNum - 1)
                    {
                        pw.print(" && _discriminator != ");
                    }
                }

                // Add checks for any unused case labels for default

                if (defaultFound)
                {
                    for (int i = 0; i < unusedCaseLabels.size (); i++)
                    {
                        String lab = (String) unusedCaseLabels.elementAt (i);
                        if (! lab.equals (defaultStr))
                        {
                            pw.print (" && _discriminator != " + lab);
                        }
                    }
                }

                pw.println(")\n\t\t\tthrow new org.omg.CORBA.BAD_OPERATION();");
                pw.println("\t\tdiscriminator = _discriminator;");
                pw.println("\t\t" + c.element_spec.declarator.name() + " = _x;");
                pw.println("\t}\n");
            }
        }

        if (parser.generateEnhanced)
        {
            printToString(className, pw, defaultStr);
        }

        /* if there is no default case and case labels do not cover
         * all discriminator values, we have to generate __defaultmethods
         */

        if (def == 0 && defaultStr.length() > 0)
        {
            pw.println("\tpublic void __default ()");
            pw.println("\t{");
            pw.println("\t\tdiscriminator = " + defaultStr + ";");
            pw.println("\t}");

            pw.println("\tpublic void __default (" + ts.typeName() + " _discriminator)");
            pw.println("\t{");
            pw.print("\t\tif( ");
            for ( int i = 0; i < allCaseLabels.size (); i++ )
            {
                String lab = (String) allCaseLabels.elementAt( i );
                if (i == 0)
                {
                    pw.print(" _discriminator == " + lab);
                }
                else
                {
                    pw.print(" || _discriminator == " + lab);
                }
            }
            pw.println(" )\n\t\t\tthrow new org.omg.CORBA.BAD_PARAM( \"Illegal value is used in __default method\","
                    + " 34, org.omg.CORBA.CompletionStatus.COMPLETED_NO );\n");
            pw.println("\t\tdiscriminator = _discriminator;");
            pw.println("\t}");
        }

        pw.println("}");
    }

    private void printToString(String s, PrintWriter pw, String defaultStr)
    {
        String indent1 = "\t\t\t";
        String indent2 = "\t\t\t\t";
        if (switch_is_longlong)
        {
            indent1 = "\t\t";
            indent2 = "\t\t\t";
        }

        String case_str = "case ";
        String colon_str = ":";
        String default_str = "default:";

        pw.println("\tpublic String toString()");
        pw.println("\t{");
        pw.println("\t\tfinal java.lang.StringBuffer _ret = new java.lang.StringBuffer();");
        pw.print("\t\t_ret.append(\"union ");
        pw.print(s);
        pw.println(" {\");");
        pw.println("\t\t_ret.append(\"\\n\");");


        if (switch_is_enum)
        {
            pw.println ("\t\tswitch (discriminator.value ())");
            pw.println ("\t\t{");
        }
        else
        {
            if (switch_is_bool)
            {
                /* special case: booleans are no switch type in java */
                case_str = "if (discriminator  ==";
                colon_str = ")";
                // colon_str and default_str are already set correctly
            }
            else if (switch_is_longlong)
            {
                pw.println ("\t\tlong disc = discriminator ;");
            }
            else
            {
                pw.println ("\t\tswitch (discriminator )");
                pw.println ("\t\t{");
            }
        }


        UnionIterator ui = new UnionIterator (false)
        {
           protected String writeExpression(PrintWriter caseWriter, Case cse, int caseLabelNum, String indent2, Object o)
           {
              caseWriter.print("\t\t\t\t_ret.append(");
              caseWriter.print(cse.element_spec.declarator.name());
              caseWriter.println(");");
              // no "break" written for default case
              if (o != null && !switch_is_bool && !switch_is_longlong)
              {
                  caseWriter.println (indent2 + "break;");
              }
              if (switch_is_longlong)
              {
                  caseWriter.println (indent2 + "return;");
              }
              return null;
           }
        };
        ui.iterate (pw, indent1, indent2, "", case_str, colon_str, default_str);

        pw.println("\t\t_ret.append(\"}\");");
        pw.println("\t\treturn _ret.toString();");
        pw.println("\t}");
        pw.println();
    }

    public void printHolderClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
        {
            ps.println("package " + pack_name + ";");
        }

        printClassComment("union", className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Holder");
        ps.println("\timplements org.omg.CORBA.portable.Streamable");
        ps.println("{");

        ps.println("\tpublic " + className + " value;\n");

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
        ps.println("\t\t" + className + "Helper.write (out, value);");
        ps.println("\t}");

        ps.println("}");
    }

    private void printHelperClass (String className, PrintWriter ps)
    {
        if (!pack_name.equals (""))
        {
            ps.println ("package " + pack_name + ";");
        }

        printImport(ps);

        printClassComment("union", className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Helper");
        ps.println("{");

        ps.println("\tprivate volatile static org.omg.CORBA.TypeCode _type;");


        ps.println("\tpublic static org.omg.CORBA.TypeCode type ()");
        ps.println("\t{");
        ps.println("\t\tif (_type == null)");
        ps.println("\t\t{");
        ps.println("\t\t\tsynchronized(" + name + "Helper.class)");
        ps.println("\t\t\t{");
        ps.println("\t\t\t\tif (_type == null)");
        ps.println("\t\t\t\t{");

        Enumeration e;
        Case cse;
        int caseLabelNum;

        ps.println("\t\t\torg.omg.CORBA.UnionMember[] members = new org.omg.CORBA.UnionMember[" + labels + "];");
        ps.println("\t\t\torg.omg.CORBA.Any label_any;");

        TypeSpec label_t = switch_type_spec.typeSpec();

        if (label_t instanceof ScopedName)
        {
            label_t = ((ScopedName)label_t).resolvedTypeSpec();
        }

        label_t = label_t.typeSpec();
        e = switch_body.caseListVector.elements ();

        int mi = 0;

        while (e.hasMoreElements ())
        {
            cse = (Case) e.nextElement();
            TypeSpec t = cse.element_spec.typeSpec;

            if (t instanceof ScopedName)
                t = ((ScopedName)t).resolvedTypeSpec();

            t = t.typeSpec();
            Declarator d = cse.element_spec.declarator;

            caseLabelNum = cse.case_label_list.v.size();
            for (int i = 0; i < caseLabelNum; i++)
            {
                Object o = cse.case_label_list.v.elementAt(i);

                ps.println("\t\t\tlabel_any = org.omg.CORBA.ORB.init().create_any ();");

                TypeSpec tocheck = label_t;
                if (label_t instanceof AliasTypeSpec)
                {
                    tocheck =  ((AliasTypeSpec)label_t).originalType();
                }

                if (o == null)
                {
                    ps.println("\t\t\tlabel_any.insert_octet ((byte)0);");
                }
                else if (tocheck instanceof BaseType)
                {
                    if ((tocheck instanceof CharType) ||
                            (tocheck instanceof BooleanType) ||
                            (tocheck instanceof LongType) ||
                            (tocheck instanceof LongLongType))
                        {
                            ps.print("\t\t\tlabel_any." + tocheck.printInsertExpression() + " ((");
                        }
                        else if (tocheck instanceof ShortType)
                        {
                            ps.print("\t\t\tlabel_any." + tocheck.printInsertExpression() + " ((short)(");
                        }
                        else
                        {
                            throw new RuntimeException("Compiler error: unrecognized BaseType: "
                                                       + tocheck.typeName() + ":" + tocheck + ": " + tocheck.typeSpec()
                                                       + ": " + tocheck.getClass().getName());
                        }
                        ps.println(((ConstExpr)o) + "));");
                }
                else if (switch_is_enum)
                {
                    String _t = ((ScopedName)o).typeName();
                    ps.println("\t\t\t" + _t.substring(0, _t.lastIndexOf('.'))
                               + "Helper.insert(label_any, " + _t + ");");
                }
                else
                {
                    throw new RuntimeException("Compiler error: unrecognized label type: " + tocheck.typeName());
                }

                ps.print
                    (
                     "\t\t\tmembers[" +
                     (mi++) +
                     "] = new org.omg.CORBA.UnionMember (\"" +
                     d.deEscapeName() +
                     "\", label_any, "
                     );

                if (t instanceof ConstrTypeSpec)
                {
                    try
                    {
                        ps.print(t.typeSpec().helperName() + ".type(),");
                    } catch (NoHelperException ex)
                    {
                        ps.print(t.typeSpec().getTypeCodeExpression() + ",");
                    }
                }
                else
                {
                    ps.print(t.typeSpec().getTypeCodeExpression() + ",");
                }

                ps.println("null);");
            }
        }
        ps.print("\t\t\t _type = org.omg.CORBA.ORB.init().create_union_tc(id(),\"" + className() + "\",");
        ps.println(switch_type_spec.typeSpec().getTypeCodeExpression() + ", members);");
        ps.println("\t\t\t\t}");
        ps.println("\t\t\t}");
        ps.println("\t\t}");
        ps.println("\t\t\treturn _type;");
        ps.println("\t}"  + Environment.NL);





        TypeSpec.printInsertExtractMethods(ps, typeName());

        printIdMethod(ps);

        /** read method */

        ps.println("\tpublic static " + className + " read (org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");

        if (parser.hasObjectCachePlugin())
        {
            parser.getObjectCachePlugin().printCheckout(ps, className, "result");
            parser.getObjectCachePlugin().printPreMemberRead(ps, this);
        }
        else
        {
            ps.println("\t\t" + className + " result = new " + className + "();");
        }

        TypeSpec switch_ts_resolved = switch_type_spec;

        if (switch_type_spec.type_spec instanceof ScopedName)
        {
            switch_ts_resolved = ((ScopedName)switch_type_spec.type_spec).resolvedTypeSpec();
        }

        String indent1 = "\t\t\t";
        String indent2 = "\t\t\t\t";
        String tryIndent = "";
        if (switch_is_longlong)
        {
            indent1 = "\t\t";
            indent2 = "\t\t\t";
        }
        String case_str = "case ";
        String colon_str = ":";
        String default_str = "default:";

        if (switch_is_enum)
        {
            ps.println("\t\t" + switch_ts_resolved.toString() + " disc;");
            if(explicit_default_case || !allCasesCovered)
            {
                tryIndent = "\t";
                indent1 = "\t\t\t\t";
                indent2 = "\t\t\t\t\t";

                ps.println("\t\ttry");
                ps.println("\t\t{");
            }
            ps.println(tryIndent + "\t\tdisc = " + switch_ts_resolved.toString() + ".from_int(in.read_long());");

            ps.println(tryIndent + "\t\tswitch (disc.value ())");
            ps.println(tryIndent + "\t\t{");
        }
        else
        {
            ps.println ("\t\t" + switch_ts_resolved.toString () + " "
                        + switch_ts_resolved.printReadStatement ("disc", "in"));
            if (switch_is_bool)
            {
                /* special case: boolean is not a switch type in java */

                case_str = "if (disc == ";
                colon_str = ")";
                default_str = "else";
            }
            else if (switch_is_longlong)
            {
                /* special case: long is not a switch type in java */

                case_str = "if (disc == ";
                colon_str = ")";
                default_str = "else";
            }
            else
            {
                ps.println("\t\tswitch (disc)");
                ps.println("\t\t{");
            }
        }


        UnionIterator ui = new UnionIterator (true)
        {
           protected String writeExpression(PrintWriter caseWriter, Case cse, int caseLabelNum, String indent2, Object o)
           {
              ByteArrayOutputStream bosDef = new ByteArrayOutputStream ();
              PrintWriter bad_paramDefaultWriter = new PrintWriter (bosDef);
               TypeSpec t = cse.element_spec.typeSpec;
               Declarator d = cse.element_spec.declarator;
               String varname = "_var";

               if (t instanceof ScopedName)
               {
                   t = ((ScopedName)t).resolvedTypeSpec ();
               }
               t = t.typeSpec ();

               bad_paramDefaultWriter.println (indent2 + t.typeName () + " " + varname + ";");
               bad_paramDefaultWriter.println (indent2 + t.printReadStatement (varname, "in"));
               bad_paramDefaultWriter.print (indent2 + "result." + d.name () + " (");

               bad_paramDefaultWriter.close();
               caseWriter.print(bosDef.toString());
               if (caseLabelNum > 1)
               {
                   caseWriter.print ("disc,");
               }
               caseWriter.println (varname + ");");

               // no "break" written for default case or for "if" construct
               if (o != null && !switch_is_bool && !switch_is_longlong)
               {
                   caseWriter.println (indent2 + "break;");
               }
               if (switch_is_longlong)
               {
                   caseWriter.println (indent2 + "return result;");
               }
               return bosDef.toString ();
           }
        };
        ui.iterate (ps, indent1, indent2, "", case_str, colon_str, default_str);



        if (parser.hasObjectCachePlugin())
        {
            parser.getObjectCachePlugin().printPostMemberRead(ps, this, "result");
        }

        if (!switch_is_longlong)
        {
            ps.println ("\t\treturn result;");
        }
        ps.println ("\t}");

        if (parser.hasObjectCachePlugin())
        {
            parser.getObjectCachePlugin().printCheckinHelper(ps, this);
        }

        /** write method */

        ps.println ("\tpublic static void write (org.omg.CORBA.portable.OutputStream out, " + className + " s)");
        ps.println ("\t{");

        // Write out discriminator value plus start of switch statement

        if (switch_is_enum)
        {
            ps.println ("\t\tout.write_long (s.discriminator().value ());");
            ps.println ("\t\tswitch (s.discriminator().value ())");
            ps.println ("\t\t{");
        }
        else
        {
            ps.println ("\t\t" + switch_type_spec.typeSpec().printWriteStatement("s.discriminator ()", "out"));
            if (switch_is_bool)
            {
                /* special case: booleans are no switch type in java */
                case_str = "if (s.discriminator () == ";
                // colon_str and default_str are already set correctly
            }
            else if (switch_is_longlong)
            {
                ps.println ("\t\tlong disc = s.discriminator ();");
            }
            else
            {
                ps.println ("\t\tswitch (s.discriminator ())");
                ps.println ("\t\t{");
            }
        }

        // Write out cases

        ui = new UnionIterator (false)
        {
           protected String writeExpression(PrintWriter caseWriter, Case cse, int caseLabelNum, String indent2, Object o)
           {
               TypeSpec t = cse.element_spec.typeSpec;
               Declarator d = cse.element_spec.declarator;

               if (t instanceof ScopedName)
               {
                   t = ((ScopedName)t).resolvedTypeSpec ();
               }
               t = t.typeSpec ();

               caseWriter.println (indent2 + t.printWriteStatement ("s." + d.name ()
                                                                    + " ()", "out"));

               // no "break" written for default case

               if (o != null && !switch_is_bool && !switch_is_longlong)
               {
                   caseWriter.println (indent2 + "break;");
               }
               if (switch_is_longlong)
               {
                   caseWriter.println (indent2 + "return;");
               }
               return null;
           }
        };
        ui.iterate (ps, indent1, indent2, "", case_str, colon_str, default_str);

        ps.println ("\t}");

        ps.println("}"); // end of helper class
    }

    /** generate required classes */

    public void print(PrintWriter ps)
    {
        setPrintPhaseNames();

        // no code generation for included definitions
        if (included && !generateIncluded())
            return;

        // only write once
        if (!written)
        {
            // Forward declaration
            if (switch_type_spec != null)
            {
                try
                {
                    // JAC570: when the enum declaration is used in union declaration
                    // the following code will generate implementation classes for enum
                    if (switch_type_spec.type_spec instanceof ConstrTypeSpec)
                    {
                        switch_type_spec.print(ps);
                    }

                    switch_body.print(ps);

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
                        printUnionClass(className, printWriter);
                        printWriter.close();
                    }

                    fname = className + "Holder.java";
                    f = new File(dir, fname);
                    if (GlobalInputStream.isMoreRecentThan(f))
                    {
                        // print the holder  class
                        PrintWriter printWriter = new PrintWriter(new java.io.FileWriter(f));
                        printHolderClass(className, printWriter);
                        printWriter.close();
                    }

                    fname = className + "Helper.java";
                    f = new File(dir, fname);
                    if (GlobalInputStream.isMoreRecentThan(f))
                    {
                        // print the help class
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
        ps.println("\t\t" + resultname + " = " + className() + "Helper.extract(" + anyname + ");");
    }

    public String toString()
    {
        return typeName();
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitUnion(this);
    }

    public void set_name(String n)
    {
        super.set_name(n);

        boolean setpkg = (switch_type_spec != null && !(switch_type_spec.typeSpec() instanceof ScopedName));

        // Don't override the package if this is a scopedname.
        if (setpkg)
        {
            switch_type_spec.setPackage( n );
        }

        // As per above.
        if (switch_body != null && setpkg)
        {
            switch_body.setPackage( n );
        }
    }

    private abstract class UnionIterator
    {
       boolean readExpression = false;

       UnionIterator(boolean writeExpression)
       {
          this.readExpression = writeExpression;
       }

        public void iterate(PrintWriter pw, String indent1, String indent2, String tryIndent, String case_str, String colon_str, String default_str)
        {
            Enumeration e;
            Case cse;
            ByteArrayOutputStream bos;
            PrintWriter caseWriter;
            boolean was_default = false;
            int caseCount = 0;
            int caseLabelNum;
            String defaultCases = null;
            String varname = "_var";
            String bosDef = "";

            if (switch_is_longlong)
            {
                indent1 = "\t\t";
                indent2 = "\t\t\t";
            }

            // Write out cases
            e = switch_body.caseListVector.elements ();
            while (e.hasMoreElements ())
            {
                was_default = false;
                bos = new ByteArrayOutputStream ();
                caseWriter = new PrintWriter (bos);

                cse = (Case) e.nextElement ();
                caseLabelNum = cse.case_label_list.v.size ();
                Object o;

                for (int i = 0; i < caseLabelNum; i++)
                {
                    o = cse.case_label_list.v.elementAt (i);
                    caseCount++;

                    if (o == null)
                    {
                        // null means "default"

                        caseWriter.println (indent1 + default_str);
                        was_default = true;
                    }
                    else if (o instanceof ConstExpr)
                    {
                        caseWriter.println (indent1 + case_str
                                            + ((ConstExpr)o) + colon_str);
                    }
                    else if (o instanceof ScopedName)
                    {
                        String _t = ((ScopedName)o).typeName ();
                        if (switch_is_enum)
                        {
                            caseWriter.println (indent1 + case_str
                                                + _t.substring (0, _t.lastIndexOf ('.') + 1)
                                                + "_" + _t.substring (_t.lastIndexOf ('.') + 1)
                                                + colon_str);
                        }
                        else
                        {
                            caseWriter.println (indent1 + case_str + _t + colon_str);
                        }
                    }

                    if (i == caseLabelNum - 1)
                    {
                        caseWriter.println (indent1 + "{");

                        bosDef = writeExpression (caseWriter, cse, caseLabelNum, indent2, o);

                        caseWriter.println (indent1 + "}");
                    }
                }

                if (switch_is_bool && !was_default)
                {
                    case_str = "else " + case_str;
                }

                // Print cases unless default

                caseWriter.close ();
                if (bos.size () > 0)
                {
                    if (was_default)
                    {
                        defaultCases = bos.toString ();
                    }
                    else
                    {
                        pw.print (bos.toString ());
                    }
                }
            }

            if (readExpression)
            {
                if (switch_is_enum && (!explicit_default_case && !switch_is_bool
                                       && !switch_is_longlong && !allCasesCovered))
                {
                    pw.println (tryIndent + "\t\t\tdefault: result.__default (disc);");
                }
                if (!explicit_default_case && switch_is_longlong)
                {
                    pw.println ("\t\tresult.__default (disc);");
                    pw.println ("\t\treturn result;");
                }
                // How can we have boolean with more than two cases?
                if (switch_is_bool && caseCount > 2)
                {
                    System.err.println ("Case count is larger than two for a boolean expression");
                    throw new RuntimeException ("Case count is larger than two for a boolean expression");
                }
                // If we have has boolean with one case then add a default.
                if (!explicit_default_case && switch_is_bool && caseCount != 2)
                {
                    pw.println ("\t\t\telse");
                    pw.println ("\t\t\t{");
                    pw.println ("\t\t\t\tresult.__default (disc);");
                    pw.println ("\t\t\t}");
                }
            }
            // Print out default cases last
            if (defaultCases != null)
            {
                pw.print (defaultCases);
            }

            /* close switch statement */

            if (!switch_is_bool && !switch_is_longlong)
            {
                pw.println ("\t\t}");
            }

            // close try
            if(readExpression && switch_is_enum && (explicit_default_case || !allCasesCovered))
            {
                pw.println("\t\t}");
                pw.println("\t\tcatch (org.omg.CORBA.BAD_PARAM b)");
                pw.println("\t\t{");
                pw.println("\t\t\t// The default value was out-of-bounds for the Enum. Just use the default.");

                if(was_default)
                {
                    pw.print(bosDef);
                    pw.println(varname + ");");
                }
                else
                {
                    pw.println("\t\t\tresult.__default ();");
                }
                pw.println("\t\t}");
            }
        }

        protected abstract String writeExpression(PrintWriter caseWriter, Case cse, int caseLabelNum, String indent2, Object o);
    }
}
