/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

class ShortType 
    extends IntType 
{
    public ShortType(int num) 
    {
        super(num);
    }

    public String typeName()
    {
        return "short";
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public boolean basic()
    {
        return true;
    }

    public int getTCKind()
    {
        return 2;
    }

    public String toString() 
    {
        return typeName();
    }

    public String holderName()
    {
        return "org.omg.CORBA.ShortHolder";
    }

    public String signature()
    {
        return "S";
    }

    public String printReadExpression(String ps)
    {
        return ps + ".read_short()";
    }

    public String printReadStatement(String var_name, String ps)
    {
        return var_name + "=" + ps + ".read_short();";
    }

    public String printWriteStatement(String var_name, String ps)
    {
        return ps + ".write_short(" + var_name + ");";
    }
    
    public String printInsertExpression()
    {
	return "insert_short";
    }

    public String printExtractExpression()
    {
	return "extract_short";
    }
}














