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

class StringType 
    extends TemplateTypeSpec  
{
    public ConstExpr max = null;
    private int length = 0;
    private boolean wide = false;

    public StringType(int num)
    {
	super(num);
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
	StringType s = new StringType(new_num());
	s.max = max;
	if( wide )
	    s.setWide();
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


    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	enclosing_symbol = s;
    }

    public void print(java.io.PrintWriter pw)
    {}

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	if( max != null )
	    max.setPackage(s);
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
	    return "org.omg.CORBA.ORB.init().create_wstring_tc(" + length + ")";
	else
	    return "org.omg.CORBA.ORB.init().create_string_tc(" + length + ")";
    }


    public String printReadExpression(String strname)
    {
	if( wide )
	    return  strname + ".read_wstring()";
	else
	    return strname + ".read_string()";
    }

    public String printReadStatement(String var_name, String strname)
    {
	if( wide )
	    return var_name + "=" + strname + ".read_wstring();";
	else
	    return var_name + "=" + strname + ".read_string();";
    }

    public String printWriteStatement(String var_name, String strname)
    {
	if( wide )
	    return strname + ".write_wstring("+var_name+");";
	else
	    return strname + ".write_string("+var_name+");";
    }


    public String printInsertExpression()
    {
	if( wide )
            return "insert_wstring";
	else
            return "insert_string";
    }

    public String printExtractExpression()
    {
	if( wide )
            return "extract_wstring";
	else
            return "extract_string";
    }

    public void parse() 	 
    {
	if( max != null )
	    length = max.pos_int_const();
    }
}


