/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 * @version $id$
 */


class FixedPointConstType
        extends BaseType
{

    public FixedPointConstType( int num )
    {
        super( num );
    }

    public Object clone()
    {
        return new FixedPointConstType( new_num() );
    }

    public String typeName()
    {
        return "java.math.BigDecimal";
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public String toString()
    {
        return typeName();
    }

    public boolean basic()
    {
        return true;
    }

    public int getTCKind()
    {
        return 28;
    }

    public void parse()
    {
    }

    public String holderName()
    {
        return "org.omg.CORBA.FixedHolder";
    }


    public String printReadExpression( String strname )
    {
        return strname + ".read_fixed()";
    }

    public String printWriteStatement( String var_name, String strname )
    {
        return strname + ".write_fixed(" + var_name + ");";
    }
}







