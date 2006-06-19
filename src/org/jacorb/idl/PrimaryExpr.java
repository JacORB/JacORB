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

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class PrimaryExpr
    extends IdlSymbol
{
    public IdlSymbol symbol;

    public PrimaryExpr( int num )
    {
        super( num );
    }

    public void print( PrintWriter ps )
    {
        if( symbol instanceof ConstExpr )
        {
            ps.print( "(" );
            symbol.print( ps );
            ps.print( ")" );
        }
        else if( symbol instanceof ScopedName )
        {
            ps.print( ( (ScopedName)symbol ).resolvedName() );
        }
        else
        {
            // Literal
            symbol.print( ps );
        }
    }

    public void parse()
    {
        symbol.parse();
    }

    public void setDeclaration( ConstDecl declared_in )
    {
        if( symbol instanceof Literal )
        {
            ( (Literal)symbol ).setDeclaration( declared_in );
        }
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
        {
            pack_name = s + "." + pack_name;
        }
        else
        {
            pack_name = s;
        }

        symbol.setPackage( s );
    }

    int pos_int_const()
    {
        if( symbol instanceof ConstExpr )
        {
            return ( (ConstExpr)symbol ).pos_int_const();
        }
        else if( symbol instanceof ScopedName )
        {
            ConstExprEvaluator eval =
                new ConstExprEvaluator( ConstDecl.namedValue( (ScopedName)symbol ));
            if( logger.isDebugEnabled() )
            {
                logger.debug( "PrimaryExpr: returning value " + eval.getValue().intValue());
            }

            return eval.getValue().intValue();
        }
        return Integer.parseInt( ( (Literal)symbol ).toString() );
    }

    public String value()
    {
        if( symbol instanceof ConstExpr )
        {
            return "(" + ( (ConstExpr)symbol ).value() + ")";
        }
        else if( symbol instanceof ScopedName )
        {
            return ConstDecl.namedValue( (ScopedName)symbol );
        }
        return ( (Literal)symbol ).toString();
    }

    public String toString()
    {
        if( symbol instanceof ConstExpr )
        {
            return "(" + ( (ConstExpr)symbol ).toString() + ")";
        }
        else if( symbol instanceof ScopedName )
        {
            return ( (ScopedName)symbol ).resolvedName();
        }
        else
        {
            return ( (Literal)symbol ).toString();
        }
    }

    public str_token get_token()
    {
        return symbol.get_token();
    }
}
