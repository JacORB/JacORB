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

/**
 * @author Gerald Brose
 * @version $Id$
 */

import java.io.PrintWriter;

public class VoidTypeSpec
    extends TypeSpec
{
    public VoidTypeSpec( int num )
    {
        super( num );
    }

    public String typeName()
    {
        return "void";
    }

    /**
     * @return true if this is a basic type
     */

    public boolean basic()
    {        
        return true;
    }

    public void print( PrintWriter ps )
    {
    }

    public String toString()
    {
        return typeName();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
    }

    public void parse()
    {
    }

    public TypeSpec typeSpec()
    {
        return this;
    }
}
























