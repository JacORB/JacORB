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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SwitchBody
        extends IdlSymbol
{
    /** holds case list */
    public Vector caseListVector = new Vector();

    TypeSpec ts = null;
    UnionType myUnion = null;

    public SwitchBody( int num )
    {
        super( num );
    }

    public void setTypeSpec( TypeSpec s )
    {
        ts = s;
        for( Enumeration e = caseListVector.elements(); e.hasMoreElements(); )
        {
            Case c = (Case)e.nextElement();
            c.setPackage( pack_name );
            c.setTypeSpec( s );
        }
    }

    /**
     * pass a reference to the containing union through
     * to the case elements, which pass it on
     */

    public void setUnion( UnionType ut )
    {
        myUnion = ut;
        for( Enumeration e = caseListVector.elements(); e.hasMoreElements(); )
        {
            Case c = (Case)e.nextElement();
            c.setUnion( ut );
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
        for( Enumeration e = caseListVector.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setEnclosingSymbol( s );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        if( ts != null )
            ts.setPackage( s );
    }

    /**
     * do the parsing
     */

    public void parse()
    {
        Map usedLabelNames = new HashMap();

        for( Enumeration e = caseListVector.elements(); e.hasMoreElements(); )
        {
            Case theCase = (Case)e.nextElement();
            theCase.parse();

            // get all case labels and check for duplicates

            IdlSymbol[] labels = theCase.getLabels();

            for( int i = 0; i < labels.length; i++ )
            {
                if( labels[ i ] != null ) // null means default
                {
                    IdlSymbol sym =
                            (IdlSymbol)usedLabelNames.get( labels[ i ].toString() );

                    if( sym != null )
                    {
                        parser.error( "Duplicate case label <" +
                                sym.toString() + ">", sym.get_token() );
                    }

                    usedLabelNames.put( labels[ i ].toString(), labels[ i ] );
                }
            }
        }
        usedLabelNames.clear();

        ts.parse();
        myUnion.addImportedName( ts.typeName() );

    }

    public void print( java.io.PrintWriter ps )
    {
        for( Enumeration e = caseListVector.elements(); e.hasMoreElements(); )
        {
            Case c = (Case)e.nextElement();
            c.print( ps );
        }
    }
}
