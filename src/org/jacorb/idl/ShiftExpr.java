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

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.PrintWriter;

public class ShiftExpr
        extends IdlSymbol
{
    public ShiftExpr shift_expr = null;
    public AddExpr add_expr;
    public String operator;

    public ShiftExpr( int num )
    {
        super( num );
    }

    public void print( PrintWriter ps )
    {
        if( shift_expr != null )
        {
            shift_expr.print( ps );
            ps.print( operator );
        }
        add_expr.print( ps );
    }


    public void setDeclaration( ConstDecl declared_in )
    {
        add_expr.setDeclaration( declared_in );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        if( shift_expr != null )
        {
            shift_expr.setPackage( s );
        }
        add_expr.setPackage( s );
    }

    public void parse()
    {
        if( shift_expr != null )
        {
            shift_expr.parse();
        }
        add_expr.parse();
    }

    int pos_int_const()
    {
        return add_expr.pos_int_const();
    }

    public String toString()
    {
        String x = "";
        if( shift_expr != null )
        {
            x = shift_expr + operator;
        }
        return x + add_expr;
    }

    public str_token get_token()
    {
        return add_expr.get_token();
    }
}
