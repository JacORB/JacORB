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


import java.io.File;
import java.io.PrintWriter;

import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class ValueBoxDecl
    extends Value
{
    private boolean written = false;
    private boolean parsed = false;

    TypeSpec typeSpec;

    public ValueBoxDecl( int num )
    {
        super( num );
        pack_name = "";
    }

    public Object clone()
    {
        return null;
    }

    public TypeDeclaration declaration()
    {
        return this;
    };

    public String typeName()
    {
        if( typeName == null )
            setPrintPhaseNames();

        if( typeSpec.typeSpec() instanceof BaseType )
            return typeName;
        else
            return typeSpec.typeSpec().typeName();
    }

    public String boxTypeName()
    {
        if( typeName == null )
            setPrintPhaseNames();
        return typeName;
    }

    public boolean basic()
    {
        return false;
    }


    public void set_included( boolean i )
    {
        included = i;
    }


    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        typeSpec.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            System.err.println( "was " + enclosing_symbol.getClass().getName() +
                    " now: " + s.getClass().getName() );
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        }
        enclosing_symbol = s;
    }


    public String toString()
    {
        return typeName();
    }


    public void parse()
    {
        if( parsed )
            throw new RuntimeException( "Compiler error: Value box already parsed!" );

        escapeName();

        typeSpec.parse();

        try
        {
            ConstrTypeSpec ctspec = new ConstrTypeSpec( new_num() );
            ctspec.c_type_spec = this;

            NameTable.define( full_name(), "type" );
            TypeMap.typedef( full_name(), ctspec );
        }
        catch( NameAlreadyDefined nad )
        {
            Environment.output( 4, nad );
            parser.error( "Value box " + typeName() + " already defined", token );
        }

        parsed = true;
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
        return "(" + typeName() + ")((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ").read_value (new " + helperName() + "())";
    }

    public String printWriteStatement( String var_name, String streamname )
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ").write_value (" + var_name + ", new " + helperName() + "());";
    }

    public String holderName()
    {
        return boxTypeName() + "Holder";
    }

    public String helperName()
    {
        return boxTypeName() + "Helper";
    }

    /**
     * @returns a string for an expression of type TypeCode that describes this type
     */

    public String getTypeCodeExpression( Set knownTypes )
    {
        if( knownTypes.contains( this ) )
        {
            return this.getRecursiveTypeCodeExpression();
        }
        else
        {
            knownTypes.add( this );
            StringBuffer sb = new StringBuffer();
            String className = boxTypeName();
            if( className.indexOf( '.' ) > 0 )
                className = className.substring( className.lastIndexOf( '.' ) + 1 );
            sb.append( "org.omg.CORBA.ORB.init().create_value_box_tc(" +
                       helperName() + ".id(),\"" + className + "\"," +
                       typeSpec.typeSpec().getTypeCodeExpression() + ")" );

            return sb.toString();
        }
    }

    public String getTypeCodeExpression()
    {
        return this.getTypeCodeExpression( new HashSet() );
    }

    private void printHolderClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";" );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Holder" );
        ps.println( "\timplements org.omg.CORBA.portable.Streamable" );
        ps.println( "{" );

        ps.println( "\tpublic " + typeName() + " value;\n" );

        ps.println( "\tpublic " + className + "Holder ()" );
        ps.println( "\t{" );
        ps.println( "\t}" );

        ps.println( "\tpublic " + className + "Holder (final " + typeName() + " initial)" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = initial;" );
        ps.println( "\t}" );

        ps.println( "\tpublic org.omg.CORBA.TypeCode _type ()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + helperName() + ".type ();" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _read (final org.omg.CORBA.portable.InputStream _in)" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = " + helperName() + ".read (_in);" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _write (final org.omg.CORBA.portable.OutputStream _out)" );
        ps.println( "\t{" );
        ps.println( "\t\t" + helperName() + ".write (_out,value);" );
        ps.println( "\t}" );

        ps.println( "}" );
    }


    private void printHelperClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";" );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Helper" );
        ps.println( "\timplements org.omg.CORBA.portable.BoxedValueHelper" );
        ps.println( "{" );
        ps.println( "\tprivate static org.omg.CORBA.TypeCode _type = " + getTypeCodeExpression() + ";" );

        String type = typeName();

        TypeSpec.printHelperClassMethods( className, ps, type );

        printIdMethod( ps ); // inherited from IdlSymbol

        /* read */
        ps.println( "\tpublic static " + type + " read (final org.omg.CORBA.portable.InputStream in)" );
        ps.println( "\t{" );
        if( typeSpec.typeSpec() instanceof BaseType )
            ps.println( "\t\t" + type + " result = new " + type + "(" + typeSpec.typeSpec().printReadExpression( "in" ) + ");" );
        else
        {
            ps.println( "\t\t" + type + " result;");
            ps.println( "\t\t" + typeSpec.typeSpec().printReadStatement("result", "in" ) + ";" );
            //ps.println( "\t\t" + type + " result = " + typeSpec.typeSpec().printReadExpression( "in" ) + ";" );
        }
        ps.println( "\t\treturn result;" );
        ps.println( "\t}" );

        /* write */
        ps.println( "\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, final " + type + " s)" );
        ps.println( "\t{" );
        if( typeSpec.typeSpec() instanceof BaseType )
            ps.println( "\t\t" + typeSpec.typeSpec().printWriteStatement( "s.value", "out" ) );
        else
            ps.println( "\t\t" + typeSpec.typeSpec().printWriteStatement( "s", "out" ) );
        ps.println( "\t}" );

        ps.println( "\tpublic java.io.Serializable read_value (final org.omg.CORBA.portable.InputStream is)" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + helperName() + ".read (is);" );
        ps.println( "\t}" );

        ps.println( "\tpublic void write_value (final org.omg.CORBA.portable.OutputStream os, final java.io.Serializable value)" );
        ps.println( "\t{" );
        ps.println( "\t\t" + helperName() + ".write (os, (" + type + ")value);" );
        ps.println( "\t}" );

        ps.println( "\tpublic java.lang.String get_id()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + helperName() + ".id();" );
        ps.println( "\t}" );
        ps.println( "}" );
    }

    private void printValueClass( String className, PrintWriter ps )
    {
        String fullClassName = className;

        if( !pack_name.equals( "" ) )
        {
            fullClassName = pack_name + "." + className;

            ps.println( "package " + pack_name + ";" );
        }

        ps.println( "public class " + className );
        ps.println( "\timplements org.omg.CORBA.portable.ValueBase" );
        ps.println( "{" );
        ps.println( "\tpublic " + typeSpec.typeName() + " value;" );
        ps.println( "\tprivate static String[] _ids = { " + className + "Helper.id() };" );

        ps.println( "\tpublic " + className + "(" + typeSpec.typeName() + " initial )" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = initial;" );
        ps.println( "\t}" );

        ps.println( "\tpublic String[] _truncatable_ids()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn _ids;" );
        ps.println( "\t}" );

        ps.println( "}" );
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

        written = true;

        try
        {
            String className = boxTypeName();
            if( className.indexOf( '.' ) > 0 )
                className = className.substring( className.lastIndexOf( '.' ) + 1 );

            String path = parser.out_dir + fileSeparator +
                    pack_name.replace( '.', fileSeparator );

            File dir = new File( path );
            if( !dir.exists() )
                if( !dir.mkdirs() )
                {
                    org.jacorb.idl.parser.fatal_error( "Unable to create " + path, null );
                }

            /** print the mapped java class */

            PrintWriter decl_ps;
            String fname = className + ".java";

            if( typeSpec.typeSpec() instanceof BaseType )
            {
                decl_ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
                printValueClass( className, decl_ps );
                decl_ps.close();
            }

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



