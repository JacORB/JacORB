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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class ValueInheritanceSpec
        extends SymbolList
{

    /** the value types (both abstract and stateful) inherited by this
     value type */
    public Vector v;

    /** the IDL interfaces inherited ("supported") by this value type */
    public Vector supports;

    /** if the value type this spec belongs to is truncatable to the
     single stateful ancestor value type */
    public Truncatable truncatable = null;

    public ValueInheritanceSpec( int num )
    {
        super( num );
        v = new Vector();
        supports = new Vector();
    }

    public String[] getTruncatableIds()
    {
        if( truncatable == null )
        {
            return new String[ 0 ];
        }
        else
        {
            return new String[]{truncatable.scopedName.toString()};
        }
    }

    public Enumeration getValueTypes()
    {
        return v.elements();
    }

    public Enumeration getSupportedInterfaces()
    {
        return supports.elements();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        if( truncatable != null )
            truncatable.scopedName.setPackage( s );

        for( Enumeration e = v.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( s );

        for( Enumeration e = supports.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( s );
    }

    public void parse()
    {
        Enumeration e = v.elements();
        for( ; e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).parse();
    }

    public void print( PrintWriter ps )
    {
        ps.print( toString() );
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        if( truncatable != null )
            sb.append( truncatable.toString() + " " );

        Enumeration e = v.elements();

        if( e.hasMoreElements() )
            sb.append( (IdlSymbol)e.nextElement() + " " );

        for( ; e.hasMoreElements(); )
        {
            sb.append( "," + (IdlSymbol)e.nextElement() + " " );
        }

        Enumeration s = supports.elements();
        if( s.hasMoreElements() )
        {
            sb.append( "supports " );
            ( (IdlSymbol)s.nextElement() ).toString();
        }

        for( ; s.hasMoreElements(); )
        {
            sb.append( "," );
            ( (IdlSymbol)s.nextElement() ).toString();
        }

        return sb.toString();
    }
}


