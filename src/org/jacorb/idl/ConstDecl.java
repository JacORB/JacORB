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

/**
 *
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

class ConstDecl extends Declaration
{
    private static Hashtable values = new Hashtable();
    private static Hashtable declarations = new Hashtable();

    private ScopedName t = new ScopedName( new_num() );
    private int pos_int_const = 0;
    private boolean int_const_set = false;

    public ConstExpr const_expr;
    public ConstType const_type;

    public ConstDecl( int num )
    {
        super( num );
    }

    public static void init()
    {
        values.clear();
        declarations.clear();
    }

    public static String namedValue( ScopedName sn )
    {
        String resolvedName = sn.resolvedName();
        if( values.containsKey( resolvedName ) )
        {
            return (String)values.get( resolvedName );
        }
        else
        {
            return resolvedName;
        }
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        super.setPackage( s );
        const_type.setPackage( s );
        const_expr.setPackage( s );
        t.typeName = name;
        t.setPackage( s );
    }

    public void parse()
    {
        const_expr.setDeclaration( this );
        try
        {
            NameTable.define( full_name(), "constant" );
        }
        catch( NameAlreadyDefined p )
        {
            parser.error( "Constant " + full_name() +
                    " already defined", token );
        }
        const_type.parse();
        const_expr.parse();
        t.typeName = name;
        values.put( t.resolvedName() +
                    ( contained() ? "" : ".value" ),
                    const_expr.value() );
        declarations.put( t.resolvedName(), this );
    }

    static ConstDecl getDeclaration( String resolvedName )
    {
        return (ConstDecl)declarations.get( resolvedName );
    }

    int pos_int_const()
    {
        if( !int_const_set )
        {
            pos_int_const = const_expr.pos_int_const();
            int_const_set = true;
        }
        return pos_int_const;
    }

    /**
     *  prints  a  constant  declaration  as  part  of  an  enclosing
     *  interface
     */

    public void printContained( PrintWriter ps )
    {
        TypeSpec ts = const_type.symbol.typeSpec();
        boolean alias = ts instanceof AliasTypeSpec;

        if( ts instanceof AliasTypeSpec )
        {
            ts = ( (AliasTypeSpec)ts ).originalType();
        }

        ps.print( "\t" + const_type + " " + name + " = " );
        if( ts instanceof IntType && ( (IntType)ts ).type_spec instanceof ShortType )
        {
            // short constant values have to be cast explicitly
            ps.print( "(short)(" );
            const_expr.print( ps );
            ps.println( ");" );
        }
        else if( ts instanceof LongLongType )
        {
            ps.print (const_expr.toString () + ';');
        }
        else if( ts instanceof FloatType )
        {
            // float constant values have to be cast explicitly
            ps.print( "(float)(" );
            const_expr.print( ps );
            ps.println( ");" );
        }
        else if( ts instanceof FixedPointConstType )
        {
            // fixed point values have to be created explicitly
            ps.print( "new java.math.BigDecimal(" );
            const_expr.print( ps );
            ps.println( ");" );
        }
        else if( ts instanceof OctetType )
        {
            // float constant values have to be cast explicitly
            ps.print( "(byte)(" );
            const_expr.print( ps );
            ps.println( ");" );
        }
        else
        {
            const_expr.print( ps );
            ps.println( ";" );
        }
    }

    boolean contained()
    {

        boolean result = false;
        IdlSymbol enc = getEnclosingSymbol();

        while( enc != null )
        {
            if( enc instanceof Interface )
            {
                result = true;
                break;
            }
            enc = enc.getEnclosingSymbol();
        }
        if( logger.isDebugEnabled() )
		 logger.debug( "ConstDecl.contained()? " + full_name()
                + " returns " + result );
        return result;
    }

    /** prints a constant declaration outside of an enclosing interface
     *  into a separate interface
     */

    public void print( PrintWriter ps )
    {
        if( contained() || ( included && !generateIncluded() ) )
            return;
        try
        {
            //new RuntimeException().printStackTrace();
            String fullName = ScopedName.unPseudoName( full_name() );
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
                    org.jacorb.idl.parser.fatal_error( "Unable to create " + path, null );
                }
            }

            String fname = className + ".java";
            PrintWriter pw =
                    new PrintWriter( new java.io.FileWriter(
                            new File( dir, fname ) ) );

            if( logger.isDebugEnabled() )
		 logger.debug( "ConstDecl.print " + fname );

            if( parser.checkJdk14 && pack_name.equals( "" ) )
                parser.fatal_error
                    ( "No package defined for " + className + " - illegal in JDK1.4", token );
            if( !pack_name.equals( "" ) )
                pw.println( "package " + pack_name + ";" );

            pw.println( "/**" );
            pw.println( " * Automatically generated from IDL const definition " );
            pw.println( " * @author JacORB IDL compiler " );
            pw.println( " */\n" );


            pw.println( "public interface " + className );
            pw.println( "{" );

            TypeSpec ts = const_type.symbol.typeSpec();
            if (ts instanceof AliasTypeSpec)
            {
                ts = ( (AliasTypeSpec)ts ).originalType();
            }

            pw.print( "\t" + const_type.toString() + " value = " );

            if( logger.isWarnEnabled() )
		 logger.warn( "ConstDecl, ts " +
                    const_type.toString() + " " + ts.getClass() );

            if( ts instanceof ShortType )
            {
                // short constant values have to be cast explicitly
                pw.print( "(short)(" + const_expr.toString() + ");" );
            }
            else if( ts instanceof LongLongType )
            {
                pw.print (const_expr.toString () + ';');
            }
            else if( ts instanceof FloatType )
            {
                // float constant values have to be cast explicitly
                pw.println( "(float)(" + const_expr.toString() + ");" );
            }
            else if( ts instanceof OctetType )
            {
                // float constant values have to be cast explicitly
                pw.println( "(byte)(" + const_expr.toString() + ");" );
            }
            else if( ts instanceof FixedPointConstType ||
                    ts instanceof FixedPointType )
            {
                pw.println( "new java.math.BigDecimal (" + const_expr.toString() + ");" );
            }
            else
            {
                pw.println( const_expr.toString() + ";" );
            }
            pw.println( "}" );
            pw.close();
        }
        catch( java.io.IOException i )
        {
            System.err.println( "File IO error" );
            i.printStackTrace();
        }
    }
}
