package org.jacorb.idl;
 
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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


import java.util.*;
import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
*/

class AndExpr 
    extends IdlSymbol
{
    public AndExpr and_expr = null;
    public ShiftExpr shift_expr;

    public AndExpr(int num)
    {
        super(num);
    }

    public void print(PrintWriter ps )
    {
        if( and_expr != null )
        {
            and_expr.print(ps);
            ps.print(" & ");
        }
        shift_expr.print(ps);
    }


    public void setDeclaration( ConstDecl declared_in )
    {
        shift_expr.setDeclaration( declared_in );
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        if( and_expr != null )
        {
            and_expr.setPackage(s);
        }
        shift_expr.setPackage( s);
    }

    public void parse()  
    {
        if( and_expr != null )
        {
            and_expr.parse();
        }
        shift_expr.parse();
    }

    int pos_int_const()
    {
        return shift_expr.pos_int_const();
    }

    public String  value() 
    {
        String x = "";
        if( and_expr != null )
        {
            x = and_expr.value() + "&";
        }
        return x + shift_expr.value();
    }

    public String  toString() 
    {
        String x = "";
        if( and_expr != null )
        {
            x = and_expr + "&";
        }
        return x + shift_expr;
    }

}




