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

import java.io.PrintWriter;
import java.math.BigInteger;
import org.jacorb.idl.runtime.int_token;
import org.jacorb.idl.runtime.long_token;
import org.jacorb.idl.runtime.token;

/**
 * @author Gerald Brose
 */

public class Literal
    extends IdlSymbol
{
    private static final BigInteger IDL_SHORT_MIN = new BigInteger ("-32768");
    private static final BigInteger IDL_SHORT_MAX = new BigInteger ("32768");
    private static final BigInteger IDL_UNSIGNED_MIN = new BigInteger ("0");
    private static final BigInteger IDL_UNSIGNED_SHORT_MAX = new BigInteger ("65535");
    private static final BigInteger IDL_LONG_MIN = new BigInteger ("-2147483648");
    private static final BigInteger IDL_LONG_MAX = new BigInteger ("2147483647");
    private static final BigInteger IDL_UNSIGNED_LONG_MAX = new BigInteger ("4294967295");
    private static final BigInteger IDL_LONG_LONG_MIN = new BigInteger ("-9223372036854775808");
    private static final BigInteger IDL_LONG_LONG_MAX = new BigInteger ("9223372036854775807");
    private static final BigInteger IDL_UNSIGNED_LONG_LONG_MAX = new BigInteger ("18446744073709551615");

    public String string;
    public boolean wide;
    public token primitiveToken;

    private ConstDecl declared_in;

    public Literal( int num )
    {
        super( num );
    }


    public void setDeclaration( ConstDecl declared_in )
    {
        this.declared_in = declared_in;
    }

    public void parse()
    {
        // const expressions containing literals can be declared
        // outside cons declarations (e.g, in sequence bounds),
        // but we care only for const declarations here.

        if( declared_in != null )
        {
            TypeSpec ts = declared_in.const_type.symbol.typeSpec();
            // If its an alias check the actual type not the alias
            if (ts instanceof AliasTypeSpec)
            {
                ts = ((AliasTypeSpec)ts).originalType ();
            }

            if( logger.isWarnEnabled() )
                logger.warn( "Literal " + ts.getClass().getName() + " " +
                             ( primitiveToken != null? primitiveToken.getClass().getName() :"<no token>" ) );

            // At first check the float types and strings
            if( ts instanceof FloatPtType &&
                    !( primitiveToken instanceof org.jacorb.idl.runtime.float_token ) )
            {
                parser.error( "Expecting float/double constant!" );
            }
            else if( ts instanceof FixedPointConstType &&
                    !( primitiveToken instanceof fixed_token ) )
            {
                parser.error( "Expecting fixed point constant (perhaps a missing \"d\")!" );
            }
            else if( ts instanceof StringType )
            {
                if( wide && !( (StringType)ts ).isWide() )
                    parser.error( "Illegal assignment of wide string constant to string!" );
            }
            else if( ts instanceof IntType )
            {
                // COS578 constant value check was reworked

                // convert constant for the comparison
                BigInteger value = null;

                if( primitiveToken instanceof int_token
                    || primitiveToken instanceof long_token
                    || primitiveToken instanceof fixed_token )
                {
                    value = new BigInteger( string );
                }
                else
                {
                    parser.error( "Illegal assignment to '" + TypeSpec.getIDLType (ts)
                        + "' of '" + string + "' value" );

                    // do not check further conditions
                    return;
                }

                // check the unsigned values first
                if( ( (IntType)ts ).unsigned )
                {
                    if( value.compareTo( IDL_UNSIGNED_MIN ) < 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too small for unsigned type" );
                    }
                    else if( ts instanceof LongLongType )
                    {
                        if( value.compareTo( IDL_UNSIGNED_LONG_LONG_MAX ) > 0 )
                        {
                            parser.error( "Value " + value.toString()
                                    + " is too big for unsigned long long" );
                        }
                        else if( primitiveToken instanceof fixed_token )
                        {
                            primitiveToken = new long_token
                            (
                                ((fixed_token)primitiveToken).sym,
                                ((fixed_token)primitiveToken).fixed_val.longValue ()
                            );
                            string = Long.toString (((long_token)primitiveToken).long_val);
                        }
                    }
                    else if( ts instanceof LongType )
                    {
                        if( value.compareTo (IDL_UNSIGNED_LONG_MAX) > 0 )
                        {
                            parser.error( "Value " + value.toString() + " is too big for unsigned long" );
                        }
                        else if( primitiveToken instanceof long_token )
                        {
                            primitiveToken = new int_token
                            (
                                ((long_token)primitiveToken).sym,
                                (int)((long_token)primitiveToken).long_val
                            );
                            string = Integer.toString (((int_token)primitiveToken).int_val);
                        }
                    }
                    else if( ts instanceof ShortType
                                && value.compareTo( IDL_UNSIGNED_SHORT_MAX ) > 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too big for unsigned long" );
                    }
                }
                // then checking the signed type ranges
                else if( ts instanceof LongLongType )
                {
                    if( value.compareTo( IDL_LONG_LONG_MIN ) < 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too small for long long type" );
                    }
                    else if( value.compareTo( IDL_LONG_LONG_MAX ) > 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too big for long long type" );
                    }
                }
                else if( ts instanceof LongType )
                {
                    if( value.compareTo (IDL_LONG_MIN) < 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too small for long type" );
                    }
                    else if( value.compareTo (IDL_LONG_MAX) > 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too big for long type" );
                    }
                }
                else if (ts instanceof ShortType)
                {
                    if( value.compareTo (IDL_SHORT_MIN) < 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too small for short type" );
                    }
                    else if( value.compareTo (IDL_SHORT_MAX) > 0 )
                    {
                        parser.error( "Value " + value.toString() + " is too big for short type" );
                    }
                }
            }
        }
    }

    public String toString()
    {
        String result = string;

        if (primitiveToken instanceof org.jacorb.idl.runtime.long_token)
        {
            if (string.indexOf( '.' ) > 0 )
            {
                result = (string + 'D');
            }
            else
            {
                result = (string + 'L');
            }
        }
        return escapeBackslash (result);
    }

    public void print( PrintWriter ps )
    {
        ps.print( escapeBackslash( string  ));
    }


    /**
     * Doubles up instances of the backslash character in a
     * string, to avoid them being interpreted as escape sequences
     *
     * @param name a <code>String</code> value
     * @return string
     */
    public static String escapeBackslash( String name )
    {
        StringBuffer result = new StringBuffer();

        char[] chrs = name.toCharArray();

        // Don't bother escaping if we have "xxx"
        if( chrs[ 0 ] == '\"' )
        {
            return name;
        }

        for( int i = 0; i < chrs.length; i++ )
        {
            switch( chrs[ i ] )
            {
                case '\n':
                    {
                        result.append( '\\' );
                        result.append( 'n' );
                        break;
                    }
                case '\t':
                    {
                        result.append( '\\' );
                        result.append( 't' );
                        break;
                    }
                case '\013':
                    {
                        result.append( '\\' );
                        result.append( "013" );
                        break;
                    }
                case '\b':
                    {
                        result.append( '\\' );
                        result.append( 'b' );
                        break;
                    }
                case '\r':
                    {
                        result.append( '\\' );
                        result.append( 'r' );
                        break;
                    }
                case '\f':
                    {
                        result.append( '\\' );
                        result.append( 'f' );
                        break;
                    }
                case '\007':
                    {
                        result.append( '\\' );
                        result.append( "007" );
                        break;
                    }
                case '\\':
                    {
                        result.append( '\\' );
                        result.append( '\\' );
                        break;
                    }
                case '\0':
                    {
                        result.append( '\\' );
                        result.append( '0' );
                        break;
                    }
                case '\'':
                    {
                        if( i == 1 )
                        {
                            result.append( '\\' );
                            result.append( '\'' );
                        }
                        else
                        {
                            result.append( chrs[ i ] );
                        }
                        break;
                    }
                case '\"':
                    {
                        if( i == 1 )
                        {
                            result.append( '\\' );
                            result.append( '\"' );
                        }
                        else
                        {
                            result.append( chrs[ i ] );
                        }
                        break;
                    }
                default:
                    {
                        result.append( chrs[ i ] );
                    }
            }
        }
        return result.toString();
    }
}
