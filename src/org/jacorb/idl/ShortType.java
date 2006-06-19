/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

public class ShortType
    extends IntType
{
    public ShortType( int num )
    {
        super( num );
    }

    public String typeName()
    {
        return "short";
    }

    /**
     * get this types's mapped Java name
     */

    public String getJavaTypeName()
    {
        return "short";
    }

    /**
     * get this symbol's IDL type name
     */

    public String getIDLTypeName()
    {
        if( unsigned )
        {
            return "ushort";
        }
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
        {
            return 4; //_tk_ushort
        }
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

    public String printReadExpression( String ps )
    {
        if( unsigned )
        {
            return ps + ".read_ushort()";
        }
        return ps + ".read_short()";
    }

    public String printReadStatement( String var_name, String ps )
    {
        if( unsigned )
        {
            return var_name + "=" + ps + ".read_ushort();";
        }
        return var_name + "=" + ps + ".read_short();";
    }

    public String printWriteStatement( String var_name, String ps )
    {
        if( unsigned )
        {
            return ps + ".write_ushort(" + var_name + ");";
        }
        return ps + ".write_short(" + var_name + ");";
    }

    public String printInsertExpression()
    {
        if( unsigned )
        {
            return "insert_ushort";
        }
        return "insert_short";
    }

    public String printExtractExpression()
    {
        if( unsigned )
        {
            return "extract_ushort";
        }
        return "extract_short";
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        ps.println( "\t\t" + anyname + "."
                 + printInsertExpression() + "(" + varname + ");");

    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = " + anyname + "." + printExtractExpression() + "();");
    }
}
