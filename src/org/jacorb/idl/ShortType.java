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
        if( unsigned )
            return  4; //_tk_ushort
        else
            return 2; // _tk_short
    }

    public String toString() 
    {
        return typeName();
    }

    public String holderName()
    {
        return "org.omg.CORBA.ShortHolder";
    }


    public String printReadExpression(String ps)
    {
        if( unsigned )
            return ps + ".read_ushort()";
        else
            return ps + ".read_short()";
    }

    public String printReadStatement(String var_name, String ps)
    {
        if( unsigned )
            return var_name + "=" + ps + ".read_ushort();";
        else
            return var_name + "=" + ps + ".read_short();";
    }

    public String printWriteStatement(String var_name, String ps)
    {
        if( unsigned )
            return ps + ".write_ushort(" + var_name + ");";
        else
            return ps + ".write_short(" + var_name + ");";
    }
    
    public String printInsertExpression()
    {
        if( unsigned )
            return "insert_ushort";
        else
            return "insert_short";
    }

    public String printExtractExpression()
    {
        if( unsigned )
            return "extract_ushort";
        else
            return "extract_short";
    }
}


