package org.jacorb.idl;
 
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


import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */

class ValueAbsDecl 
    extends Value
{
    public ValueAbsDecl(int num)
    {
	super(num);
	pack_name = "";
    }
 
    public Object clone()
    {
        return null;
    }

    public TypeDeclaration declaration()
    {
	return this;
    };

    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	{
	    System.err.println("was " + enclosing_symbol.getClass().getName() + 
                               " now: " + s.getClass().getName());
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	}
	enclosing_symbol = s;	
    }

    public void parse() 	
    {
        System.err.println ("sorry, abstract valuetypes not yet supported " +
                            " -- ignoring definition");
    }

    public void print(PrintWriter ps)
    {
        System.err.println ("sorry, abstract valuetypes not yet supported " +
                            " -- ignoring definition");
    }
}



