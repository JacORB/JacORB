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
public class CharType
    extends BaseType
    implements SwitchTypeSpec
{
    private boolean wide = false;

    public CharType( int num )
    {
        super( num );
    }

    public Object clone()
    {
        CharType s = new CharType( new_num() );
        if( wide )
        {
            s.setWide();
        }
        return s;
    }

    public boolean isWide()
    {
        return wide;
    }

    public void setWide()
    {
        wide = true;
    }

    public String typeName()
    {
        return "char";
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
        if( wide )
        {
            return 26;
        }
        return 9;
    }

    public String holderName()
    {
        return "org.omg.CORBA.CharHolder";
    }

    public String printReadExpression( String strname )
    {
        if( wide )
        {
            return strname + ".read_wchar()";
        }
        return strname + ".read_char()";
    }

    public String printWriteStatement( String var_name, String strname )
    {
        if( wide )
        {
            return strname + ".write_wchar(" + var_name + ");";
        }
        return strname + ".write_char(" + var_name + ");";
    }

    public String printInsertExpression()
    {
        if( wide )
        {
            return "insert_wchar";
        }
        return "insert_char";

    }

    public String printExtractExpression()
    {
        if( wide )
        {
            return "extract_wchar";
        }
        return "extract_char";
    }

    public boolean isSwitchable()
    {
        // wchar is not a valid union discriminator type

        return !wide;
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
