/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
import java.util.Set;

/**
 * @author Gerald Brose
 */

public class StringType
    extends TemplateTypeSpec
{
    private static int tmpResultsCount = 0;

    public ConstExpr max = null;
    private boolean wide = false;

    public StringType( int num )
    {
        super( num );
    }

    public boolean isWide()
    {
        return wide;
    }

    public void setWide()
    {
        wide = true;
    }

    public Object clone()
    {
        StringType s = new StringType( new_num() );
        s.max = max;
        if( wide )
        {
            s.setWide();
        }
        s.parse();
        return s;
    }

    public String typeName()
    {
        return "java.lang.String";
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    public int getTCKind()
    {
    	return org.omg.CORBA.TCKind._tk_string;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );
        enclosing_symbol = s;
    }

    public void print( java.io.PrintWriter pw )
    {
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( max != null )
            max.setPackage( s );
    }

    public String toString()
    {
        return typeName();
    }

    public String holderName()
    {
        return "org.omg.CORBA.StringHolder";
    }

    public String getTypeCodeExpression()
    {
        if( wide )
        {
            return
            (
                "org.omg.CORBA.ORB.init().create_wstring_tc(" +
                (max == null ? 0 : max.pos_int_const()) +
                ')'
            );
        }
        return
        (
                "org.omg.CORBA.ORB.init().create_string_tc(" +
                (max == null ? 0 : max.pos_int_const()) +
                ')'
        );
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
    	return getTypeCodeExpression();
    }

    public String printReadExpression( String strname )
    {
        if( wide )
        {
            return strname + ".read_wstring()";
        }
        return strname + ".read_string()";
    }

    public String printReadStatement( String var_name, String strname )
    {
        String readExpr;
        if( wide )
        {
            readExpr = var_name + "=" + strname + ".read_wstring()";
        }
        else
        {
            readExpr = var_name + "=" + strname + ".read_string()";
        }

        // JAC#676: bounds check has been added
        if( max != null )
        {
            String varName =
                getFullName( ScopedName.unPseudoName( pack_name.length() > 0 ? pack_name + "." + name : name ));

            readExpr = "if( (" + readExpr + ").length() > " + max.pos_int_const()
                + " ) throw new org.omg.CORBA.BAD_PARAM(\"String bounds violation for " + varName
                + ": only not greater than <" + max.pos_int_const() + "> length is allowed\")";
        }

        readExpr += ";";
        return readExpr;
    }

    public String printWriteStatement( String var_name, String strname )
    {
        // JAC#676: bounds check has been added

        // prevent multiple calls when the method call is passed
        // as var_name - get the operation result into temporary
        // holder
        String tmpResultName = "tmpResult" + tmpResultsCount++;
        String writeStat = "java.lang.String " + tmpResultName + " = " + var_name + ";\n";


        String writeExpr;
        if( wide )
        {
            writeExpr = strname + ".write_wstring( " + tmpResultName + " )";
        }
        else
        {
            writeExpr = strname + ".write_string( " + tmpResultName + " )";
        }


        if( max != null)
        {
            writeStat += "if( " + tmpResultName + ".length() > " + max.pos_int_const()
                + " ) { throw new org.omg.CORBA.BAD_PARAM( \"String bounds violation for " + var_name
                + ": only not greater than <" + max.pos_int_const() + "> length is allowed\"); } else { "
                + writeExpr + "; }";
        }
        else
        {
            writeStat += writeExpr + ";";
        }

        return writeStat;
    }

    public String printInsertExpression()
    {
        if( wide )
        {
            return "insert_wstring";
        }
        return "insert_string";
    }

    public String printExtractExpression()
    {
        if( wide )
        {
            return "extract_wstring";
        }
        return "extract_string";
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

   public void setSize(ConstExpr max_)
   {
       max = max_;
   }

   public ConstExpr getSize()
   {
       return max;
   }
}
