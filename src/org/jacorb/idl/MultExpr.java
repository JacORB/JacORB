package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
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

class MultExpr 
    extends IdlSymbol
{
    public String operator;
    public MultExpr mult_expr = null;
    public UnaryExpr unary_expr;

    public MultExpr(int num)
    {
        super(num);
    }
 
    public void print(PrintWriter ps)
    {
        if( mult_expr != null ){
            mult_expr.print(ps);
            ps.print( operator );
        } 
        unary_expr.print(ps);
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        if( mult_expr != null ){
            mult_expr.setPackage(s);
        }
        unary_expr.setPackage( s);
    }

    public void parse()  
    {
        if( mult_expr != null ){
            mult_expr.parse();
        }
        unary_expr.parse();
    }

    int pos_int_const()
    {
        int y = unary_expr.pos_int_const();
        if( mult_expr != null ){
            int z = mult_expr.pos_int_const();
            if( operator.equals("*"))
                y *= z;
            else if( operator.equals("/"))
                y /= z;
            else if( operator.equals("%"))
                y %= z;
        } 
        return y;
    }
    
    public String  value() 
    {
        String x = "";
        if( mult_expr != null )
        {
            x = mult_expr.value() + operator;
        }
        return x + unary_expr.value();
    }
}





















