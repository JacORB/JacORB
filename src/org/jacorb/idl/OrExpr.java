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

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.util.*;
import java.io.PrintWriter;

class OrExpr 
    extends IdlSymbol
{
    public OrExpr or_expr = null;
    public XorExpr xor_expr;

    public OrExpr(int num)
    {
	super(num);
    }

    public void setContained( boolean contained )
    {
        xor_expr.setContained( contained );
    }

    public void print(PrintWriter ps)
    {
	if( or_expr != null ){
	    or_expr.print(ps);
	    ps.print(" | ");
	}
	xor_expr.print(ps);
	ps.flush();
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	if( pack_name.length() > 0 )
	    pack_name = new String( s + "." + pack_name );
	else
	    pack_name = s;
	if( or_expr != null ){
	    or_expr.setPackage(s);
	}
	xor_expr.setPackage( s);
    }

    public void parse()  
    {
	if( or_expr != null ){
	    or_expr.parse();
	}
	xor_expr.parse();
	}

    int pos_int_const()
    {
	return xor_expr.pos_int_const();
    }

    public String  value() 
    {
	String x = "";
	if( or_expr != null )
        {
	    x = or_expr.value() + "|";
	}
	return x + xor_expr.value();
    }

    public String toString() 
    {
	String x = "";
	if( or_expr != null )
        {
	    x = or_expr + "|";
	}
	return x + xor_expr;
    }
}





