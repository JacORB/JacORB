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

package org.jacorb.idl;

import java.io.PrintWriter;
import java.util.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class AttrDecl
        extends Declaration
{

    public boolean readOnly;
    public TypeSpec param_type_spec;
    public SymbolList declarators;

    private Vector operations = new Vector();

    public AttrDecl( int num )
    {
        super( num );
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;
        declarators.setPackage( s );
        param_type_spec.setPackage( s );
    }

    public void parse()
    {
        IdlSymbol myInterface = enclosing_symbol;

        if( param_type_spec.typeSpec() instanceof ScopedName )
        {
            TypeSpec ts =
                    ( (ScopedName)param_type_spec.typeSpec() ).resolvedTypeSpec();
            if( ts != null )
                param_type_spec = ts;

            myInterface.addImportedName( ts.typeName() );
        }

        declarators.parse();

        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
        {
            operations.addElement(
                    new Method( param_type_spec,
                            null,
                            ( (SimpleDeclarator)e.nextElement() ).name(),
                            is_pseudo )
            );
        }
        if( !readOnly )
        {
            for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
            {
                SimpleDeclarator d =
                        (SimpleDeclarator)e.nextElement();
                operations.addElement(
                        new Method( null,
                                param_type_spec,
                                d.name(),
                                is_pseudo )
                );
            }
        }
    }

    public void print( PrintWriter ps )
    {
    }

    public Enumeration getOperations()
    {
        return operations.elements();
    }

    /**
     * collect Interface Repository information in the argument hashtable
     */

    public void getIRInfo( Hashtable irInfoTable )
    {
        for( Enumeration e = declarators.v.elements(); e.hasMoreElements(); )
        {
            String fullName = param_type_spec.full_name();
            irInfoTable.put( ( (SimpleDeclarator)e.nextElement() ).name(),
                    "attribute" + ( readOnly?"":"-w" ) + ";" +
                    ( fullName != null ? fullName :  param_type_spec.typeName() ) );
        }
    }


}




