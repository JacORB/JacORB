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

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

class ValueDeclaration
    extends TypeDeclaration
{
    private ValueDeclaration value_decl;

    public ValueDeclaration(int num) 
    {
	super(num);
	pack_name = "";
    }

    public Object clone()
    {
	return value_decl.clone();
    }

    public void setValueDeclaration( ValueDeclaration valueDeclaration)
    {
        value_decl = valueDeclaration;
    }

    public TypeDeclaration declaration()
    {
	return value_decl;
    };

    public String typeName()
    {
	return value_decl.typeName();
    }

    public String signature()
    {
	return value_decl.signature();
    }

   /**
     *	we have to be able to distinguish between explicitly typedef'd
     *	type names and anonymously defined type names
     */

//      public void markTypeDefd(String alias)
//      {
//  	value_decl.markTypeDefd(alias);
//      }

    /**
     * @returns a string for an expression of type TypeCode 
     * 			that describes this type
     */

    public String getTypeCodeExpression()
    {
	return value_decl.getTypeCodeExpression();
    }

    public boolean basic()
    {
	return value_decl.basic();
    } 

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
	value_decl.setPackage(s);
    }

    public void set_included(boolean i)
    {
	included = i;
	value_decl.set_included(i);
    }

    public void parse() 	 
    {
	value_decl.parse();
    }

    public String holderName()
    {
	return value_decl.holderName();
    }

    public void print(PrintWriter ps)
    {
	value_decl.print( ps );
    }

    public String toString()
    {
	return value_decl.toString();
    }
 
    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	{
	    System.err.println("was " + enclosing_symbol.getClass().getName() + " now: " + s.getClass().getName());
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	}
	enclosing_symbol = s;
	value_decl.setEnclosingSymbol(s);
    }

    public String printReadExpression(String streamname)
    {
	return value_decl.printReadExpression( streamname);
    }

    public String printReadStatement(String var_name, String streamname)
    {
	return var_name + "=" + printReadExpression(streamname) + ";";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
	return value_decl.printWriteStatement(var_name, streamname);
    }

}




