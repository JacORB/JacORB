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

package org.jacorb.idl;

import java.io.PrintWriter;

/**
 * @author Gerald Brose
 */

public class LongType
    extends IntType
{
    public LongType( int num )
    {
        super( num );
    }

    public Object clone()
    {
        return new LongType( new_num() );
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public String typeName()
    {
        return "int";
    }

    public boolean basic()
    {
        return true;
    }

    public int getTCKind()
    {
        return ( ( unsigned ) ? 5 : 3 );
    }

    public String toString()
    {
        return typeName();
    }


    public String holderName()
    {
        return "org.omg.CORBA.IntHolder";
    }


    public String printReadExpression( String ps )
    {
        if( unsigned )
        {
            return ps + ".read_ulong()";
        }
        return ps + ".read_long()";
    }


    public String printWriteStatement( String var_name, String ps )
    {
        if( unsigned )
        {
            return ps + ".write_ulong(" + var_name + ");";
        }
        return ps + ".write_long(" + var_name + ");";
    }

    public String printInsertExpression()
    {
        if( unsigned )
        {
            return "insert_ulong";
        }
        return "insert_long";
    }

    public String printExtractExpression()
    {
        if( unsigned )
        {
            return "extract_ulong";
        }
        return "extract_long";
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
                                   String resulttype) {
        ps.println("\t\t" + resultname + " = " + anyname + "." + printExtractExpression() + "();");
    }

}
