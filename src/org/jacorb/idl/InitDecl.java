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

package org.jacorb.idl;

import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class InitDecl
    extends Declaration
{
    public Vector paramDecls;
    public IdlSymbol myValue;

    public InitDecl( int num )
    {
        super( num );
        paramDecls = new Vector();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );

        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        for( Enumeration e = paramDecls.elements();
             e.hasMoreElements();
             ( (ParamDecl)e.nextElement() ).setPackage( s )
                )
            ;
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );
        enclosing_symbol = s;
    }

    public void parse()
    {
        myValue = enclosing_symbol;

        try
        {
            NameTable.define( full_name(), "factory" );
        }
        catch( NameAlreadyDefined nad )
        {
            parser.error( "Factory " + full_name() + " already defined", token );
        }

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            param.parse();
            try
            {
                NameTable.define( full_name() + "." +
                        param.simple_declarator.name(),
                        "argument" );
            }
            catch( NameAlreadyDefined nad )
            {
                parser.error( "Argument " + param.simple_declarator.name() +
                        " already defined in operation " + full_name(),
                        token );
            }
        }
    }


    public void print( PrintWriter ps, String type_name )
    {
        ps.print( "\t" + type_name + " " + name + "( " );

        Enumeration e = paramDecls.elements();

        if( e.hasMoreElements() )
            ( (ParamDecl)e.nextElement() ).print( ps );

        for( ; e.hasMoreElements(); )
        {
            ps.print( ", " );
            ( (ParamDecl)e.nextElement() ).print( ps );
        }
        ps.println( ");" );
    }


    public String name()
    {
        return name;
    }


    public String opName()
    {
        return name();
    }


}




