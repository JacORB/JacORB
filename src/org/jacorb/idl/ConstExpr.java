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

import java.io.PrintWriter;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class ConstExpr 
    extends IdlSymbol
{
    public OrExpr or_expr;
    
    public ConstExpr(int num)
    {
        super(num);
    }

    public void parse() 
         
    {
        or_expr.parse();
    }

    public void print(PrintWriter ps)
    {
        or_expr.print(ps);
    }

    int pos_int_const()
    {
        return or_expr.pos_int_const();
    }

    public String toString()
    {
        return new String(value());
    }

    public String value() 
    {
        return or_expr.value();
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        or_expr.setPackage(s);
    }
}







