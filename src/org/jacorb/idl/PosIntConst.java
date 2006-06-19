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

/**
 * Represents positive integer constants, used in array sizes and
 * sequence bounds declarations.
 *
 * @author Gerald Brose
 * @version $Id$
 */

public class PosIntConst
    extends IdlSymbol
{
    private int value = -1;
    ConstExpr const_expr;

    public PosIntConst( int num )
    {
        super( num );
    }

    void setExpression( ConstExpr const_expr )
    {
        this.const_expr = const_expr;
    }

    public void parse()
    {
        const_expr.parse();
    }

    public int value()
    {
        if( value == -1 )
        {
            value = const_expr.pos_int_const();
            if( value <= 0 )
                throw new ParseException("Integer constant value must be greater 0.",
                                         this.myPosition );
        }
        return value;
    }

    public String toString()
    {
        return const_expr.toString();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        const_expr.setPackage( s );
    }
}
