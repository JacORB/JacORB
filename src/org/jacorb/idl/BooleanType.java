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
 * @version $Id$
 */


class BooleanType
        extends BaseType
        implements SwitchTypeSpec
{

    public BooleanType( int num )
    {
        super( num );
    }

    public String typeName()
    {
        return "boolean";
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
        return 8;
    }


    public String toString()
    {
        return typeName();
    }

    public void parse()

    {
    }

    public String holderName()
    {
        return "org.omg.CORBA.BooleanHolder";
    }

    public String printReadExpression( String strname )
    {
        return strname + ".read_boolean()";
    }


    public String printWriteStatement( String var_name, String strname )
    {
        return strname + ".write_boolean(" + var_name + ");";
    }

    public String printInsertExpression()
    {
        return "insert_boolean";
    }

    public String printExtractExpression()
    {
        return "extract_boolean";
    }

    public boolean isSwitchable()
    {
        return true;
    }
}
