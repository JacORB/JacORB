/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import java.io.PrintWriter;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class AnyType 
    extends BaseType
{
    public AnyType(int num)
    {
        super(num);
    }

    public Object clone()
    {
        return new AnyType( new_num());
    }

    public String typeName()
    {
        return "org.omg.CORBA.Any";
    }

    public TypeSpec typeSpec()
    {
        return this;
    }
    public boolean basic()
    {
        return false;
    } 

    public String toString()
    {
        return typeName();
    }

    public String holderName()
    {
        return "org.omg.CORBA.AnyHolder";
    }

    public void parse() 
		 
    {}

    public int getTCKind()
    {
        return 11;
    }

    public String printReadExpression(String strname)
    {
        return strname + ".read_any()";
    }

    public String printWriteStatement(String var_name, String strname)
    {
        return strname + ".write_any(" + var_name + ");";
    }

}



