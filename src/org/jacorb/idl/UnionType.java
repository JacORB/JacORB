package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
import java.util.*;

class UnionType
    extends TypeDeclaration
    implements Scope
{
    /** the union's discriminator's type spec */
    TypeSpec switch_type_spec;

    SwitchBody switch_body;
    boolean written = false;

    private ScopeData scopeData;

    private boolean allCasesCovered = false;
    private boolean switch_is_enum = false;
    private boolean switch_is_bool = false;
    private boolean switch_is_longlong = false;
    private boolean explicit_default_case = false;
    private int labels;

    public UnionType( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        UnionType ut = new UnionType( new_num() );
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

    public void setScopeData( ScopeData data )
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


    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
        if (switch_body != null)
        {
            switch_body.setEnclosingSymbol( s );
        }
    }

    public String typeName()
    {
        if( typeName == null )
            setPrintPhaseNames();
        return typeName;
    }

    public String className()
    {
        String fullName = typeName();
        if( fullName.indexOf( '.' ) > 0 )
        {
            return fullName.substring( fullName.lastIndexOf( '.' ) + 1 );
        }
        else
        {
            return fullName;
        }
    }

    public String printReadExpression( String Streamname )
    {
        return typeName() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return typeName() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public void set_included( boolean i )
    {
        included = i;
    }

    public void setSwitchType( TypeSpec s )
    {
        switch_type_spec = s;
    }

    public void setSwitchBody( SwitchBody sb )
    {
        switch_body = sb;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
        {
           pack_name = new String( s + "." + pack_name);
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
            switch_body.setPackage( s );
        }
    }

    public boolean basic()
    {
        return false;
    }

    public void parse()
    {
        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
        try
        {
            ScopedName.definePseudoScope( full_name() );
            ctspec.c_type_spec = this;
            NameTable.define( full_name(), "type-union" );
            TypeMap.typedef( full_name(), ctspec );
        }
        catch (NameAlreadyDefined nad)
        {
            if (parser.get_pending (full_name ()) != null)
            {
                if (switch_type_spec == null )
                {
                    justAnotherOne = true;
                }
                // else actual definition

                if( !full_name().equals( "org.omg.CORBA.TypeCode" ) && switch_type_spec != null )
                {
                    TypeMap.replaceForwardDeclaration( full_name(), ctspec );
                }
            }
            else
            {
                Environment.output( 4, nad );
                parser.error( "Union " + full_name() + " already defined", token );
            }
        }

        if (switch_type_spec != null)
        {
            // Resolve scoped names and aliases
            TypeSpec ts;
            if( switch_type_spec.type_spec instanceof ScopedName )
            {
                ts = ( (ScopedName)switch_type_spec.type_spec ).resolvedTypeSpec();

                while( ts instanceof ScopedName || ts instanceof AliasTypeSpec )
                {
                    if( ts instanceof ScopedName )
                    {
                        ts = ( (ScopedName)ts ).resolvedTypeSpec();
                    }
                    else
                    {
                        ts = ( (AliasTypeSpec)ts ).originalType();
                    }
                }
                addImportedName( switch_type_spec.typeName() );
            }
            else
            {
                ts = switch_type_spec.type_spec;
            }

            // Check if we have a valid discriminator type

            if
            ( !(
                    ( ( ts instanceof SwitchTypeSpec ) &&
                    ( ( (SwitchTypeSpec)ts ).isSwitchable() ) )
                    ||
                    ( ( ts instanceof BaseType ) &&
                    ( ( (BaseType)ts ).isSwitchType() ) )
                    ||
                    ( ( ts instanceof ConstrTypeSpec ) &&
                    ( ( (ConstrTypeSpec)ts ).c_type_spec instanceof EnumType ) )
                    ) )
            {
                parser.error( "Illegal Switch Type: " + ts.typeName(), token );
            }

            switch_type_spec.parse();
            switch_body.setTypeSpec( switch_type_spec );
            switch_body.setUnion( this );

            ScopedName.addRecursionScope( typeName() );
            switch_body.parse();
            ScopedName.removeRecursionScope( typeName() );

            // Fixup array typing

            for( Enumeration e = switch_body.caseListVector.elements(); e.hasMoreElements(); )
            {
                Case c = (Case)e.nextElement();
                c.element_spec.t = getElementType( c.element_spec );
            }

            NameTable.parsed_interfaces.put( full_name(), "" );
            parser.remove_pending( full_name() );
        }
        else if( !justAnotherOne )
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending( full_name() );
        }
    }


    /**
     * @returns a string for an expression of type TypeCode that
     * describes this type
     */

    public String getTypeCodeExpression()
    {
        return typeName() + "Helper.type()";
    }

    public String getTypeCodeExpression( Set knownTypes )
    {
        if( knownTypes.contains( this ) )
        {
            return this.getRecursiveTypeCodeExpression();
        }
        else
        {
            return this.getTypeCodeExpression();
        }
    }

    private void printClassComment( String className, PrintWriter ps )
    {
        ps.println( "/**" );
        ps.println( " *\tGenerated from IDL definition of union " +
                "\"" + className + "\"" );
        ps.println( " *\t@author JacORB IDL compiler " );
        ps.println( " */\n" );
    }

    public void printUnionClass( String className, PrintWriter pw )
    {
        Enumeration e;
        if( !pack_name.equals( "" ) )
            pw.println( "package " + pack_name + ";" );

        printImport( pw );

        printClassComment( className, pw );

        pw.println( "public" + parser.getFinalString() + " class " + className );
        pw.println( "\timplements org.omg.CORBA.portable.IDLEntity" );
        pw.println( "{" );

        TypeSpec ts = switch_type_spec.typeSpec();

        while( ts instanceof ScopedName || ts instanceof AliasTypeSpec )
        {
            if( ts instanceof ScopedName )
                ts = ( (ScopedName)ts ).resolvedTypeSpec();
            if( ts instanceof AliasTypeSpec )
                ts = ( (AliasTypeSpec)ts ).originalType();
        }

        pw.println( "\tprivate " + ts.typeName() + " discriminator;" );

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
            for( int i = 0; i < c.case_label_list.v.size(); i++ )
            {
                labels++; // the overall number of labels is needed in a number of places...
                Object ce = c.case_label_list.v.elementAt( i );
                if( ce != null )
                {
                    if( ce instanceof ConstExpr )
                    {
                        allCaseLabels.addElement( ( (ConstExpr)ce ).value() );
                    }
                    else
                    {
                        // this is a scoped name
                        allCaseLabels.addElement
                            (ScopedName.unPseudoName (((ScopedName)ce).resolvedName()));

                        Environment.output
                        (
                            4,
                             "Adding " +
                            (ScopedName.unPseudoName (((ScopedName)ce).resolvedName ())) +
                            " case labels."
                        );
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

        if( ( ts instanceof ConstrTypeSpec &&
                ( (ConstrTypeSpec)ts ).declaration() instanceof EnumType ) )
        {
            this.switch_is_enum = true;
            EnumType et = (EnumType)( (ConstrTypeSpec)ts ).declaration();

            if( allCaseLabels.size() + def > et.size() )
            {
                lexer.emit_warn( "Too many case labels in definition of union " +
                        full_name() + ", default cannot apply", token );
            }
            if( allCaseLabels.size() + def == et.size() )
            {
                allCasesCovered = true;
            }

            for( int i = 0; i < et.size(); i++ )
            {
                String qualifiedCaseLabel =
                        ts.typeName() + "." + (String)et.enumlist.v.elementAt( i );
                if( !( allCaseLabels.contains( qualifiedCaseLabel ) ) )
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
            if( ts instanceof BaseType )
            {
                ts = ( (BaseType)ts ).typeSpec();
            }

            if( ts instanceof BooleanType )
            {
                this.switch_is_bool = true;

                // find a "default" for boolean

                if( allCaseLabels.size() + def > 2 )
                {
                    parser.error( "Case label error: too many default labels.", token );
                    return;
                }
                else if( allCaseLabels.size() == 1 )
                {
                    if( ( (String)allCaseLabels.elementAt( 0 ) ).equals( "true" ) )
                        defaultStr = "false";
                    else
                        defaultStr = "true";
                }
                else
                {
                    // labels for both true and false -> no default possible
                }
            }
            else if (ts instanceof CharType)
            {
                // find a "default" for char

                Enumeration enum;
                String charStr;
                boolean matched = false;
                short val;

                // Iterate through values until find on that is not a case value

                for (short s = 0; s < 256; s++)
                {
                    matched = false;
                    enum = allCaseLabels.elements ();
                    while (enum.hasMoreElements ())
                    {
                        charStr = (String) enum.nextElement ();

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
                        else
                        {
                            continue;
                        }
                    }

                    // Current value does not match a case value so set as default

                    if (! matched)
                    {
                        defaultStr = "(char)" + s;
                        break;
                    }
                }
            }
            else if( ts instanceof IntType )
            {
                int maxint = 65536; // 2^16, max short
                if( ts instanceof LongType )
                    maxint = 2147483647; // -2^31,  max long
                for( int i = 0; i < maxint; i++ )
                {
                    if( !( allCaseLabels.contains( String.valueOf( i ) ) ) )
                    {
                        defaultStr = Integer.toString( i );
                        break;
                    }
                }
                if( ts instanceof LongLongType )
                {
                    this.switch_is_longlong = true;
                }
            }
            else
            {
                System.err.println( "Something went wrong in UnionType, "
                        + "could not identify switch type "
                        + switch_type_spec.type_spec );
            }

        }


        /* print members */

        e = switch_body.caseListVector.elements();
        while (e.hasMoreElements())
        {
            Case c = (Case)e.nextElement();
            int caseLabelNum = c.case_label_list.v.size();

            String label[] = new String[ caseLabelNum ];
            for( int i = 0; i < caseLabelNum; i++ )
            {
                Object o = c.case_label_list.v.elementAt( i );
                if( o == null )  // null means "default"
                {
                    label[ i ] = null;
                }
                else if( o != null && o instanceof ConstExpr )
                {
                    label[ i ] = ( (ConstExpr)o ).value();
                }
                else if( o instanceof ScopedName )
                {
                    label[ i ] = ( (ScopedName)o ).typeName();
                }
            }

            pw.println( "\tprivate " + c.element_spec.t.typeName()
                    + " " + c.element_spec.d.name() + ";" );
        }

        /*
         * print a constructor for class member initialization
         */

        pw.println( "\n\tpublic " + className + " ()" );
        pw.println( "\t{" );
        pw.println( "\t}\n" );

        /*
         * print an accessor method for the discriminator
         */

        pw.println( "\tpublic " + ts.typeName() + " discriminator ()" );
        pw.println( "\t{" );
        pw.println( "\t\treturn discriminator;" );
        pw.println( "\t}\n" );

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

            for( int i = 0; i < caseLabelNum; i++ )
            {
                Object o = c.case_label_list.v.elementAt( i );
                if( o == null ) // null means "default"
                {
                    label[ i ] = null;
                    thisCaseIsDefault = true;
                }
                else if( o instanceof ConstExpr )
                    label[ i ] = ( (ConstExpr)o ).value();
                else if( o instanceof ScopedName )
                    label[ i ] = ( (ScopedName)o ).typeName();
            }

            // accessors

            pw.println( "\tpublic " + c.element_spec.t.typeName()
                    + " " + c.element_spec.d.name() + " ()" );
            pw.println( "\t{" );
            pw.print( "\t\tif (discriminator != " );

            boolean defaultFound = false;
            for( int i = 0; i < caseLabelNum; i++ )
            {
                if( label[ i ] == null )
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
                    pw.print( " && discriminator != " );
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

            pw.println( ")\n\t\t\tthrow new org.omg.CORBA.BAD_OPERATION();" );
            pw.println( "\t\treturn " + c.element_spec.d.name() + ";" );
            pw.println( "\t}\n" );

            // modifiers

            pw.println( "\tpublic void " + c.element_spec.d.name() +
                    " (" + c.element_spec.t.typeName() + " _x)" );
            pw.println( "\t{" );

            pw.print( "\t\tdiscriminator = " );

            if( label[ 0 ] == null )
                pw.println( defaultStr + ";" );
            else
                pw.println( label[ 0 ] + ";" );
            pw.println( "\t\t" + c.element_spec.d.name() + " = _x;" );
            pw.println( "\t}\n" );

            if( caseLabelNum > 1 || thisCaseIsDefault )
            {
                pw.println( "\tpublic void " + c.element_spec.d.name() + " (" +
                        ts.typeName() + " _discriminator, " +
                        c.element_spec.t.typeName() + " _x)" );
                pw.println( "\t{" );
                pw.print( "\t\tif (_discriminator != " );

                defaultFound = false;
                for( int i = 0; i < caseLabelNum; i++ )
                {
                    if( label[ i ] == null )
                    {
                        defaultFound = true;
                        pw.print( defaultStr );
                    }
                    else
                    {
                        pw.print( label[ i ] );
                    }

                    if( i < caseLabelNum - 1 )
                    {
                        pw.print( " && _discriminator != " );
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

                pw.println( ")\n\t\t\tthrow new org.omg.CORBA.BAD_OPERATION();" );
                pw.println( "\t\tdiscriminator = _discriminator;" );
                pw.println( "\t\t" + c.element_spec.d.name() + " = _x;" );
                pw.println( "\t}\n" );
            }
        }

        /* if there is no default case and case labels do not cover
         * all discriminator values, we have to generate __defaultmethods
         */

        if( def == 0 && defaultStr.length() > 0 )
        {
            pw.println( "\tpublic void __default ()" );
            pw.println( "\t{" );
            pw.println( "\t\tdiscriminator = " + defaultStr + ";" );
            pw.println( "\t}" );

            pw.println( "\tpublic void __default (" + ts.typeName() + " _discriminator)" );
            pw.println( "\t{" );
            pw.println( "\t\tdiscriminator = _discriminator;" );
            pw.println( "\t}" );
        }

        pw.println( "}" );
    }


    public void printHolderClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";" );

        printClassComment( className, ps );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Holder" );
        ps.println( "\timplements org.omg.CORBA.portable.Streamable" );
        ps.println( "{" );

        ps.println( "\tpublic " + className + " value;\n" );

        ps.println( "\tpublic " + className + "Holder ()" );
        ps.println( "\t{" );
        ps.println( "\t}" );

        ps.println( "\tpublic " + className + "Holder (final " + className + " initial)" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = initial;" );
        ps.println( "\t}" );

        ps.println( "\tpublic org.omg.CORBA.TypeCode _type ()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + className + "Helper.type ();" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _read (final org.omg.CORBA.portable.InputStream in)" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = " + className + "Helper.read (in);" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _write (final org.omg.CORBA.portable.OutputStream out)" );
        ps.println( "\t{" );
        ps.println( "\t\t" + className + "Helper.write (out, value);" );
        ps.println( "\t}" );

        ps.println( "}" );
    }

    private void printHelperClass (String className, PrintWriter ps)
    {
        if (!pack_name.equals (""))
        {
            ps.println ("package " + pack_name + ";");
        }

        printImport( ps );

        printClassComment( className, ps );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Helper" );
        ps.println( "{" );
        ps.println( "\tprivate static org.omg.CORBA.TypeCode _type;" );

        String _type = typeName();

        TypeSpec.printInsertExtractMethods( ps, typeName() );

        printIdMethod( ps );

        /** read method */

        ps.println( "\tpublic static " + className + " read (org.omg.CORBA.portable.InputStream in)" );
        ps.println( "\t{" );
        ps.println( "\t\t" + className + " result = new " + className + " ();" );

        TypeSpec switch_ts_resolved = switch_type_spec;

        if( switch_type_spec.type_spec instanceof ScopedName )
        {
            switch_ts_resolved = ( (ScopedName)switch_type_spec.type_spec ).resolvedTypeSpec();
        }

        String indent1 = "\t\t\t";
        String indent2 = "\t\t\t\t";
        if( switch_is_longlong )
        {
            indent1 = "\t\t";
            indent2 = "\t\t\t";
        }

        String case_str = "case ";
        String colon_str = ":";
        String default_str = "default:";

        if( switch_is_enum )
        {
            ps.println( "\t\t" + switch_ts_resolved.toString() + " disc = " +
                switch_ts_resolved.toString() + ".from_int(in.read_long());" );
            ps.println( "\t\tswitch (disc.value ())" );
            ps.println( "\t\t{" );
        }
        else
        {
            ps.println ("\t\t" + switch_ts_resolved.toString () + " "
                + switch_ts_resolved.printReadStatement ("disc", "in"));
            if( switch_is_bool )
            {
                /* special case: boolean is not a switch type in java */

                case_str = "if (disc == ";
                colon_str = ")";
                default_str = "else";
            }
            else if( switch_is_longlong )
            {
                /* special case: long is not a switch type in java */

                case_str = "if (disc == ";
                colon_str = ")";
                default_str = "else";
            }
            else
            {
                ps.println( "\t\tswitch (disc)" );
                ps.println( "\t\t{" );
            }
        }

        Enumeration e;
        Case cse;
        ByteArrayOutputStream bos;
        PrintWriter caseWriter;
        boolean was_default;
        int caseLabelNum;
        String defaultCases = null;

        e = switch_body.caseListVector.elements ();
        while (e.hasMoreElements ())
        {
            bos = new ByteArrayOutputStream ();
            caseWriter = new PrintWriter (bos);
            was_default = false;
            cse = (Case) e.nextElement();
            caseLabelNum = cse.case_label_list.v.size();
            TypeSpec t = cse.element_spec.t;
            Declarator d = cse.element_spec.d;

            for (int i = 0; i < caseLabelNum; i++)
            {
                Object o = cse.case_label_list.v.elementAt (i);

                if (o == null)
                {
                    // null means "default"

                    caseWriter.println (indent1 + default_str);
                    was_default = true;
                }
                else if (o instanceof ConstExpr)
                {
                    caseWriter.println (indent1 + case_str
                        + ((ConstExpr)o).value () + colon_str);
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

                    if (t instanceof ScopedName)
                    {
                        t = ((ScopedName)t).resolvedTypeSpec ();
                    }
                    t = t.typeSpec ();

                    String varname = "_var";
                    caseWriter.println (indent2 + t.typeName () + " " + varname + ";");
                    caseWriter.println (indent2 + t.printReadStatement (varname, "in"));
                    caseWriter.print (indent2 + "result." + d.name () + " (");
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
                    ps.print (bos.toString ());
                }
            }
        }

        if
        (
            !explicit_default_case && !switch_is_bool
            && !switch_is_longlong && !allCasesCovered
        )
        {
            ps.println ("\t\t\tdefault: result.__default (disc);");
        }
        if (!explicit_default_case && switch_is_longlong)
        {
            ps.println ("\t\tresult.__default (disc);");
            ps.println ("\t\treturn result;" );
        }

        // Print out default cases last

        if (defaultCases != null)
        {
            ps.print (defaultCases);
        }

        if (!switch_is_bool && !switch_is_longlong)
        {
            ps.println ("\t\t}"); // close switch statement
        }
        if (!switch_is_longlong)
        {
            ps.println ("\t\treturn result;");
        }
        ps.println ("\t}");

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
            ps.println ("\t\t" + switch_type_spec.typeSpec().printWriteStatement( "s.discriminator ()", "out" ) + ";");
            if (switch_is_bool)
            {
                /* special case: booleans are no switch type in java */
                case_str = "if (s.discriminator () ==";
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

        defaultCases = null;

        e = switch_body.caseListVector.elements ();
        while (e.hasMoreElements ())
        {
            was_default = false;
            bos = new ByteArrayOutputStream ();
            caseWriter = new PrintWriter (bos);
            cse = (Case) e.nextElement ();
            TypeSpec t = cse.element_spec.t;
            Declarator d = cse.element_spec.d;
            caseLabelNum = cse.case_label_list.v.size ();
            Object o;

            for (int i = 0; i < caseLabelNum; i++)
            {
                o = cse.case_label_list.v.elementAt (i);

                if (o == null)
                {
                    // null means "default"

                    caseWriter.println (indent1 + default_str);
                    was_default = true;
                }
                else if (o != null && o instanceof ConstExpr)
                {
                    caseWriter.println (indent1 + case_str
                        + ((ConstExpr)o ).value () + colon_str);
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
                    ps.print (bos.toString ());
                }
            }
        }

        // Print out default cases last

        if (defaultCases != null)
        {
            ps.print (defaultCases);
        }

        /* close switch statement */

        if (!switch_is_bool && !switch_is_longlong)
        {
            ps.println ("\t\t}");
        }

        ps.println ("\t}");

        /** type() */

        ps.println( "\tpublic static org.omg.CORBA.TypeCode type ()" );
        ps.println( "\t{" );
        ps.println( "\t\tif (_type == null)" );
        ps.println( "\t\t{" );
        ps.println( "\t\t\torg.omg.CORBA.UnionMember[] members = new org.omg.CORBA.UnionMember[" + labels + "];" );
        ps.println( "\t\t\torg.omg.CORBA.Any label_any;" );

        TypeSpec label_t = switch_type_spec.typeSpec();

        while( label_t instanceof ScopedName || label_t instanceof AliasTypeSpec )
        {
            if( label_t instanceof ScopedName )
                label_t = ( (ScopedName)label_t ).resolvedTypeSpec();
            if( label_t instanceof AliasTypeSpec )
                label_t = ( (AliasTypeSpec)label_t ).originalType();
        }

        label_t = label_t.typeSpec();
        e = switch_body.caseListVector.elements ();

        int mi = -1;
        for (int i=0; i<switch_body.caseListVector.size (); i++)
        {
            mi += ((Case)switch_body.caseListVector.get (i)).case_label_list.v.size ();
        }

        while (e.hasMoreElements ())
        {
            cse = (Case) e.nextElement();
            TypeSpec t = cse.element_spec.t;

            while( t instanceof ScopedName || t instanceof AliasTypeSpec )
            {
                if( t instanceof ScopedName )
                    t = ( (ScopedName)t ).resolvedTypeSpec();
                if( t instanceof AliasTypeSpec )
                    t = ( (AliasTypeSpec)t ).originalType();
            }

            t = t.typeSpec();
            Declarator d = cse.element_spec.d;

            caseLabelNum = cse.case_label_list.v.size();
            for (int i = 0; i < caseLabelNum; i++)
            {
                Object o = cse.case_label_list.v.elementAt( i );

                ps.println( "\t\t\tlabel_any = org.omg.CORBA.ORB.init().create_any ();" );

                if( o == null )
                {
                    ps.println( "\t\t\tlabel_any.insert_octet ((byte)0);" );
                }
                else if( label_t instanceof BaseType )
                {
                    if( ( label_t instanceof CharType ) ||
                            ( label_t instanceof BooleanType ) ||
                            ( label_t instanceof LongType ) ||
                            ( label_t instanceof LongLongType ) )
                    {
                        ps.print( "\t\t\tlabel_any." + label_t.printInsertExpression() + " (" );
                    }
                    else if( label_t instanceof ShortType )
                    {
                        ps.print( "\t\t\tlabel_any." + label_t.printInsertExpression() + " ((short)" );
                    }
                    else
                    {
                        throw new RuntimeException( "Compiler error: unrecognized BaseType: "
                                + label_t.typeName() + ":" + label_t + ": " + label_t.typeSpec()
                                + ": " + label_t.getClass().getName() );
                    }
                    ps.println( ( (ConstExpr)o ).value() + ");" );
                }
                else if( switch_is_enum )
                {
                    String _t = ( (ScopedName)o ).typeName();
                    ps.println( "\t\t\t" + _t.substring( 0, _t.lastIndexOf( '.' ) )
                            + "Helper.insert( label_any, " + _t + " );" );
                }
                else
                {
                    throw new Error( "Compiler error: unrecognized label type: " + label_t.typeName() );
                }

                ps.print
                (
                   "\t\t\tmembers[" +
                   (mi--) +
                   "] = new org.omg.CORBA.UnionMember (\"" +
                   d.deEscapeName() +
                   "\", label_any, "
                );

                if( t instanceof ConstrTypeSpec )
                {
                    ps.print( t.typeSpec().toString() + "Helper.type()," );
                }
                else
                {
                    ps.print( t.typeSpec().getTypeCodeExpression() + "," );
                }

                ps.println( "null);" );
            }
        }
        ps.print( "\t\t\t _type = org.omg.CORBA.ORB.init().create_union_tc(id(),\"" + className() + "\"," );
        ps.println( switch_type_spec.typeSpec().getTypeCodeExpression() + ", members);" );
        ps.println( "\t\t}" );
        ps.println( "\t\treturn _type;" );
        ps.println( "\t}" );

        ps.println( "}" ); // end of helper class
    }

    /** generate required classes */

    public void print( PrintWriter ps )
    {
        setPrintPhaseNames();

        /** no code generation for included definitions */
        if( included && !generateIncluded() )
            return;

        /** only write once */
        if( written )
        {
            return;
        }

        // Forward declaration
        if (switch_type_spec != null)
        {
            try
            {
                switch_body.print( ps );

                String className = className();

                String path = parser.out_dir + fileSeparator +
                pack_name.replace( '.', fileSeparator );

                File dir = new File( path );
                if( !dir.exists() )
                    if( !dir.mkdirs() )
                    {
                        org.jacorb.idl.parser.fatal_error( "Unable to create " + path, null );
                    }

                /** print the mapped java class */

                String fname = className + ".java";
                PrintWriter decl_ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
                printUnionClass( className, decl_ps );
                decl_ps.close();

                /** print the holder class */

                fname = className + "Holder.java";
                decl_ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
                printHolderClass( className, decl_ps );
                decl_ps.close();

                /** print the helper class */

                fname = className + "Helper.java";
                decl_ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
                printHelperClass( className, decl_ps );
                decl_ps.close();

                written = true;
            }
            catch( java.io.IOException i )
            {
                System.err.println( "File IO error" );
                i.printStackTrace();
            }
        }
    }

    private TypeSpec getElementType( ElementSpec element )
    {
        TypeSpec tspec = element.t;

        // Arrays are handled as a special case. Parser generates
        // an ArrayDeclarator for an array, need to spot this and
        // create appropriate type information with an ArrayTypeSpec.

        if( element.d.d instanceof ArrayDeclarator )
        {
            tspec = new ArrayTypeSpec( new_num(), tspec,
                    (ArrayDeclarator)element.d.d, pack_name );
            tspec.parse();
        }
        return tspec;
    }
}
