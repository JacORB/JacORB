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

import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class InitDecl
    extends Declaration
{
    public Vector paramDecls;
    public IdlSymbol myValue;

    /** new in CORBA 3.0, factory methods may raise exceptions */
    public RaisesExpr raisesExpr;

    public InitDecl( int num )
    {
        super( num );
        paramDecls = new Vector();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );

        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        for( Enumeration e = paramDecls.elements();
             e.hasMoreElements();
             ( (ParamDecl)e.nextElement() ).setPackage( s )
                )
            ;
        raisesExpr.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );
        enclosing_symbol = s;
        raisesExpr.setEnclosingSymbol( s );
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
        raisesExpr.parse();
    }

    /**
     * Prints the method's signature, for inclusion in the
     * factory interface.
     */
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
        ps.print( ")" );
        raisesExpr.print( ps );
        ps.println( ";" );
    }

    /**
     * Prints the Helper method that corresponds to this factory method.
     */
    public void printHelperMethod( PrintWriter ps, String type_name )
    {
        ps.print( "\tpublic static " + type_name + " " + name + "( " );
        ps.print( "org.omg.CORBA.ORB orb" );

        for ( Enumeration e = paramDecls.elements();
              e.hasMoreElements(); )
        {
            ps.print( ", " );
            ( (ParamDecl)e.nextElement() ).print( ps );
        }
        ps.println (" )");

        ps.println ("\t{");
        ps.println ("\t\t" + type_name + "ValueFactory f = "
                    + "( " + type_name + "ValueFactory )"
                    + "((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory(id());");
        ps.println ("\t\tif (f == null)");
        ps.println ("\t\t\tthrow new org.omg.CORBA.MARSHAL( "
                    + "1, org.omg.CORBA.CompletionStatus.COMPLETED_NO );");
        ps.print   ("\t\treturn f." + name + "( ");

        for ( Enumeration e = paramDecls.elements();
              e.hasMoreElements(); )
        {
            ps.print (( (ParamDecl)e.nextElement() ).simple_declarator );
            if (e.hasMoreElements()) ps.print (", ");
        }

        ps.println (" );");

        ps.println ("\t}");
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
