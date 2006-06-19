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

public class UnaryExpr
        extends IdlSymbol
{
    public String unary_op = "";
    public PrimaryExpr primary_expr;

    public UnaryExpr( int num )
    {
        super( num );
    }

    public void print( PrintWriter ps )
    {
        ps.print( unary_op );
        primary_expr.print( ps );
    }

    public void setDeclaration( ConstDecl declared_in )
    {
        primary_expr.setDeclaration( declared_in );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        primary_expr.setPackage( s );
    }

    public void parse()
    {
        primary_expr.parse();
    }

    int pos_int_const()
    {
        int y = primary_expr.pos_int_const();
        if( !unary_op.equals( "" ) )
        {
            if( unary_op.equals( "-" ) )
            {
                return y * -1;
            }
            return y;
        }
        return y;
    }

    public String value()
    {
        return unary_op + primary_expr.value();
    }

    public String toString()
    {
        return unary_op.toString() + primary_expr.toString();
    }

    public str_token get_token()
    {
        return primary_expr.get_token();
    }
}





