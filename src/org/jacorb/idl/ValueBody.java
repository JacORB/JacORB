package org.jacorb.idl;

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

import java.util.*;
import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class ValueBody 
    extends InterfaceBody
{
    public ValueAbsDecl myAbsValue;

    ValueBody( int num )
    {
        super( num );
    }


    public void setEnclosingSymbol( IdlSymbol s )
    {
	if( enclosing_symbol != null && enclosing_symbol != s )
	    throw new RuntimeException("Compiler Error: trying to reassign container for " + name );
	enclosing_symbol = s;

	for(Enumeration e = v.elements(); e.hasMoreElements(); )
	    ((IdlSymbol)e.nextElement()).setEnclosingSymbol( myAbsValue );
    }

}








