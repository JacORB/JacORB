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

package org.jacorb.idl;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class EnumType
    extends TypeDeclaration
    implements SwitchTypeSpec
{
    public SymbolList enumlist;
    int const_counter = 0;
    private boolean written = false;
    private boolean parsed = false;

    public EnumType( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        EnumType et = new EnumType( new_num() );
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
     * @overrides  set_included from TypeDeclaration
     */

    public void set_included( boolean i )
    {
        included = i;
    }

    public String typeName()
    {
        if( typeName == null )
        {
            setPrintPhaseNames();
        }
        return typeName;
    }

    public boolean basic()
    {
        return true;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    public void parse()
    {
        if( parsed )
        {
            return;
        }
        parsed = true;

        escapeName();

        try
        {
            ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
            ctspec.c_type_spec = this;
            NameTable.define( full_name(), "type" );
            TypeMap.typedef( full_name(), ctspec );
            String enum_ident = null;

            // we have to get the scoping right: enums do not
            // define scopes, but their element identifiers are scoped.
            // for the Java mapping, we need to get the enum type name
            // back as it defines the class name where the constants
            // are defined. Therefore, an additional mapping in
            // ScopedName is required.

            String prefix = ( pack_name.length() > 0 ? 
                              full_name().substring( 0, full_name().lastIndexOf('.')+1 ) : 
                              "" );
            //           String prefix = ( pack_name.length() > 0 ? pack_name + "." : "" );

            for( Enumeration e = enumlist.v.elements(); e.hasMoreElements(); )
            {
                enum_ident = (String)e.nextElement();
                try
                {
                    NameTable.define( prefix + enum_ident, "enum label" );
                    ScopedName.enumMap( prefix + enum_ident, full_name() +
                                        "." + enum_ident );
                }
                catch( NameAlreadyDefined p )
                {
                    parser.error( "Identifier " + enum_ident +
                            " already defined \n" +
                            "(Enums don't define new scopes in IDL)",
                            token );
                }
            }
        }
        catch( NameAlreadyDefined p )
        {
            parser.error( "Enum " + full_name() + " already defined", token );
        }
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

    public String printReadExpression( String streamname )
    {
        return toString() + "Helper.read(" + streamname + ")";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return toString() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }


    public String getTypeCodeExpression()
    {
        return full_name() + "Helper.type()";
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
        ps.println( " *\tGenerated from IDL definition of enum " +
                "\"" + className + "\"" );
        ps.println( " *\t@author JacORB IDL compiler " );
        ps.println( " */\n" );
    }

    private void printHolderClass( String className, PrintWriter ps )
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
        ps.println( "\t\t" + className + "Helper.write (out,value);" );
        ps.println( "\t}" );

        ps.println( "}" );
    }

    private void printHelperClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";" );

        printClassComment( className, ps );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Helper" );
        ps.println( "{" );

        ps.println( "\tprivate static org.omg.CORBA.TypeCode _type = null;");

        /* type() method */
        ps.println( "\tpublic static org.omg.CORBA.TypeCode type ()" );
        ps.println( "\t{" );
        ps.println( "\t\tif( _type == null )" );
        ps.println( "\t\t{" );

        StringBuffer sb = new StringBuffer();
        sb.append( "org.omg.CORBA.ORB.init().create_enum_tc(" +
                typeName() + "Helper.id(),\"" + className() + "\"," );

        sb.append( "new String[]{" );

        for( Enumeration e = enumlist.v.elements(); e.hasMoreElements(); )
        {
            sb.append( "\"" + (String)e.nextElement() + "\"" );
            if( e.hasMoreElements() )
                sb.append( "," );
        }
        sb.append( "})" );

        ps.println("\t\t\t_type = " + sb.toString() + ";" );
        ps.println( "\t\t}" );
        ps.println( "\t\treturn _type;" );
        ps.println( "\t}\n" );

        String type = typeName();

        TypeSpec.printInsertExtractMethods( ps, type );
        printIdMethod( ps );

        ps.println( "\tpublic static " + className + " read (final org.omg.CORBA.portable.InputStream in)" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + className + ".from_int( in.read_long());" );
        ps.println( "\t}\n" );

        ps.println( "\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, final " + className + " s)" );
        ps.println( "\t{" );
        ps.println( "\t\tout.write_long(s.value());" );
        ps.println( "\t}" );
        ps.println( "}" );
    }

    /** print the class that maps the enum */

    private void printEnumClass( String className, PrintWriter pw )
    {
        if( !pack_name.equals( "" ) )
            pw.println( "package " + pack_name + ";" );

        printClassComment( className, pw );

        pw.println( "public" + parser.getFinalString() + " class " + className );
        pw.println( "\timplements org.omg.CORBA.portable.IDLEntity\n{" );

        pw.println( "\tprivate int value = -1;" );

        for( Enumeration e = enumlist.v.elements(); e.hasMoreElements(); )
        {
            String label = (String)e.nextElement();
            pw.println( "\tpublic static final int _" + label + " = " + ( const_counter++ ) + ";" );
            pw.println( "\tpublic static final " + name + " " + label + " = new " + name + "(_" + label + ");" );
        }
        pw.println( "\tpublic int value()" );
        pw.println( "\t{" );
        pw.println( "\t\treturn value;" );
        pw.println( "\t}" );

        pw.println( "\tpublic static " + name + " from_int(int value)" );
        pw.println( "\t{" );
        pw.println( "\t\tswitch (value) {" );

        for( Enumeration e = enumlist.v.elements(); e.hasMoreElements(); )
        {
            String label = (String)e.nextElement();
            pw.println( "\t\t\tcase _" + label + ": return " + label + ";" );
        }
        pw.println( "\t\t\tdefault: throw new org.omg.CORBA.BAD_PARAM();" );
        pw.println( "\t\t}" );
        pw.println( "\t}" );

        pw.println( "\tprotected " + name + "(int i)" );
        pw.println( "\t{" );
        pw.println( "\t\tvalue = i;" );
        pw.println( "\t}" );

//          pw.println("\tpublic boolean equals( java.lang.Object other )");
//          pw.println("\t{");
//          pw.println("\t\treturn ( other instanceof " + className + " ) && value == ((" + className + " )other).value();");
//          pw.println("\t}");
//
//          pw.println("\tpublic int hashCode()");
//          pw.println("\t{");
//          pw.println("\t\treturn ( \"" + pack_name + "." + className + "\" + value()).hashCode();");
//          pw.println("\t}");

        pw.println( "\tjava.lang.Object readResolve()" );
        pw.println( "\tthrows java.io.ObjectStreamException" );
        pw.println( "\t{" );
        pw.println( "\t\treturn from_int( value() );" );
        pw.println( "\t}" );
        pw.println( "}" );
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
            return;

        try
        {
            String className = className();

            String path = parser.out_dir + fileSeparator + pack_name.replace( '.', fileSeparator );
            File dir = new File( path );
            if( !dir.exists() )
            {
                if( !dir.mkdirs() )
                {
                    org.jacorb.idl.parser.fatal_error( "Unable to create " + path, null );
                }
            }

            /** print the mapped java class */

            String fname = className + ".java";
            PrintWriter decl_ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
            printEnumClass( className, decl_ps );
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

    public String toString()
    {
        return typeName();
    }

    public boolean isSwitchable()
    {
        return true;
    }
}


