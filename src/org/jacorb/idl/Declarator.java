package org.jacorb.idl;

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

import java.io.PrintWriter;

/**
 * @author Gerald Brose
 * @version 1.0, December 1998
 */

public class Declarator
    extends IdlSymbol
{
    public Declarator d;

    public Declarator( int num )
    {
        super( num );
    }

    public String name()
    {
        return d.name();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        d.setPackage( s );
    }

    public void set_included( boolean i )
    {
        included = i;
        d.set_included( i );
    }

    String full_name()
    {
        return d.full_name();
    }

    /**
     * @overrides escapeName from IdlSymbol
     */

    public void escapeName()
    {
        d.escapeName();
    }

    public void parse()
    {
        d.parse();
    }

    public void print( PrintWriter ps )
    {
    }

    public String toString()
    {
        return d.toString();
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        d.setEnclosingSymbol( s );
    }

    public IdlSymbol getEnclosingSymbol()
    {
        return d.getEnclosingSymbol();
    }
}


