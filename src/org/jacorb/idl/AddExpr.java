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

class AddExpr
    extends IdlSymbol
{
    public AddExpr add_expr = null;
    public String operator;
    public MultExpr mult_expr;

    public AddExpr( int num )
    {
        super( num );
    }

    public void print( PrintWriter ps )
    {
        if( add_expr != null )
        {
            add_expr.print( ps );
            ps.print( operator );
        }
        mult_expr.print( ps );
    }


    public void setDeclaration( ConstDecl declared_in )
    {
        mult_expr.setDeclaration( declared_in );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        if( add_expr != null )
        {
            add_expr.setPackage( s );
        }
        mult_expr.setPackage( s );
    }

    public void parse()
    {
        if( add_expr != null )
        {
            add_expr.parse();
        }
        mult_expr.parse();
    }

    int pos_int_const()
    {
        int y = mult_expr.pos_int_const();
        if( add_expr != null )
        {
            int z = add_expr.pos_int_const();
            if( operator.equals( "-" ) )
                z *= -1;
            return z + y;
        }
        else
            return y;
    }

    public String value()
    {
        String x = "";
        if( add_expr != null )
        {
            x = add_expr.value() + operator;
        }
        return x + mult_expr.value();
    }


    public String toString()
    {
        String x = "";
        if( add_expr != null )
        {
            x = add_expr.toString () + ' ' + operator + ' ';
        }
        return x + mult_expr;
    }

    public str_token get_token()
    {
        return mult_expr.get_token();
    }
}

