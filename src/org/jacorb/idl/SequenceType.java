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
    public int length = 0;

    public SequenceType( int num )
    {
        super( num );
        name = null;
        typedefd = false;
    }

    public Object clone()
    {
        SequenceType st = new SequenceType( IdlSymbol.new_num() );
        st.type_spec = this.type_spec;
        st.max = this.max;
        st.length = this.length;
        st.name = this.name;
        st.pack_name = this.pack_name;
        st.included = this.included;
        st.typedefd = this.typedefd;
        st.recursive = this.recursive;
        st.set_token( this.get_token() );
        st.setEnclosingSymbol( this.getEnclosingSymbol() );
        return st;
    }


    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
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

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        type_spec.setPackage( s );
        if( max != null )
            max.setPackage( s );
    }



    void setRecursive()
    {
        Environment.output( 2, "Sequence " + typeName +
                " set recursive ------- this: " + this );
        recursive = true;
    }

    /**
     * @returns a string for an expression of type TypeCode that describes this type
     */

    public String getTypeCodeExpression()
    {
        Environment.output( 1, "Sequence getTypeCodeExpression " + name );
        String originalType = null;

        if( recursive )
        {
            originalType = "org.omg.CORBA.ORB.init().create_sequence_tc(" +
                    length + ", org.omg.CORBA.ORB.init().create_recursive_tc(\"" +
                    elementTypeSpec().id() + "\"))";
        }
        else
        {
            originalType = "org.omg.CORBA.ORB.init().create_sequence_tc(" +
                    length + ", " + elementTypeExpression() + " )";
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

    public String printReadStatement( String var_name, String streamname )
    {
        Environment.output( 1, "Sequence printReadStatement for " + typeName() );

        StringBuffer sb = new StringBuffer();
        String type = typeName();
        String lgt = "_l" + var_name.replace( '.', '_' );

        // if [i] is part of the name, trim that off
        if( lgt.indexOf( "[" ) > 0 )
            lgt = lgt.substring( 0, lgt.indexOf( "[" ) ) + "_";

        // make local variable name unique
        lgt = lgt + getNumber();

        sb.append( "int " + lgt + " = " + streamname + ".read_long();\n" );
        if( length != 0 )
        {
            sb.append( "\t\tif(" + lgt + " > " + length + ")\n" );
            sb.append( "\t\t\tthrow new org.omg.CORBA.MARSHAL(\"Sequence length incorrect!\");\n" );
        }
        sb.append( "\t\t" + var_name + " = new " + type.substring( 0, type.indexOf( "[" ) ) +
                "[" + lgt + "]" + type.substring( type.indexOf( "]" ) + 1 ) + ";\n" );

        if( elementTypeSpec() instanceof BaseType &&
                !( elementTypeSpec() instanceof AnyType ) )
        {
            String _tmp = elementTypeSpec().printReadExpression( streamname );
            sb.append( "\t" + _tmp.substring( 0, _tmp.indexOf( "(" ) ) +
                    "_array(" + var_name + ",0," + lgt + ");" );
        }
        else
        {
            char idx_variable = 'i';
            String indent = "";
            if( var_name.endsWith( "]" ) )
            {
                idx_variable = (char)( var_name.charAt( var_name.length() - 2 ) + 1 );
                indent = "    ";
            }
            sb.append( "\t\t" + indent + "for(int " + idx_variable + "=0;" +
                    idx_variable + "<" + var_name + ".length;" + idx_variable + "++)\n\t\t" + indent + "{\n" );

            sb.append( "\t\t\t" + indent +
                    elementTypeSpec().printReadStatement( var_name +
                    "[" + idx_variable + "]",
                            streamname )
                    + "\n" );

            sb.append( "\t\t" + indent + "}\n" );

        }
        return sb.toString();
    }


    public String printWriteStatement( String var_name, String streamname )
    {
        StringBuffer sb = new StringBuffer();
        String type = typeName();
        if( length != 0 )
        {
            sb.append( "\t\tif( " + var_name + ".length > " + length + ")\n" );
            sb.append( "\t\t\tthrow new org.omg.CORBA.MARSHAL(\"Incorrect sequence length\");" );
        }
        sb.append( "\n\t\t" + streamname + ".write_long(" + var_name + ".length);\n" );

        if( elementTypeSpec() instanceof BaseType &&
                !( elementTypeSpec() instanceof AnyType ) )
        {
            String _tmp = elementTypeSpec().printWriteStatement( var_name, streamname );
            sb.append( "\t\t" + _tmp.substring( 0, _tmp.indexOf( "(" ) ) + "_array(" +
                    var_name + ",0," + var_name + ".length);" );
        }
        else
        {
            char idx_variable = 'i';
            String indent = "";
            if( var_name.endsWith( "]" ) )
            {
                idx_variable = (char)( var_name.charAt( var_name.length() - 2 ) + 1 );
                indent = "    ";
            }
            sb.append( "\t\t" + indent + "for( int " + idx_variable + "=0; " +
                    idx_variable + "<" + var_name + ".length;" +
                    idx_variable + "++)\n\t\t" + indent + "{\n" );

            sb.append( "\t\t\t" + indent +
                    elementTypeSpec().printWriteStatement( var_name
                    + "[" + idx_variable + "]",
                            streamname ) + "\n" );
            sb.append( "\t\t" + indent + "}\n" );
        }
        return sb.toString();
    }


    public String holderName()
    {
        if( !typedefd )
            throw new RuntimeException( "Compiler Error: should not be called (helpername on not typedef'd SequenceType " + name + ")" );

        String s = full_name();
        if( pack_name.length() > 0 )
        {
            if( !s.startsWith( "org.omg" ) )
            {
                s = omg_package_prefix + s;
            }
        }

        return s + "Holder";
    }


    public String helperName()
    {
        if( !typedefd )
            throw new RuntimeException( "Compiler Error: should not be called (helperName() on not typedef'd SequenceType)" );

        String s = full_name();
        if( pack_name.length() > 0 )
        {
            if( !s.startsWith( "org.omg" ) )
            {
                s = omg_package_prefix + s;
            }
        }

        return s + "Helper";
    }

    public String className()
    {
        String fullName = full_name();
        String cName;
        if( fullName.indexOf( '.' ) > 0 )
        {
            pack_name = fullName.substring( 0, fullName.lastIndexOf( '.' ) );
            cName = fullName.substring( fullName.lastIndexOf( '.' ) + 1 );
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
        if( max != null )
        {
            max.parse();
            length = Integer.parseInt( max.value() );
        }

        if( type_spec.typeSpec() instanceof ScopedName )
        {
            TypeSpec ts =
                    ( (ScopedName)type_spec.typeSpec() ).resolvedTypeSpec();
            if( ts != null )
                type_spec = ts;

            if( type_spec instanceof AliasTypeSpec )
                addImportedAlias( type_spec.full_name() );
            else
                addImportedName( type_spec.typeName() );

            addImportedName( type_spec.typeSpec().typeName() );
        }
        try
        {
            //NameTable.define( typeName(), "type" );
            NameTable.define( full_name(), "type" );
        }
        catch( NameAlreadyDefined n )
        {
            // ignore, sequence types can be defined a number
            // of times under different names
        }
    }

    public String full_name()
    {
        if( name == null )
        {
            return "<" + pack_name + ".anon>";
        }
        if( pack_name.length() > 0 )
            return ScopedName.unPseudoName( pack_name + "." + name );
        else
            return ScopedName.unPseudoName( name );
    }


    private void printClassComment( String className, PrintWriter ps )
    {
        ps.println( "/**" );
        ps.println( " *\tGenerated from IDL definition of sequence" +
                "\"" + className + "\"" );
        ps.println( " *\t@author JacORB IDL compiler " );
        ps.println( " */\n" );
    }

    private void printHolderClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";\n" );

        String type = typeName();

        printImport( ps );

        printClassComment( className, ps );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Holder" );
        ps.println( "\timplements org.omg.CORBA.portable.Streamable" );

        ps.println( "{" );
        ps.println( "\tpublic " + type + " value;" );
        ps.println( "\tpublic " + className + "Holder ()" );
        ps.println( "\t{" );
        ps.println( "\t}" );

        ps.println( "\tpublic " + className + "Holder (final " + type + " initial)\n\t{" );
        ps.println( "\t\tvalue = initial;" );
        ps.println( "\t}" );

        ps.println( "\tpublic org.omg.CORBA.TypeCode _type ()" );
        ps.println( "\t{" );
        ps.println( "\t\treturn " + className + "Helper.type ();" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _read (final org.omg.CORBA.portable.InputStream _in)" );
        ps.println( "\t{" );
        ps.println( "\t\tvalue = " + className + "Helper.read (_in);" );
        ps.println( "\t}" );

        ps.println( "\tpublic void _write (final org.omg.CORBA.portable.OutputStream _out)" );
        ps.println( "\t{" );
        ps.println( "\t\t" + className + "Helper.write (_out,value);" );
        ps.println( "\t}" );

        ps.println( "}" );
    }


    private void printHelperClass( String className, PrintWriter ps )
    {
        if( !pack_name.equals( "" ) )
            ps.println( "package " + pack_name + ";" );

        String type = typeName();
        printImport( ps );

        printClassComment( className, ps );

        ps.println( "public" + parser.getFinalString() + " class " + className + "Helper" );
        ps.println( "{" );
        ps.println( "\tprivate static org.omg.CORBA.TypeCode _type = " +
                getTypeCodeExpression() + ";" );

        TypeSpec.printHelperClassMethods( className, ps, type );
        printIdMethod( ps ); // from IdlSymbol

        /** read */

        ps.println( "\tpublic static " + type +
                " read (final org.omg.CORBA.portable.InputStream in)" );

        ps.println( "\t{" );
        ps.println( "\t\tint l = in.read_long();" );

        if( length != 0 )
        {
            ps.println( "\t\tif( l > " + length + ")" );
            ps.println( "\t\t\tthrow new org.omg.CORBA.MARSHAL();" );
        }

        ps.println( "\t\t" + type + " result = new " +
                type.substring( 0, type.indexOf( "[" ) ) + "[l]" +
                type.substring( type.indexOf( "]" ) + 1 ) + ";" );

        if( elementTypeSpec() instanceof BaseType &&
                !( elementTypeSpec() instanceof AnyType ) )
        {
            String _tmp = elementTypeSpec().printReadExpression( "in" );
            ps.println( "\t\t" + _tmp.substring( 0, _tmp.indexOf( "(" ) ) +
                    "_array(result,0,result.length);" );
        }
        else
        {
            ps.println( "\t\tfor( int i = 0; i < l; i++ )" );
            ps.println( "\t\t{" );
            ps.println( "\t\t\t" + elementTypeSpec().printReadStatement( "result[i]", "in" ) );
            ps.println( "\t\t}" );
        }

        ps.println( "\t\treturn result;" );
        ps.println( "\t}" );

        /* write */

        ps.println( "\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, "
                + "final " + type + " s)" );
        ps.println( "\t{" );
        if( length != 0 )
        {
            ps.println( "\t\tif( s.length > " + length + ")" );
            ps.println( "\t\t\tthrow new org.omg.CORBA.MARSHAL();" );
        }
        ps.println( "\t\tout.write_long(s.length);" );

        if( elementTypeSpec() instanceof BaseType &&
                !( elementTypeSpec() instanceof AnyType ) )
        {
            String _tmp = elementTypeSpec().printWriteStatement( "s", "out" );
            ps.println( _tmp.substring( 0, _tmp.indexOf( "(" ) ) + "_array(s,0,s.length);" );
        }
        else
        {
            ps.println( "\t\tfor( int i = 0; i < s.length; i++ )" );
            ps.println( "\t\t\t" + elementTypeSpec().printWriteStatement( "s[i]", "out" ) );
        }

        ps.println( "\t}" );
        ps.println( "}" );
    }


    public void print( PrintWriter _ps )
    {
        try
        {
            // only generate class files for explicitly
            // defined sequence types, i.e. for typedef'd ones

            if( ( !written ) && typedefd )
            {

                // write holder file

                String fullName = full_name();
                String className;
                if( fullName.indexOf( '.' ) > 0 )
                {
                    pack_name = fullName.substring( 0, fullName.lastIndexOf( '.' ) );
                    className = fullName.substring( fullName.lastIndexOf( '.' ) + 1 );
                }
                else
                {
                    pack_name = "";
                    className = fullName;
                }

                String path = parser.out_dir + fileSeparator +
                        pack_name.replace( '.', fileSeparator );

                File dir = new File( path );
                if( !dir.exists() )
                {
                    if( !dir.mkdirs() )
                    {
                        org.jacorb.idl.parser.fatal_error( "Unable to create " +
                                path, null );
                    }
                }

                String fname = className + "Holder.java";
                PrintWriter ps =
                        new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );

                printHolderClass( className, ps );
                ps.close();

                fname = className + "Helper.java";
                ps = new PrintWriter( new java.io.FileWriter( new File( dir, fname ) ) );
                printHelperClass( className, ps );
                ps.close();

                written = true;
            }
        }
        catch( java.io.IOException i )
        {
            System.err.println( "File IO error" );
            i.printStackTrace();
        }
    }


}




