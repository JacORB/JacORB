package org.jacorb.idl;

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


import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */


public class Spec
    extends IdlSymbol
{
    public Vector definitions;

    public Spec( int num )
    {
        super( num );
        definitions = new Vector();
    }

    public void parse()
    {
        Enumeration e = definitions.elements();
        for( ; e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).parse();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        Enumeration e = definitions.elements();
        while( e.hasMoreElements() )
        {
            IdlSymbol i = (IdlSymbol)e.nextElement();
            i.setPackage( s );
        }
    }

    public void print( PrintWriter ps )
    {
        Enumeration e = definitions.elements();
        while( e.hasMoreElements() )
            ( (IdlSymbol)e.nextElement() ).print( ps );
    }

    /**
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitSpec( this );
    }


}
























