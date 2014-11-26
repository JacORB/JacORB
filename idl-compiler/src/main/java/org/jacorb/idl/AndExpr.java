package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 */

public class AndExpr
    extends IdlSymbol
{
    public AndExpr and_expr = null;
    public ShiftExpr shift_expr;

    public AndExpr(int num)
    {
        super(num);
    }

    public void print(PrintWriter ps)
    {
        if (and_expr != null)
        {
            and_expr.print(ps);
            ps.print(" & ");
        }
        shift_expr.print(ps);
    }


    public void setDeclaration(ConstDecl declared_in)
    {
        shift_expr.setDeclaration(declared_in);
    }

    public void setPackage(String packageName)
    {
        packageName = parser.pack_replace(packageName);
        if (pack_name.length() > 0)
        {
            pack_name = packageName + "." + pack_name;
        }
        else
        {
            pack_name = packageName;
        }

        if (and_expr != null)
        {
            and_expr.setPackage(packageName);
        }
        shift_expr.setPackage(packageName);
    }

    public void parse()
    {
        if (and_expr != null)
        {
            and_expr.parse();
        }
        shift_expr.parse();
    }

    int pos_int_const()
    {
        return shift_expr.pos_int_const();
    }

    public String toString()
    {
        String x = "";
        if (and_expr != null)
        {
            x = and_expr + "&";
        }
        return x + shift_expr;
    }

    public str_token get_token()
    {
        return shift_expr.get_token();
    }
}
