package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import org.jacorb.idl.runtime.long_token;
import org.jacorb.idl.runtime.int_token;
import java.math.BigInteger;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class Literal
    extends IdlSymbol
{
    private static BigInteger maximum;
    public String string;
    public boolean wide;
    public org.jacorb.idl.runtime.token token;

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

            // If its unsigned may need to fit the value into a signed
            // value.
            if (ts instanceof IntType)
            {
                if (((IntType)ts).unsigned &&
                    ts instanceof LongLongType &&
                    token instanceof fixed_token)
                {
                    // Need to reset the unsigned value to fit into a
                    // signed long value.
                    if (maximum == null)
                    {
                        maximum = new BigInteger ("18446744073709551615");
                    }
                    BigInteger current = new BigInteger (string);
                    if (current.compareTo (maximum ) > 0)
                    {
                        parser.error ("Value too big for unsigned long");
                    }
                    else
                    {
                        token = new long_token
                        (
                            ((fixed_token)token).sym,
                            ((fixed_token)token).fixed_val.longValue ()
                        );
                        string = Long.toString (((long_token)token).long_val);
                    }
                }
                else if (((IntType)ts).unsigned == true &&
                         ts instanceof LongType &&
                         token instanceof long_token)
                {
                    // Need to reset the unsigned value to fit into a
                    // signed integer value. Will need to replace the
                    // token so it is a int_token not a long.
                    if (((long_token)token).long_val > 4294967295L )
                    {
                        parser.error
                        (
                            "Value (" +
                            ((long_token)token).long_val +
                            ") too big for unsigned long"
                        );
                    }
                    else
                    {
                        token = new int_token
                        (
                            ((long_token)token).sym,
                            (int)((long_token)token).long_val
                        );
                        string = Integer.toString (((int_token)token).int_val);
                    }
                }
                // Not unsigned but still have a long token for a Java Int type
                else if (ts instanceof LongType && token instanceof long_token)
                {
                    parser.error
                    (
                        "Value (" +
                        ((long_token)token).long_val +
                        ") too big for Java int"
                    );
                }
                // Not unsigned but still have a fixed_token for a Java Long type
                else if (ts instanceof LongLongType && token instanceof fixed_token)
                {
                    parser.error
                    (
                        "Value (" +
                        ((fixed_token)token).fixed_val.toString () +
                        ") too big for Java long"
                    );
                }
            }

            if( logger.isWarnEnabled() )
                logger.warn( "Literal " + ts.getClass().getName() + " " +
                             ( token != null? token.getClass().getName() :"<no token>" ) );

            if( ts instanceof FloatPtType &&
                    !( token instanceof org.jacorb.idl.runtime.float_token ) )
            {
                parser.error( "Expecting float/double constant!" );
            }
            else if( ts instanceof FixedPointConstType &&
                    !( token instanceof fixed_token ) )
            {
                parser.error( "Expecting fixed point constant (perhaps a missing \"d\")!" );
            }
            else if( ts instanceof StringType )
            {
                if( wide && !( (StringType)ts ).isWide() )
                    parser.error( "Illegal assignment of wide string constant to string!" );

            }
            else if( ( ts instanceof LongType ) || ( ts instanceof ShortType ) )
            {
                if( token instanceof org.jacorb.idl.runtime.long_token )
                {
                    parser.error( "Illegal assignment from long long" );
                }
            }
        }
    }

    public String toString()
    {
        String result = string;

        if (token instanceof org.jacorb.idl.runtime.long_token)
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
